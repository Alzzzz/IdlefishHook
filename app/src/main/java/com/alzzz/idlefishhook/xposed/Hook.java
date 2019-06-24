package com.alzzz.idlefishhook.xposed;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.alzzz.idlefishhook.bean.IdlefishConfig;
import com.alzzz.idlefishhook.utils.FileUtils;
import com.alzzz.idlefishhook.utils.LOGGER;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (IDLEFISH_NAME.equalsIgnoreCase(lpparam.packageName)){
            //匹配到闲鱼
            //初始化配置
            initConfig();
            setupHookComponent();
        }
    }

    /**
     * 组建context和appliction组件
     */
    private void setupHookComponent() {
        XposedHelpers.findAndHookMethod(Application.class, "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        mApplicationContext = (Context) param.args[0];
                        mClassLoader = mApplicationContext.getClassLoader();
                        LOGGER.d("mClassLoader = "+mClassLoader);
                        startHook();

                    }
                });
    }

    /**
     * 开始hook
     */
    private void startHook() {
        if (idlefishConfig.isOkhttpHook()){
            //开启了okHttp hook开关
            LOGGER.d("starting okhttp hook");
            IFishHook okHttpHook = new OkHttpHook(mApplicationContext, mClassLoader);
            okHttpHook.startHook();
        }

        if (idlefishConfig.isLogHook()){
            LOGGER.d("starting log hook");
            IFishHook logHook = new FishLogHook(mApplicationContext, mClassLoader);
            logHook.startHook();

        }
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
                LOGGER.d(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
