package com.ldybob.ac3korea;

public enum ReplyType {
    NEW("comment_regis"), MODIFY("cmodify"), REREPLY("creply");
    private final String mszValue;

    private ReplyType(String szValue) {
        mszValue = szValue;
    }

    @Override
    public String toString() {
        return mszValue;
    }
}
