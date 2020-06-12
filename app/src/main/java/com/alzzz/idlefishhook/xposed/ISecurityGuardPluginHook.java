package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @Description ISecurityGuardPluginHook
 * @Date 2020-06-12
 * @Author sz
 */
public class ISecurityGuardPluginHook implements IFishHook {
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    Class<?> instanceClazz;
    boolean hasHooked = false;


    public ISecurityGuardPluginHook(Context mApplicationContext, ClassLoader mClassLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = mClassLoader;
    }

    @Override
    public void startHook() {
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("com.alibaba.wireless.security.framework.d", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                LOGGER.e(e.getMessage());
            }
        }

        doHookSecurityGuardPlugin();
    }

    private void doHookSecurityGuardPlugin() {
        XposedHelpers.findAndHookMethod(instanceClazz, "a", ClassLoader.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                synchronized (DisableSpdyHook.class){
                    if (hasHooked){
                        return;
                    }
                    hasHooked = true;
                    LOGGER.d("SecurityGuardPlugin_sz",param.args[1]+"");
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

}
