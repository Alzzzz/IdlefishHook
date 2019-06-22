package com.alzzz.idlefishhook.xposed;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.alzzz.idlefishhook.bean.IdlefishConfig;
import com.alzzz.idlefishhook.utils.FileUtils;
import com.alzzz.idlefishhook.utils.LOGGER;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import dalvik.system.DexClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @Description OkHttpHook
 * @Date 2019-06-23
 * @Author sz
 */
public class OkHttpHook {
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    Class<?> instanceClazz = null;
    Class<?> mHttpLoggingInterceptor;
    Class<?> mLoggerClass;

    Boolean hasHooked = false;

    public OkHttpHook(Context mApplicationContext, ClassLoader classLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = classLoader;
    }

    /**
     * OkHttp Hook方法
     */
    public void hookOkHttp() {
        //闲鱼没有LoggingInterceptor只能通过自己写的hook进行
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("okhttp3.OkHttpClient$Builder", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        doOkHttpProxy();
    }

    /**
     * 进行动态匹配
     */
    private void doOkHttpProxy() {
        XposedHelpers.findAndHookMethod(instanceClazz, "build", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                synchronized (OkHttpHook.class){
                    if (hasHooked){
                        return;
                    }
                    hasHooked = true;
                    LOGGER.d("正在运行build方法，开始hook");
                    List interceptors = (List) XposedHelpers.getObjectField(param.thisObject, "interceptors");

                    if (interceptors != null){
                        //如果获取到了interceptors
                        LOGGER.d("interceptors = "+interceptors.size());
                        Object logInterceptor = getLoggingInterceptor();
                        if (logInterceptor != null){
                            //获取到了interceptor
                            LOGGER.d("获取到Log的interceptor");
                            interceptors.add(logInterceptor);
                        }
                        LOGGER.d("interceptors = "+interceptors.size());
                        LOGGER.e("has been hooked succeed!");
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    /**
     * 获取jar包中的interceptor
     * @return
     */
    private Object getLoggingInterceptor() {
        String jarPath = FileUtils.PATH_FILE_DIR + File.separator + FileUtils.FILE_NAME_INTERCEPTOR;
        LOGGER.d("当前jarPath="+jarPath);
        File file = new File(jarPath);
        if (file.exists()){
            LOGGER.d("存在interceptor的jar文件");
        }
        DexClassLoader dexClassLoader = new DexClassLoader(jarPath, FileUtils.PATH_OUT_DIR, null, mClassLoader);
        try {
            mHttpLoggingInterceptor = dexClassLoader.loadClass("com.alzzz.interceptor.AlzInterceptor");
            mLoggerClass = dexClassLoader.loadClass("com.alzzz.interceptor.AlzInterceptor$Logger");
            if (mLoggerClass != null && mHttpLoggingInterceptor != null) {
                LOGGER.e("获取到jar包中的拦截器");
                return InitInterceptor();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化拦截器
     * @return
     */
    private Object InitInterceptor() {
        //动态代理
        Object logger = Proxy.newProxyInstance(mLoggerClass.getClassLoader(), new Class[]{mLoggerClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                LOGGER.d((String) args[0]);

                Object result = method.invoke(proxy, args);

                return result;
            }
        });

        try {
            Object interceptor = mHttpLoggingInterceptor.getConstructor(mLoggerClass).newInstance(logger);
            return interceptor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
