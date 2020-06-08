package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @Description DisableSpdyHook
 * @Date 2020-06-02
 * @Author sz
 */
public class DisableSpdyHook implements IFishHook {
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    Class<?> instanceClazz;
    boolean hasHooked = false;


    public DisableSpdyHook(Context mApplicationContext, ClassLoader mClassLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = mClassLoader;
    }

    @Override
    public void startHook() {
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("mtopsdk.mtop.global.SwitchConfig", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                LOGGER.e(e.getMessage());
            }
        }

        doDisableSpdy();
    }

    private void doDisableSpdy() {
        XposedHelpers.findAndHookMethod(instanceClazz, "nI", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                synchronized (DisableSpdyHook.class){
                    if (hasHooked){
                        return;
                    }
                    hasHooked = true;
                    LOGGER.d("正在运行nI方法，开始hook");
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LOGGER.d("返回false");
                param.setResult(false);
            }
        });
    }

}
