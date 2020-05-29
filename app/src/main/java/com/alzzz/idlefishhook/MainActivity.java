package com.alzzz.idlefishhook;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.alzzz.idlefishhook.bean.IdlefishConfig;
import com.alzzz.idlefishhook.utils.FileUtils;
import com.alzzz.idlefishhook.utils.LOGGER;
import com.alzzz.idlefishhook.utils.PermissionUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    CheckBox okHttpCb;
    CheckBox logCb;
    CheckBox xModuleCb;
    CheckBox taobaoNetCb;
    CheckBox pushPostCb;
    CheckBox securityGuardCb;

    IdlefishConfig idlefishConfig;

    String[] permissionList = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        okHttpCb = findViewById(R.id.cb_okhttp_hook);
        logCb = findViewById(R.id.cb_log_hook);
        xModuleCb = findViewById(R.id.cb_xmodule_hook);
        taobaoNetCb = findViewById(R.id.cb_taobao_net_hook);
        pushPostCb = findViewById(R.id.cb_push_post_hook);
        securityGuardCb = findViewById(R.id.cb_security_guard_hook);

        okHttpCb.setOnCheckedChangeListener(new OnHookCheckListener());
        logCb.setOnCheckedChangeListener(new OnHookCheckListener());
        xModuleCb.setOnCheckedChangeListener(new OnHookCheckListener());
        taobaoNetCb.setOnCheckedChangeListener(new OnHookCheckListener());
        pushPostCb.setOnCheckedChangeListener(new OnHookCheckListener());
        securityGuardCb.setOnCheckedChangeListener(new OnHookCheckListener());

        setupViews();

        PermissionUtil.initPermission(this, permissionList, this::doCopyConfigFile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.tv_save){
            saveConfig();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存设置
     */
    private void saveConfig() {
        String content = idlefishConfig.toJson();
        try {
            FileUtils.clearInfoForFile(FileUtils.PATH_FILE_DIR,
                    FileUtils.FILE_NAME_CONFIG);
            FileUtils.saveContentToFile(FileUtils.PATH_FILE_DIR,
                    FileUtils.FILE_NAME_CONFIG, content);
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            LOGGER.d("保存成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupViews() {
        //初始化配置的设置
        initConfig();
        if (idlefishConfig.isOkhttpHook()){
            okHttpCb.setChecked(true);
        } else {
            okHttpCb.setChecked(false);
        }

        if (idlefishConfig.isLogHook()){
            logCb.setChecked(true);
        } else {
            logCb.setChecked(false);
        }

        if (idlefishConfig.isXModuleCenterHook()){
            xModuleCb.setChecked(true);
        } else {
            xModuleCb.setChecked(false);
        }

        if (idlefishConfig.isTaobaoNetHook()){
            taobaoNetCb.setChecked(true);
        } else {
            taobaoNetCb.setChecked(false);
        }

        if (idlefishConfig.isPostServiceHook()){
            pushPostCb.setChecked(true);
        } else {
            pushPostCb.setChecked(false);
        }

        if (idlefishConfig.isSecurityGuardHook()){
            securityGuardCb.setChecked(true);
        } else {
            securityGuardCb.setChecked(false);
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissonResult = true;
        if (requestCode == 1){
            for (Integer result: grantResults){
                if (result < 0){
                    permissonResult = false;
                }
            }
            //授权成功
            if (permissonResult){
                doCopyConfigFile();
            }
        }
    }

    private void doCopyConfigFile(){
        LOGGER.d("doCopyConfigFile starting");
        File file = new File(FileUtils.PATH_FILE_DIR);
        if (file.exists()) {
            LOGGER.d("文件已存在");
        } else {
            file.mkdirs();
            FileUtils.Assets2Sd(this,FileUtils.FILE_NAME_CONFIG,FileUtils.PATH_FILE_DIR+File.separator+FileUtils.FILE_NAME_CONFIG);
            FileUtils.Assets2Sd(this,FileUtils.FILE_NAME_INTERCEPTOR,FileUtils.PATH_FILE_DIR+File.separator+FileUtils.FILE_NAME_INTERCEPTOR);
        }
        LOGGER.d("doCopyConfigFile finished");
    }

    private class OnHookCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == okHttpCb){
                idlefishConfig.setOkhttpHook(isChecked);
            } else if (buttonView == logCb){
                idlefishConfig.setLogHook(isChecked);
            } else if (buttonView == xModuleCb){
                idlefishConfig.setXModuleCenterHook(isChecked);
            } else if (buttonView == taobaoNetCb){
                idlefishConfig.setTaobaoNetHook(isChecked);
            } else if (buttonView == pushPostCb){
                idlefishConfig.setPostServiceHook(isChecked);
            } else if (buttonView == securityGuardCb){
                idlefishConfig.setSecurityGuardHook(isChecked);
            }
        }
    }
}
