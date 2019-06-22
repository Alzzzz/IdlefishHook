package com.alzzz.idlefishhook;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.alzzz.idlefishhook.bean.IdlefishConfig;
import com.alzzz.idlefishhook.utils.FileUtils;
import com.alzzz.idlefishhook.utils.LOGGER;
import com.alzzz.idlefishhook.utils.PermissionUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    CheckBox okHttpCb;
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

        PermissionUtil.initPermission(this, permissionList,
                new PermissionUtil.PermissionCallback() {
            @Override
            public void onPermissionSuccess() {
                doCopyConfigFile();
            }
        });
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
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "IdlefishHook";
        String Outpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "IdlefishHook"+"/"+"Out";
        File file = new File(path);
        File file1 = new File(Outpath);
        if (file.exists()) {
            LOGGER.e("文件已存在");
            file.delete();
        }
        file.mkdirs();
        file1.mkdirs();
        FileUtils.Assets2Sd(this,"idlefish_hook_config",path+"/"+"idlefish_hook_config");
        LOGGER.d("doCopyConfigFile finished");
    }

    private class OnHookCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == okHttpCb){

            }
        }
    }
}
