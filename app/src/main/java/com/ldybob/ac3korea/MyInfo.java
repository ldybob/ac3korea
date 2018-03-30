package com.ldybob.ac3korea;

/**
 * 로그인 한 사용자의 계정 data
 */
public class MyInfo {
    private String title;
    private String id;
    private String msg;
    private String point;
    private String nae;

    public MyInfo() {

    }

    public MyInfo(String t, String i, String m, String p, String n) {
        title = t;
        id = i;
        msg = m;
        point = p;
        nae = n;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public String getPoint() {
        return point;
    }

    public String getNae() {
        return nae;
    }
}
