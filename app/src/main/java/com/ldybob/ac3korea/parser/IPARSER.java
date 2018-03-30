package com.ldybob.ac3korea.parser;

import android.content.Context;
import android.os.AsyncTask;

import com.ldybob.ac3korea.ListItem;
import com.ldybob.ac3korea.ReplyItem;

import net.htmlparser.jericho.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser Interface
 */
public interface IPARSER {
    /**
     * 파싱한 게시판 글 목록 반환
     */
    public ArrayList<ListItem> getBoardList();

    /**
     * 파싱한 comment 목록 반환
     */
    public ArrayList<ReplyItem> getReplyList();

    /**
     * 게시판 파싱
     * @param context
     * @param boardID 파싱할 게시판 ID
     * @param pageNo 파싱할 page 번호
     * @param searchWhere 검색 category 설정(ex.제목, 글쓴이, 내용 등)
     * @param searchText 검색 할 text
     * @param task
     * @return 파싱 성공 여부
     */
    boolean ParsingList(Context context, String boardID, int pageNo, String searchWhere, String searchText, AsyncTask task);

    /**
     * 본문에 달린 comment 파싱
     * @param context
     * @param elements 댓글 목록 elements
     * @param task
     * @return 파싱 성공 여부
     */
    boolean ParsingReply(Context context, List<Element> elements, AsyncTask task);
}
