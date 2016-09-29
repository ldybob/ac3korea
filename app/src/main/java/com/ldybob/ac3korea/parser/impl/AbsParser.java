package com.ldybob.ac3korea.parser.impl;

import android.content.Context;
import android.os.AsyncTask;

import com.ldybob.ac3korea.ListItem;
import com.ldybob.ac3korea.ReplyItem;
import com.ldybob.ac3korea.parser.IPARSER;

import net.htmlparser.jericho.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsParser implements IPARSER {
    ArrayList<ListItem> mBBSList;
    ArrayList<ReplyItem> mReplyList;

    @Override
    public ArrayList<ListItem> getBoardList() {
        return mBBSList;
    }

    @Override
    public ArrayList<ReplyItem> getReplyList() {
        return mReplyList;
    }

    @Override
    public boolean ParsingList(Context context, String boardID, int pageNo, String searchWhere, String searchText, AsyncTask task) {
        return false;
    }

    @Override
    public boolean ParsingReply(Context context, List<Element> elements, AsyncTask task) {
        return false;
    }
}