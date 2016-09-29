package com.ldybob.ac3korea.parser;

import android.content.Context;
import android.os.AsyncTask;

import com.ldybob.ac3korea.ListItem;
import com.ldybob.ac3korea.ReplyItem;

import net.htmlparser.jericho.Element;

import java.util.ArrayList;
import java.util.List;

public interface IPARSER {
    public ArrayList<ListItem> getBoardList();
    public ArrayList<ReplyItem> getReplyList();
    boolean ParsingList(Context context, String boardID, int pageNo, String searchWhere, String searchText, AsyncTask task);
    boolean ParsingReply(Context context, List<Element> elements, AsyncTask task);
}
