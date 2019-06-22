package com.alzzz.idlefishhook.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by Lyh on
 * 2019/6/19
 */
public class FileUtils {
    public static final String PATH_FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "IdlefishHook";
    public static final String PATH_OUT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "IdlefishHook"+"/"+"opt";
    public static final String FILE_NAME_CONFIG = "idlefish_hook_config";
    public static final String FILE_NAME_INTERCEPTOR = "idlefish_interceptor.jar";


    private static final String TAG = FileUtils.class.getSimpleName();


    /**
     * 清空文件
     *
     * @param fileName
     */
    public static boolean clearInfoForFile(String directory, String fileName) {
        File file = new File(directory, fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * 保存内容到文件
     *
     * @param directory
     * @param fileName
     * @param content
     *
     * @return
     *
     * @throws IOException
     */
    public static String saveContentToFile(String directory, String fileName, String content)
            throws IOException {
        StringBuffer buf = new StringBuffer();
        File file = null;
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileWriter output = null;
        file = new File(directory, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        String sb = null;
        try {
            output = new FileWriter(file, true);
            output.write(content);
            output.flush();
            sb = buf.toString();
        } catch (Exception e) {
            LOGGER.e(TAG, e.getMessage());
        } finally {
            CloseUtils.close(output);
        }
        return sb;

    }

    /**
     * 根据文件路径，得到文件的内容
     *
     * @param directory
     * @param fileName
     *
     * @return
     *
     * @throws IOException
     */
    public static String getFileContent(String directory, String fileName) throws IOException {
        StringBuffer buf = new StringBuffer();
        File file = new File(directory, fileName);
        if (!file.exists()){
            return "";
        }
        FileInputStream fileInputStream = new FileInputStream(new File(directory, fileName));
        InputStreamReader input = new InputStreamReader(fileInputStream, "utf-8");
        BufferedReader br = null;
        String sb = null;
        try {
            br = new BufferedReader(input);
            String str = null;
            while ((str = br.readLine()) != null) {
                buf.append(str);
            }
            sb = buf.toString();
        } catch (Exception e) {
            LOGGER.e(TAG, e.getMessage());
        } finally {
            CloseUtils.close(br);
        }
        return sb;
    }

    /***
     * 调用方式
     *
     * String path = Environment.getExternalStorageDirectory().toString() + "/" + "Tianchaoxiong/useso";
     String modelFilePath = "Model/seeta_fa_v1.1.bin";
     Assets2Sd(this, modelFilePath, path + "/" + modelFilePath);
     *
     * @param context
     * @param fileAssetPath assets中的目录
     * @param fileSdPath 要复制到sd卡中的目录
     */
    public static void Assets2Sd(Context context, String fileAssetPath, String fileSdPath){
        //测试把文件直接复制到sd卡中 fileSdPath完整路径
        File file = new File(fileSdPath);
        if (!file.exists()) {
            LOGGER.d("************文件不存在,文件创建");
            try {
                copyBigDataToSD(context, fileAssetPath, fileSdPath);
                LOGGER.d("************拷贝成功");
            } catch (IOException e) {
                LOGGER.d("************拷贝失败");
                e.printStackTrace();
            }
        } else {
            LOGGER.d("************文件夹存在,文件存在");
        }

    }
    public static void copyBigDataToSD(Context context, String fileAssetPath, String strOutFileName) throws IOException
    {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(fileAssetPath);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while(length > 0)
        {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }



}
