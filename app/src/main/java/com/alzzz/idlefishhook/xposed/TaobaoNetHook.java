package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @Description TaobaoNetHook
 * @Date 2019-06-24
 * @Author sz
 */
public class TaobaoNetHook implements IFishHook {
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    private Class<?> instanceClazz;

    public TaobaoNetHook(Context mApplicationContext, ClassLoader mClassLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = mClassLoader;
    }

    @Override
    public void startHook() {
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("com.taobao.android.remoteobject.mtopsdk.MtopSDKHandler", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                LOGGER.e(e.getMessage());
            }
        }
        if (instanceClazz == null){
            LOGGER.e("TaobaoNetHook ===> instanceClazz == null!!!");
            return;
        }
        LOGGER.d("TaobaoNetHook ===> instanceClazz="+instanceClazz);
        doTaobaoNetHook();
    }

    /**
     * 只hook MtopBuilder的asyncRequest方法
     */
    private void doTaobaoNetHook() {
        try {
            Class<?> remoteContextClazz = Class.forName("com.taobao.android.remoteobject.core.RemoteContext", true, mClassLoader);
            LOGGER.d("TaobaoNetHook ===> remoteContextClazz="+remoteContextClazz);

            try {
                Method method = instanceClazz.getMethod("preProcess", remoteContextClazz);
                LOGGER.d("TaobaoNetHook ===> method=["+method+"]");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            //找到对应方法
            XposedHelpers.findAndHookMethod(instanceClazz, "preProcess", remoteContextClazz, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object[] args = param.args;
                    if (args == null){
                        LOGGER.e("TaobaoNetHook ===> param.arg is null!!!");
                        return;
                    }

                    Object paramRemoteContext = param.args[0];

                    //得到internalRequest
                    Field internalRequestField = remoteContextClazz.getDeclaredField("internalRequest");
                    internalRequestField.setAccessible(true);
                    Object internalRequest = internalRequestField.get(paramRemoteContext);
                    //调用internalRequest的toString方法
                    Class<?> MtopRequest = Class.forName("mtopsdk.mtop.domain.MtopRequest", true, mClassLoader);
                    Method toStringMethod = MtopRequest.getMethod("toString");
                    toStringMethod.setAccessible(true);
                    String content = (String)toStringMethod.invoke(internalRequest);
                    LOGGER.d("TaobaoNetHook ===> request = ["+content+"]");
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
