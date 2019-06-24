package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @Description XModuleCenterHook
 * @Date 2019-06-24
 * @Author sz
 */
public class XModuleCenterHook implements IFishHook {
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    Class<?> instanceClazz;
    boolean hasHooked = false;

    public XModuleCenterHook(Context mApplicationContext, ClassLoader mClassLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = mClassLoader;
    }

    @Override
    public void startHook() {
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("com.taobao.idlefish.xmc.XModuleCenter", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                LOGGER.e(e.getMessage());
            }
        }
        LOGGER.d("addModuleName ===> instanceClazz="+instanceClazz);
        doXModuleHook();
    }

    /**
     * 进行hook的主逻辑
     */
    private void doXModuleHook() {
        //找到addModule方法，打印出对应内容
        XposedHelpers.findAndHookMethod(instanceClazz, "addModule", String.class, Object.class,
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LOGGER.d("has found addModule method!!!");
                if (param != null){
                    if (param.args != null && param.args.length > 1){
                        LOGGER.d("addModuleName ===> paramString = "+ param.args[0]+
                                " ,paramObject="+param.args[1]);
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }
}
