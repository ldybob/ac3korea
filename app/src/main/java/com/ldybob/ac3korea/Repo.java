package com.ldybob.ac3korea;

import android.content.Context;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class Repo {
    private static Repo sInstance = null;
    private HttpClient httpclient;
    private String mBlackList;
    private Util mUtil;
    private MyInfo mInfo;

    private Repo(Context context) {
        httpclient = new DefaultHttpClient();
        mUtil = new Util(context);
        mBlackList = mUtil.RoadHideList();
        mInfo = null;
    }

    public static Repo getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Repo(context);
        }
        return sInstance;
    }

    public void setHttpclient() {
        httpclient = new DefaultHttpClient();
    }

    public HttpClient getHttpClient() {
        return httpclient;
    }

    public void setBlackList(String list) {
        mBlackList = list;
    }

    public String getBlackList() {
        return mBlackList;
    }

    public void setMyInfo(MyInfo info) {
        mInfo = info;
    }

    public MyInfo getMyInfo() {
        return mInfo;
    }
}
