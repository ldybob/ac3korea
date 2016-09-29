package com.ldybob.ac3korea.parser.impl;

import android.content.Context;
import android.os.AsyncTask;

import com.ldybob.ac3korea.ListItem;
import com.ldybob.ac3korea.R;
import com.ldybob.ac3korea.ReplyItem;
import com.ldybob.ac3korea.Repo;
import com.ldybob.ac3korea.Util;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtherBBSParser extends AbsParser{
    private final String TAG = "OtherBBSParser";

    private Context mContext;
    private final String MAIN_URL = "https://www.ac3korea.com";
    private final String BOARD_URL = MAIN_URL + "/ac3korea?table=";
    private Util mUtil;
    private String boardID;

    public OtherBBSParser(String bID) {
        boardID = bID;
    }

    @Override
    public boolean ParsingList(Context context, String boardID, int pageNo, String searchWhere, String searchText, AsyncTask task) {
        mContext = context;
        mUtil = new Util(mContext);
        mBBSList = new ArrayList<ListItem>();

        try {
            String urlString = BOARD_URL + boardID + "&p=" + pageNo;
            if (!searchText.isEmpty()) {
                urlString = "https://www.ac3korea.com/ac3korea?table=" + boardID + "&where=" + searchWhere + "&keyword=" +
                        URLEncoder.encode(searchText, "euc-kr") +
                        "&search_step=1&search_sql=" +
                        URLEncoder.encode(searchWhere + " LIKE '|||" + searchText + "|||'", "euc-kr") +
                        "&p=" + pageNo;
            }
//            pageNo++;
            HttpPost httpPost = new HttpPost(urlString);
            if (Repo.getInstance(mContext).getHttpClient() == null) {
                mUtil.reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            final String responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, responseString);
            Source HTMLSource = new Source(responseString);

            List<Element> list = HTMLSource.getAllElements(HTMLElementName.TR);

            for(Element e : list) {
                if (AsyncTask.Status.RUNNING == task.getStatus()) {
                    String style = e.getAttributeValue("style");
                    String align = e.getAttributeValue("align");
                    String Class = e.getAttributeValue("class");
                    if (style != null && align != null && Class == null && style.equals("height:26px") && align.equals("center")) { //게시판에서 글목록 내부 태그만 뽑아오는 부분
                        ParsingBBSItem(e);
                    }
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 글 목록에서 각각의 아이템을 파싱하여 리스트뷰에 표시하기위한 ArrayList를 만듦
     * @param e 게시판 글 목록내 하나의 아이템에 대한 Element
     */
    public void ParsingBBSItem(Element e) {
        String member_icon = "";
        String member_name = "";
        String type_icon = "";
        String title = "";
        String time = "";
        String comment = "";
        String linkID = "";
        List<Element> list = e.getChildElements();
        ListItem item = new ListItem();
//        Log.d(TAG, "----------------list1 size = " + list.size());
        for(int i = 0; i < list.size(); i++) {
            String element = list.get(i).getContent().toString();
//            Log.d(TAG, "element" + i + " = " + element);
            String[] var;
            if (i == 1) {
                // 목록 클릭 시 링크 ID 가져오는 부분
                var = element.split("Skin_MultiCheck\\(");
                if (var.length >= 2) {
                    var = var[1].split(",");
                    linkID = var[0];
                }
                item.setUID(linkID);
            } else if (i == 3) {
                // 게시글 내 이미지/동영상 타입여부
                if (element.contains("ico_movie.gif")) {
                    type_icon = MAIN_URL + "/bbs/skin/speed_b/image/ico_movie.gif";
                } else if (element.contains("image.gif")) {
                    type_icon = MAIN_URL + "/image/image.gif";
                }
                if (type_icon.isEmpty()) {
                    item.setType_icon(null);
                } else {
                    item.setType_icon(mUtil.LoadImageFromWebOperations(type_icon));
                }

                // 게시글 제목 가져오는 부분
                var = element.split(",'" + boardID + "','',event\\);\">");
                if (var.length >= 2) {
                    var = var[1].split("</a>");
                    title = var[0];
                }
                item.setTitle(title);

                // 댓글수 가져오는 부분
                var = element.split("<script type=\"text/javascript\">Skin_ComentCheck\\(");
                if (var.length >= 2) {
                    var = var[1].split("\\)");
                    comment = var[0];
                }
                item.setComment(comment);
            } else if (i == 4) {
                // 작성자 이름 가져오는 부분
//                Log.d(TAG, element.toString());
                Pattern p = Pattern.compile("\\'(.*?)\\'");
                Matcher m = p.matcher(element.toString());
                for (int j = 0; j < 2; j++) {
                    if (m.find()) {
                        if (j == 0) {
                            item.setMember_name(m.group().replaceAll("'", ""));
                        } else if (j == 1) {
                            item.setMember_id(m.group().replaceAll("'", ""));
                        }
                    }
                }
//                var = element.split("onclick=\"getUserIdLayer\\('");
//                if (var.length >= 2) {
//                    var = var[1].split("'");
//                    member_name = var[0];
//                }
//                item.setMember_name(member_name);

                // 작성자 아이콘 가져오는 부분
                var = element.split(",event\\)\"><IMG SRC='.");
                if (var.length >= 2) {
//                    var[1] = var[1].substring(2);
                    var = var[1].split("'");
//                    var = var[0].split("\"");
                    member_icon = MAIN_URL + var[0];
                }
                if (member_icon.isEmpty()) {
                    member_icon = MAIN_URL + "/image/default_icon.gif";
                }
                item.setMember_icon(mUtil.LoadImageFromWebOperations(member_icon));
            } else if (i == 5) {
                // 작성시간 가져오는 부분
                char c = element.charAt(0);
                if (c == '<') {
                    var = element.split("getDateFormat\\('");
                    if (var.length >= 2) {
                        var = var[1].split("'");
                        time = var[0].substring(0,8);
                    }
                } else {
                    time = element;
                }
                item.setTime(time);
            }
        }
        String hidelist = Repo.getInstance(mContext).getBlackList();
        if (!((hidelist.indexOf(item.getMember_name()) >= 0) || (hidelist.indexOf(item.getMember_id()) >= 0))) {
            mBBSList.add(item);
        }

//        Log.d(TAG, "getType_icon = " + item.getType_icon());
//        Log.d(TAG, "getComment = " + item.getReply());
//        Log.d(TAG, "getLinkID = " + item.getUID());
//        Log.d(TAG, "getMember_icon = " + item.getMember_icon());
//        Log.d(TAG, "getMember_name = " + item.getMember_name());
//        Log.d(TAG, "getTime = " + item.getTime());
//        Log.d(TAG, "getTitle = " + item.getTitle());
//        for(Element e1 : list) {
//            Log.d(TAG, "link1 id = " + e1.getContent().toString());
//        }
    }

    @Override
    public boolean ParsingReply(Context context, List<Element> elements, AsyncTask task) {
        mContext = context;
        mUtil = new Util(mContext);
        mReplyList = new ArrayList<ReplyItem>();
        for(Element e : elements) {
            if (AsyncTask.Status.RUNNING == task.getStatus()) {
                String atr_id = e.getAttributeValue("id");
                if (atr_id != null && atr_id.indexOf("comment_") >= 0) {
                    getReply(e.getFirstElement());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public void getReply(Element e) {
        ReplyItem item = new ReplyItem();
        int space = 0;
        String[] var;

        // 댓글 ID 가져오는 부분
        String atr_id = e.getAttributeValue("id");
        atr_id = atr_id.replace("comment_", "");
        item.setReplyid(atr_id);
//        Log.d(TAG, "commentid = " + item.getReplyid());

        // 이름/id 가져오는 부분
        Element eA = e.getFirstElement(HTMLElementName.A);
        String onclick = eA.getAttributeValue("onclick");
//        Log.d(TAG, "onclick = " + onclick);
        Pattern p = Pattern.compile("\\'(.*?)\\'");
        Matcher m = p.matcher(onclick);
        for (int i = 0; i < 2; i++) {
            if (m.find()) {
                if (i == 0) {
                    item.setMember_name(m.group().replaceAll("'", ""));
                } else if (i == 1) {
                    item.setMember_id(m.group().replaceAll("'", ""));
                }
            }
        }
//        while(m.find()) {
//            Log.d(TAG, "onclick111 = " + m.group() + ", " + m.groupCount());
//        }

        // 대댓글 상태 가져오는 부분
        Element eTD = e.getFirstElement(HTMLElementName.TD);
        Element eIMG = eTD.getFirstElement(HTMLElementName.IMG);
        if (eIMG != null) {
            String rereply = eIMG.getAttributeValue("width");
            space = Integer.valueOf(rereply);
        }
//        Log.d(TAG, "width = " + space);
        item.setSpace(space/20);

        // 멤버이미지 가져오는 부분
        List<Element> eIMGList = e.getAllElements(HTMLElementName.IMG);
        String img_url = "";
        // 대댓글 여부에 따라 댓글 앞에 blank 이미지를 삽입하기 때문에 IMG 태그 리스트 get 시 위치가 달라짐.
        if (space == 0) {
            // 일반 댓글인경우
            img_url = eIMGList.get(0).toString();
        } else {
            // 대댓글 인경우
            img_url = eIMGList.get(1).toString();
        }
        m = p.matcher(img_url);
        if (m.find()) {
            img_url = m.group().toString().replaceAll("'", "");
            img_url = img_url.replaceFirst(".", MAIN_URL);
        } else {
            img_url = MAIN_URL + "/image/default_icon.gif";
        }
//        Log.d(TAG, "img = " + img_url);
        item.setMember_icon(mUtil.LoadImageFromWebOperations(img_url));

        // 댓글 내용가져오는 부분
        Element eTEXTAREA = e.getFirstElement(HTMLElementName.TEXTAREA);
//        Log.d(TAG, "text = " + eTEXTAREA.getContent().toString());
        item.setContent(eTEXTAREA.getContent().toString());

        // 시간정보 가져오는 부분
        List<Element> eSPAN = e.getAllElements(HTMLElementName.SPAN);
        if (eSPAN.get(1).toString().indexOf("getDateFormat('") >= 0) {
            var = eSPAN.get(1).toString().split("getDateFormat\\('");
        } else {
            var = eSPAN.get(2).toString().split("getDateFormat\\('");
        }
        String time = "";
        if (var.length >= 2) {
            var = var[1].split("'");
            time = var[0];
        }
//        Log.d(TAG, "span = " + time);
        item.setTime(time);

        String blacklist = Repo.getInstance(mContext).getBlackList();
//        if (!((blacklist.indexOf(item.getMember_name()) >= 0) || (blacklist.indexOf(item.getMember_id()) >= 0))) {
//            mReplyList.add(item);
//        }
        if ((blacklist.indexOf(item.getMember_name()) >= 0) || (blacklist.indexOf(item.getMember_id()) >= 0)) {
            item.setContent(mContext.getString(R.string.hide_reply_content));
        }
        mReplyList.add(item);
    }
}
