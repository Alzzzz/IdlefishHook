package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import java.lang.reflect.Field;

/**
 * @Description FishLogHook
 * @Date 2019-06-23
 * @Author sz
 */
public class FishLogHook implements IFishHook {
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    Class<?> instanceClazz;
    boolean hasHooked = false;


    public FishLogHook(Context mApplicationContext, ClassLoader mClassLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = mClassLoader;
    }

    @Override
    public void startHook() {
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("com.taobao.idlefish.xframework.util.Log", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                LOGGER.e(e.getMessage());
            }
        }

        doLogProxy();
    }

    private void doLogProxy() {
        if (instanceClazz != null){
            try {
                LOGGER.d("Loghook ===> start log proxy");
                Field field = instanceClazz.getDeclaredField("enable");
                field.setAccessible(true);
                LOGGER.d("Loghook ===> field = "+field.get(null));
                field.set(null, true);
                LOGGER.d("Loghook ===> field = "+field.get(null));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
