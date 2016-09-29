package com.ldybob.ac3korea;

import android.graphics.Bitmap;

public class ReplyItem {
    private Bitmap member_icon;
    private String member_name = "";
    private String member_id = "";
    private String content = "";
    private String time = "";
    private String replyid = "";
    private String answer = "";
    private int space = 0;

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

    public String getContent() {
        return content;
    }

    public void setContent(String var) {
        content = var;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String var) {
        time = var;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int var) {
        space = var;
    }

    public String getReplyid() {
        return replyid;
    }

    public void setReplyid(String var) {
        replyid = var;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String var) {
        answer = var;
    }
}

