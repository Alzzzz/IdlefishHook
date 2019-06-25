package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

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

    private boolean outParams = false;

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
        if (outParams){
            doMtopRequestHook();
            doTopPropHook();
        }
        doTaobaoRequestHook();

    }

//    private void doHeaderHook() {
//        try {
//            Class<?> convertClazz = Class.forName("mtopsdk.mtop.protocol.converter.impl.AbstractNetworkConverter", true, mClassLoader);
//
//            XposedHelpers.findAndHookMethod(convertClazz, "a", Map.class, Map.class, boolean.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                    if (param.args != null){
//                        Map<String,String> param1 = (Map<String, String>) param.args[0];
//                        Map<String,String> param2 = (Map<String, String>) param.args[1];
//                        boolean param3 = (boolean) param.args[2];
//
//                        for (Map.Entry entry:param1.entrySet()){
//                            LOGGER.d("TaobaoNetHook ===> param1:[key="+
//                                    entry.getKey()+",value="+entry.getValue()+"]");
//                        }
//
//                        for (Map.Entry entry:param2.entrySet()){
//                            LOGGER.d("TaobaoNetHook ===> param1:[key="+
//                                    entry.getKey()+",value="+entry.getValue()+"]");
//                        }
//
//                        LOGGER.e("TaobaoNetHook ===> param3="+param3);
//                    }
//
//                }
//            });
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//    }

    private void doTaobaoRequestHook() {
        try {
            Class<?> convertFilterClazz = Class.forName("mtopsdk.framework.filter.before.NetworkConvertBeforeFilter", true, mClassLoader);
//            LOGGER.d("TaobaoNetHook ===> convertFilterClazz="+convertFilterClazz);

            Class<?> mtopContextClazz = Class.forName("mtopsdk.framework.domain.MtopContext", true, mClassLoader);
//            LOGGER.d("TaobaoNetHook ===> mtopContextClazz="+mtopContextClazz);

            XposedHelpers.findAndHookMethod(convertFilterClazz, "doBefore", mtopContextClazz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    //打印出相关的内容
                    if (param.args != null){
                        Object mtopContext = param.args[0];
                        Field requestField = null;

                        Field[] fields = mtopContextClazz.getFields();
                        for (Field field: fields){
//                            LOGGER.d("TaobaoNetHook ===> 当前field的属性名为:"+field.getType().getName());
                            if (field.getType().getName().equalsIgnoreCase("mtopsdk.network.domain.Request")){
//                                LOGGER.d("TaobaoNetHook ===> 找到request属性");
                                requestField = field;
                                break;
                            }
                        }
                        Object request = requestField.get(mtopContext);
                        Method toStirngMethod = request.getClass().getMethod("toString");
                        String content = (String) toStirngMethod.invoke(request);
                        LOGGER.e("TaobaoNetHook ===> request Content = ["+content+"]");
                    }

                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

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
