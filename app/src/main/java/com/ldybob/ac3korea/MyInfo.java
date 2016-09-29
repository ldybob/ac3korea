package com.ldybob.ac3korea;

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
