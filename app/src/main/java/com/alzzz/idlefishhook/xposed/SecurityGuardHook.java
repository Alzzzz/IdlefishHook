package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @Description SecurityGuardHook
 * @Date 2020-05-25
 * @Author sz
 */
public class SecurityGuardHook implements IFishHook {
    Context mApplicationContext;
    ClassLoader mClassLoader;
    Class instanceClazz;
    public SecurityGuardHook(Context mApplicationContext, ClassLoader mClassLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = mClassLoader;
    }

    @Override
    public void startHook() {
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("com.alibaba.wireless.security.open.SecurityGuardManager", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                LOGGER.e(e.getMessage());
            }
        }
        if (instanceClazz == null){
            LOGGER.e("SecurityGuardHook ===> instanceClazz == null!!!");
            return;
        }
        LOGGER.d("SecurityGuardHook ===> instanceClazz="+instanceClazz);
        doSecurityGuardHook();
    }

    /**
     * 安全守卫hook
     */
    private void doSecurityGuardHook() {
        XposedHelpers.findAndHookMethod(instanceClazz, "getInterface", Class.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        LOGGER.d("has found getInterface method!!!, param="+param);
                        if (param != null){
                            LOGGER.d("SecurityGuardHook ===> [getResult="+ param.getResult()+", thisObject="+param.thisObject+", method="+param.method+", args="+ Arrays.toString(param.args)+"]");
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        LOGGER.d("after getInterface method!!!, param = "+param);
                        if (param != null){
                            LOGGER.d("getInterface ===> [getResult="+ param.getResult()+", thisObject="+param.thisObject+", method="+param.method+", args="+ Arrays.toString(param.args)+"]");
                        }
                    }
                });
    }
}
