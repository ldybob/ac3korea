package com.ldybob.ac3korea;

import android.graphics.Bitmap;

/**
 * 게시판 글 목록 Item
 */
public class ListItem {
    private Bitmap member_icon;
    private String member_name = "";
    private String member_id = "";
    private Bitmap type_icon;
    private String title = "";
    private String time = "";
    private String reply = "";
    private String uID = "";

    public Bitmap getMember_icon() {
        return member_icon;
    }

    public void setMember_icon(Bitmap var) {
        member_icon = var;
    }

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String var) {
        member_name = var;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String var) {
        member_id = var;
    }

    public Bitmap getType_icon() {
        return type_icon;
    }

    public void setType_icon(Bitmap var) {
        type_icon = var;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String var) {
        title = var;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String var) {
        time = var;
    }

    public String getReply() {
        return reply;
    }

    public void setComment(String var) {
        reply = var;
    }

    public String getUID() {
        return uID;
    }

    public void setUID(String var) {
        uID = var;
    }
}

