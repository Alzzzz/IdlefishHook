package com.alzzz.idlefishhook.xposed;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alzzz.idlefishhook.bean.IdlefishConfig;
import com.alzzz.idlefishhook.utils.FileUtils;
import com.alzzz.idlefishhook.utils.LOGGER;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @Description Hook
 * @Date 2019-06-22
 * @Author sz
 */
public class Hook implements IXposedHookLoadPackage {
    private static final String IDLEFISH_NAME = "com.taobao.idlefish";
    private IdlefishConfig idlefishConfig = new IdlefishConfig();
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    Class<?> instanceClazz = null;
    Class<?> mHttpLoggingInterceptor;
    Class<?> mLoggerClass;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (IDLEFISH_NAME.equalsIgnoreCase(lpparam.packageName)){
            //匹配到闲鱼
            //初始化配置
            initConfig();
            if (idlefishConfig.isOkhttpHook()){
                //开启了okHttp hook开关
                LOGGER.d("starting okhttp hook");
                doOkHttpHook();
            }

        }
    }

    /**
     * 开始OkHttpHook
     */
    private void doOkHttpHook() {
        XposedHelpers.findAndHookMethod(Application.class, "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        mApplicationContext = (Context) param.args[0];
                        mClassLoader = mApplicationContext.getClassLoader();
                        LOGGER.d("mClassLoader = "+mClassLoader);
                        HookOkHttp();
                    }
                });
    }

    /**
     * OkHttp Hook方法
     */
    private void HookOkHttp() {
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
                List interceptors = (List) XposedHelpers.getObjectField(instanceClazz, "interceptors");

                if (interceptors != null){
                    //如果获取到了interceptors
                    LOGGER.d("interceptors = "+interceptors.size());
                    Object logInterceptor = getLoggingInterceptor();
                    if (logInterceptor != null){
                        //获取到了interceptor
                        LOGGER.d("获取到Log的interceptor");
                        interceptors.add(logInterceptor);
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
        DexClassLoader dexClassLoader = new DexClassLoader(FileUtils.PATH_FILE_DIR, FileUtils.PATH_OUT_DIR,
                null, mClassLoader);
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
        Object logger = Proxy.newProxyInstance(mClassLoader, new Class[]{mLoggerClass}, new InvocationHandler() {
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


    /**
     * 初始化设置，必须在最前
     */
    private void initConfig() {
        idlefishConfig = new IdlefishConfig();
        try {
            String content = FileUtils.getFileContent(FileUtils.PATH_FILE_DIR, FileUtils.FILE_NAME_CONFIG);
            if (!TextUtils.isEmpty(content)){
                JSONObject jsonObject = new JSONObject(content);
                idlefishConfig.decode(jsonObject);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
