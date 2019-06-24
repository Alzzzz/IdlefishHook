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
        doMtopRequestHook();
        doTopPropHook();

    }

    /**
     * 只hook MtopBuilder的asyncRequest方法
     */
    private void doMtopRequestHook() {
        try {
            Class<?> remoteContextClazz = Class.forName("com.taobao.android.remoteobject.core.RemoteContext", true, mClassLoader);
            LOGGER.d("TaobaoNetHook ===> remoteContextClazz="+remoteContextClazz);

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

    /**
     * 进行TopProp的hook尝试
     */
    private void doTopPropHook() {
        try {
            Class<?> mtopBuilderClazz = Class.forName("mtopsdk.mtop.intf.MtopBuilder", true, mClassLoader);
            LOGGER.d("TaobaoNetHook ===> mtopBuilderClazz="+mtopBuilderClazz);

            //hook asyncRequest方法
            Class<?> mtopListener = Class.forName("mtopsdk.mtop.common.MtopListener", true, mClassLoader);
            LOGGER.d("TaobaoNetHook ===> mCallbakClazz="+mtopListener);

            XposedHelpers.findAndHookMethod(mtopBuilderClazz, "asyncRequest", mtopListener, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Field mtopPropField = mtopBuilderClazz.getDeclaredField("mtopProp");
                    mtopPropField.setAccessible(true);
                    Object mtopProp = mtopPropField.get(param.thisObject);
                    LOGGER.d("TaobaoNetHook ===> mtopProp="+mtopProp);
                    Class<?> mtopNetworkPropClazz = Class.forName("mtopsdk.mtop.common.MtopNetworkProp", true, mClassLoader);
                    Method toStringMethod = mtopNetworkPropClazz.getMethod("toString");
                    LOGGER.d("TaobaoNetHook ===> toStringMethod="+toStringMethod);
                    String content = (String) toStringMethod.invoke(mtopProp);
                    LOGGER.e("TaobaoNetHook ===> mtopProp="+content);
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
