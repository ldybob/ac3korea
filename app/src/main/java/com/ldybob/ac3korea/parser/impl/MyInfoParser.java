package com.ldybob.ac3korea.parser.impl;

import android.content.Context;
import android.util.Log;

import com.ldybob.ac3korea.BoardID;
import com.ldybob.ac3korea.Const;
import com.ldybob.ac3korea.MyInfo;
import com.ldybob.ac3korea.Repo;
import com.ldybob.ac3korea.Util;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class MyInfoParser {
    private final String TAG = "MyInfoParser";
    Context mContext;
    private final String MAIN_URL = Const.http + "www.ac3korea.com";
    private final String BOARD_URL = MAIN_URL + "/ac3korea?table=";

    private Util mUtil;

    public MyInfoParser(Context context) {
        mContext = context;
        mUtil = new Util(mContext);
    }

    public MyInfo getMyInfo() {
        MyInfo info = new MyInfo();
        try {
            String urlString = BOARD_URL + BoardID.FREE + "&p=" + 1;
            HttpPost httpPost = new HttpPost(urlString);
            if (Repo.getInstance(mContext).getHttpClient() == null) {
                mUtil.reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            final String responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, responseString);
            Source HTMLSource = new Source(responseString);
            List<Element> infolist = HTMLSource.getAllElements(HTMLElementName.TABLE);
            for(int i = 0; i < infolist.size(); i++) {
                String style = infolist.get(i).getAttributeValue("style");
                if (style != null && style.equals("color:#404040;")) {
                    List<Element> e = infolist.get(i).getAllElements(HTMLElementName.TD);
                    String title = e.get(0).getTextExtractor().toString();
                    String id = e.get(3).getTextExtractor().toString();
                    String msg = e.get(6).getTextExtractor().toString();
                    String point = e.get(9).getTextExtractor().toString();
                    String nae = e.get(12).getTextExtractor().toString();
                    info = new MyInfo(title, id, msg, point, nae);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return info;
    }
}
