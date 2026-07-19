package com.example.nokeybypass;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "NokeyBypass";
    private static final String TARGET_PKG = "com.ingeek.nokey";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(TARGET_PKG)) {
            return;
        }

        Log.d(TAG, "✅ 已注入乘趣 " + lpparam.processName);

        hookSignatureCheck(lpparam);
        hookSecurityCheck(lpparam);
        hookSkinUnlock(lpparam);
    }

    private void hookSignatureCheck(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> appConstants = XposedHelpers.findClass(
                    "com.ingeek.nokeeu.key.util.AppConstants",
                    lpparam.classLoader);

            XposedHelpers.findAndHookMethod(appConstants,
                    "compareNokeySignaturesSHA1",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(Boolean.TRUE);
                            Log.d(TAG, "🔓 绕过签名校验");
                        }
                    });

            Log.d(TAG, "✅ 签名校验已绕过");
        } catch (Exception e) {
            Log.e(TAG, "❌ 签名校验Hook失败: " + e.getMessage());
        }
    }

    private void hookSecurityCheck(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> baseInspector = XposedHelpers.findClass(
                    "com.ingeek.nokeeu.key.security.c.e.O00000Oo",
                    lpparam.classLoader);

            XposedHelpers.findAndHookMethod(baseInspector,
                    "O00000o0",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            int result = (int) param.args[0];
                            if (result != 1) {
                                Log.d(TAG, "🛡️ 安全检测拦截: " + result + " → 通过");
                                param.args[0] = 1;
                            }
                        }
                    });

            Log.d(TAG, "✅ 安全检测已绕过");
        } catch (Exception e) {
            Log.e(TAG, "❌ 安全检测Hook失败: " + e.getMessage());
        }
    }

    private void hookSkinUnlock(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> skinItem = XposedHelpers.findClass(
                    "com.ingeek.nokey.network.entity.SkinItem",
                    lpparam.classLoader);

            XposedHelpers.findAndHookMethod(skinItem, "canUse",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(true);
                        }
                    });

            XposedHelpers.findAndHookMethod(skinItem, "isSupportTry",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(true);
                        }
                    });

            XposedHelpers.findAndHookMethod(skinItem, "isTrialExpired",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(false);
                        }
                    });

            XposedHelpers.findAndHookMethod(skinItem, "isNeedShowPrice",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(false);
                        }
                    });

            XposedHelpers.findAndHookMethod(skinItem, "getKeepStatus",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(3);
                        }
                    });

            XposedHelpers.findAndHookMethod(skinItem, "getUsingFlag",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(1);
                        }
                    });

            Log.d(TAG, "✅ 皮肤全部解锁完成");
        } catch (Exception e) {
            Log.e(TAG, "❌ 皮肤解锁Hook失败: " + e.getMessage());
        }
    }
}
