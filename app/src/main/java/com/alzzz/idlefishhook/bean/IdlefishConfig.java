package com.alzzz.idlefishhook.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Description IdlefishConfig
 * @Date 2019-06-22
 * @Author sz
 */
public class IdlefishConfig {
    private boolean okhttpHook;
    private boolean logHook;

    public boolean isOkhttpHook() {
        return okhttpHook;
    }

    public void setOkhttpHook(boolean okhttpHook) {
        this.okhttpHook = okhttpHook;
    }

    public boolean isLogHook() {
        return logHook;
    }

    public void setLogHook(boolean logHook) {
        this.logHook = logHook;
    }

    public String toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("okhttpHook", okhttpHook);
            jsonObject.put("logHook", logHook);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public void decode(JSONObject jsonObject){
        if (jsonObject != null){
            if (jsonObject.has("okhttpHook")){
                okhttpHook = jsonObject.optBoolean("okhttpHook");
            }

            if (jsonObject.has("logHook")){
                logHook = jsonObject.optBoolean("logHook");
            }
        }
    }
}
