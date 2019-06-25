package com.alzzz.idlefishhook.xposed;

import android.content.Context;

import com.alzzz.idlefishhook.utils.LOGGER;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @Description PostServiceHook
 * @Date 2019-06-25
 * @Author sz
 */
public class PostServiceHook implements IFishHook {
    private Context mApplicationContext;
    private ClassLoader mClassLoader;

    private Class<?> instanceClazz;

    public PostServiceHook(Context mApplicationContext, ClassLoader mClassLoader) {
        this.mApplicationContext = mApplicationContext;
        this.mClassLoader = mClassLoader;
    }

    @Override
    public void startHook() {
        if (instanceClazz == null) {
            try {
                instanceClazz = Class.forName("com.taobao.fleamarket.rent.publish.view.RentPubController", true, mClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                LOGGER.e(e.getMessage());
            }
        }
        if (instanceClazz == null){
            LOGGER.e("PostServiceHook ===> instanceClazz == null!!!");
            return;
        }
        LOGGER.d("PostServiceHook ===> instanceClazz="+instanceClazz);
        doPostServiceHook();

    }

    private void doPostServiceHook() {
        XposedHelpers.findAndHookMethod(instanceClazz, "onPriceClicked", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Class<?> rentPostServiceActionClass = Class.forName("com.taobao.fleamarket.rent.publish.model.RentPostServiceAction", true, mClassLoader);
                Field actionField = null;
                //先获取this.b.a()
                Field[] fields = instanceClazz.getDeclaredFields();
                for (Field field: fields){
                    if (field.getType().equals(rentPostServiceActionClass)){
                        actionField = field;
                        LOGGER.d("has found RentPostServiceAction");
                    }
                }

                if (actionField == null){
                    LOGGER.e("RentPostServiceAction is null");
                    return;
                }
                actionField.setAccessible(true);
                Object rentPostServiceAction = actionField.get(param.thisObject);
                Method getItemPostDo = rentPostServiceAction.getClass().getMethod("a");
                Object itemPostDO = getItemPostDo.invoke(rentPostServiceAction);
                //修改properties和setCategoryId
                Field propertiesField = itemPostDO.getClass().getDeclaredField("properties");
                Field categoryIdField = itemPostDO.getClass().getDeclaredField("categoryId");

                propertiesField.setAccessible(true);
                categoryIdField.setAccessible(true);

                //hook住方法进行循环请求
                List<String> locationList = new ArrayList<>();
                locationList.add("location:116.464447999999_39.996400998899_望京港旅大厦_望京南湖南路9号_3087482");
                locationList.add("location:116.506638999412_39.960702002258_梵谷水郡_驼房营南路2号院_2883936");
                locationList.add("location:116.506638999412_39.960702002258_梵谷水郡_驼_2883936");
                locationList.add("location:116.506638999412_39.960702002258_梵谷水郡_驼房营南路2号院_2");
                new Thread(()->{
                        for (String location: locationList){
                            String props = location+";layout_173424908_173406998_173386994:173424908,173406998,173386994;173424908:125877756;173406998:48347831;173386994:135728944;prop_11773557:102.0;prop_11773703:105228641;price:0.0;prop_202698006:随时入住;";
                            try {
                                LOGGER.d("开始请求对应数据");
                                propertiesField.set(itemPostDO, props);
                                categoryIdField.set(itemPostDO, 127212002L);
                                //找到rentPostServiceAction的oF方法并运行
                                Method oF = rentPostServiceAction.getClass().getDeclaredMethod("oF");
                                oF.invoke(rentPostServiceAction);
                                Thread.sleep(1000);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                }).start();

            }
        });

        try {
            Class<?> rentPostServiceActionClazz = Class.forName("com.taobao.fleamarket.rent.publish.model.RentPostServiceAction", true, mClassLoader);
            XposedHelpers.findAndHookMethod(rentPostServiceActionClazz, "oF", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Field postDOField = rentPostServiceActionClazz.getDeclaredField("mItemPostDO");
                    postDOField.setAccessible(true);
                    Object postDO = postDOField.get(param.thisObject);
                    Field propsField = postDO.getClass().getDeclaredField("properties");

                    String properties = (String) propsField.get(postDO);
                    LOGGER.d("properties = "+properties);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Class<?> priceEventClazz = Class.forName("com.taobao.fleamarket.rent.publish.model.RentPostServiceAction$PriceEstimateEvent",true, mClassLoader);
            LOGGER.d("priceEventClazz="+priceEventClazz);
            //进行内容输出
            XposedHelpers.findAndHookMethod(instanceClazz, "onPriceEstimateReturn", priceEventClazz, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object object = param.args[0];
                    //找到对最大值和最小值
                    Field maxPriceField = object.getClass().getDeclaredField("maxPrice");
                    Field minPriceField = object.getClass().getDeclaredField("minPrice");

                    float maxPrice = (float) maxPriceField.get(object);
                    float minPrice = (float) minPriceField.get(object);

                    LOGGER.e("最大值为："+maxPrice+", 最小值为："+minPrice);

                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
