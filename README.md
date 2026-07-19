# NokeyBypass

绕过某数字钥匙签名校验、安全检测和皮肤锁的 Xposed 模块。

## Hook 工作原理

模块入口在 `com.example.nokeybypass.MainHook`，使用 Xposed 的 `handleLoadPackage` 拦截目标应用进程，根据包名命中后注册三个独立的 Hook 回调：

### 1. 签名校验绕过

目标应用的签名校验通常在 Application 或 Activity 的 onCreate 阶段检查自身签名，如果发现签名与官方不一致就闪退。

Hook 思路：找到校验方法，直接让它返回「通过」的结果。
- 如果校验方法返回 `boolean`（如 `checkSignature()`），Hook 后固定 `return true`
- 如果校验方法抛出异常才算失败，Hook 后 `return` 空值或伪造一个合法的 Signature 对象
- 如果签名校验写在 native 层，Hook `PackageManager.getPackageInfo` 或 `PackageInfo.signatures`，在 Java 层把返回值换成官方签名

### 2. 安全检测绕过

目标应用会检测 Root 权限、Xposed 框架、调试模式等环境特征，检测到后阻止运行。

Hook 策略按检测方式分三类：

- **文件/进程检测**：检测 `/sbin/su`、`/system/app/Superuser.apk` 是否存在，或检查 `com.topjohnwu.magisk` 等进程。Hook `File.exists()`、`File.length()`、`Runtime.exec()`，对敏感路径返回「文件不存在」或过滤掉相关进程名。
- **Java 反射检测**：调用 `Class.forName("de.robv.android.xposed.XposedBridge")` 来确认 Xposed 是否存在。Hook `Class.forName` 和 `ClassLoader.loadClass`，对 Xposed 相关类名抛出 `ClassNotFoundException`。
- **build 标签检测**：检查 `Build.TAGS.contains("test-keys")` 来判断是否刷过第三方 ROM。直接 Hook `Build.TAGS` 的 get 方法，返回 `release-keys`。

### 3. 皮肤解锁

数字钥匙的付费皮肤通常通过在线验证或本地校验来控制解锁。Hook 所有与皮肤相关的接口调用，直接赋予解锁状态。

- **皮肤数据接口**：Hook 皮肤列表的获取方法，在返回数据里把所有皮肤的 `isLocked`、`isTrial`、`isExpired` 等字段全部设为 `false`。
- **本地校验绕过**：如果皮肤验证依赖本地签名或 hash 校验，用方法 1 的方式绕过
- **付费状态接管**：找到皮肤管理类的 `isPurchased()` 或类似方法，直接 Hook 返回 `true`