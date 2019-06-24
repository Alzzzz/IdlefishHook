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
    private boolean XModuleCenterHook;
    private boolean taobaoNetHook;

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

    public boolean isXModuleCenterHook() {
        return XModuleCenterHook;
    }

    public void setXModuleCenterHook(boolean XModuleCenterHook) {
        this.XModuleCenterHook = XModuleCenterHook;
    }

    public boolean isTaobaoNetHook() {
        return taobaoNetHook;
    }

    public void setTaobaoNetHook(boolean taobaoNetHook) {
        this.taobaoNetHook = taobaoNetHook;
    }

    public String toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("okhttpHook", okhttpHook);
            jsonObject.put("logHook", logHook);
            jsonObject.put("XModuleCenterHook", XModuleCenterHook);
            jsonObject.put("taobaoNetHook", taobaoNetHook);
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

            if (jsonObject.has("XModuleCenterHook")){
                XModuleCenterHook = jsonObject.optBoolean("XModuleCenterHook");
            }

            if (jsonObject.has("taobaoNetHook")){
                taobaoNetHook = jsonObject.optBoolean("taobaoNetHook");
            }
        }
    }
}
