package com.ldybob.ac3korea;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ldybob.ac3korea.parser.IPARSER;
import com.ldybob.ac3korea.parser.ParserFactory;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContentActivity extends AppCompatActivity implements ReplyBaseAdapter.RereplyListener {
    private final String TAG = "ContentActivity";

//    private final String MAIN_URL = "http://ac3korea.com";
    // TODO : 댓글이 많은 경우 OOM 발생 수정필요

    private TextView mTitle;
    private TextView mMember;
    private TextView mTime;
    private ListView listview;
    private ProgressBar mProgressbar;
    private EditText mReplyTxt;
    private Button mReplyadd;
    private Button mReplyfake;

    private Util mUtil;
    private String urlString;
    private ArrayList<ReplyItem> mList;
    private ReplyBaseAdapter mAdapter;
    private String boardID = ""; //현재 게시판 id
    private String uID = ""; // 현재 글 id
    private String rID = ""; // reply id
    private String mID = ""; // 작성자 id
    private String r_content;
    private String mClickedImageUri;
    private int mPage;
    private boolean isReply = false;
    private boolean myContent = false;
    private ReplyType rType = ReplyType.NEW;

    private getReplyTask replyTask;

    VideoEnabledWebView wb;
    VideoEnabledWebChromeClient webChromeClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "execute NavigationOnClickListener");
                finish();
            }
        });

        mUtil = new Util(this);
        String title = getIntent().getStringExtra("title");
        String name = getIntent().getStringExtra("name");
        mID = getIntent().getStringExtra("mid");
        SharedPreferences pref = this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String id = pref.getString("id", "");
        if (mID.equals(id)) {
            myContent = true;
        }
        String time = getIntent().getStringExtra("time");
        boardID = getIntent().getStringExtra("boardid");
        uID = getIntent().getStringExtra("uid");
        mPage = getIntent().getIntExtra("page", 1);
        //Log.d(TAG, title + ", " + name + ", " + boardID + ", " + uID);

        mProgressbar = (ProgressBar)findViewById(R.id.reply_progress);
        listview = (ListView)findViewById(R.id.reply_list);

        RelativeLayout nonVideoLayout = (RelativeLayout)findViewById(R.id.non_videolayout);
        RelativeLayout VideoLayout = (RelativeLayout)findViewById(R.id.videolayout);
        wb = new VideoEnabledWebView(this);
//        wb.setWebChromeClient(WBprogress);
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, VideoLayout, null, wb) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress)
            {
                mProgressbar.setProgress(progress);
                if (progress == 100) {
                    mProgressbar.setVisibility(View.GONE);
                }
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
                else
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        wb.setWebChromeClient(webChromeClient);
        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setSupportMultipleWindows(true);
//        wb = (WebView)findViewById(R.id.contentx);
//        wb.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        wb.getSettings().setLoadWithOverviewMode(true);
//        wb.getSettings().setUseWideViewPort(true);
        registerForContextMenu(wb);
        listview.addHeaderView(wb);
        mList = new ArrayList<ReplyItem>();
        mAdapter = new ReplyBaseAdapter(this, mList, this);
        listview.setAdapter(mAdapter);

        mTitle = (TextView)findViewById(R.id.view_title);
        mMember = (TextView)findViewById(R.id.view_member);
        mTime = (TextView)findViewById(R.id.view_time);
        mTitle.setText(Html.fromHtml(title));
        mMember.setText(name);
        if (time.indexOf(":") >= 0) {
            mTime.setText(time);
        } else {
            mTime.setText(FormatTime(time));
        }

        mReplyTxt = (EditText)findViewById(R.id.reply_input);
        mReplyadd = (Button)findViewById(R.id.reply_add);
        mReplyadd.setOnClickListener(onClickListener);
        mReplyfake = (Button)findViewById(R.id.reply_fake);
        mReplyfake.setOnClickListener(onClickListener);

        urlString = "https://www.ac3korea.com/ac3korea?table=" + boardID + "&query=view&uid=" + uID;

//        wb.loadData(str, "text/html", "euc-kr");
        getContentTask task = new getContentTask();
        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wb.onResume();
        //Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        wb.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult hitTestResult = wb.getHitTestResult();

        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE
                || hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            mClickedImageUri = hitTestResult.getExtra();
            getMenuInflater().inflate(R.menu.content_save_image, menu);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save_image) {
            mUtil.download(mClickedImageUri);
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (myContent) {
            getMenuInflater().inflate(R.menu.content_mine, menu);
        } else {
            getMenuInflater().inflate(R.menu.content, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            FragmentManager fm = getSupportFragmentManager();
            MyDialog dialog = new MyDialog(R.string.write_delete_msg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DeleteContentTask task = new DeleteContentTask();
                    task.execute();
                }
            });
            dialog.show(fm, "delete_content");
        } else if (id == R.id.action_scrab) {
            ScrabTask task = new ScrabTask();
            task.execute();
        }
        return super.onOptionsItemSelected(item);
    }

    public String FormatTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        format = new SimpleDateFormat("yyyy.MM.dd");
        return format.format(date);
    }

    private void reLoad() {
        isReply = false;
        rID = "";
        ChangeReplyLayout(false);
        mReplyTxt.setHint(R.string.reply_write_hint);
        if (mList != null) {
            mList.clear();
        } else {
            mList = new ArrayList<ReplyItem>();
        }
        mAdapter = new ReplyBaseAdapter(this, mList, this);
        listview.setAdapter(mAdapter);
        getContentTask task = new getContentTask();
        task.execute();
    }

    @Override
    public void onBackPressed() {
        if (!webChromeClient.onBackPressed())
        {
            if (wb.canGoBack())
            {
                wb.goBack();
            }
            else
            {
                if (isReply) {
                    rID = "";
                    ChangeReplyLayout(false);
                    mReplyTxt.setText("");
                    mReplyTxt.setHint(R.string.reply_write_hint);
                    return;
                }
                if (replyTask != null) {
                    replyTask.cancel(true);
                }
                super.onBackPressed();
            }
        }

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isReply) {
                rType = ReplyType.NEW;
                ChangeReplyLayout(true);
                isReply = true;
                return;
            }
            r_content = mReplyTxt.getText().toString();
            if (r_content.isEmpty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.reply_empty_content), Toast.LENGTH_SHORT).show();
                return;
            }
            writeReplyTask task = new writeReplyTask();
            task.execute(rType);
        }
    };

    public void ChangeReplyLayout(boolean isreply) {
        if (isreply) {
            mReplyadd.setVisibility(View.VISIBLE);
            mReplyfake.setVisibility(View.GONE);
            isReply = true;
            mReplyTxt.requestFocus();
            showKeyboard();
        } else {
            mReplyadd.setVisibility(View.GONE);
            mReplyfake.setVisibility(View.VISIBLE);
            isReply = false;
            hideKeyboard();
        }
    }

    WebChromeClient WBprogress = new WebChromeClient() {
        public void onProgressChanged(WebView view, int newProgress) {
            //Log.d(TAG, "progress = " + newProgress);
            mProgressbar.setProgress(newProgress);
            if (newProgress == 100) {
                mProgressbar.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void CallRereply(int pos) {
        //Log.d(TAG, "click reply " + pos);
        // 클릭한 아이템의 reply id 저장
        rID = mAdapter.getItem(pos).getReplyid();
        showPopupMenu(pos);
    }

    private void showPopupMenu(final int pos) {
        PopupMenu popup = new PopupMenu(this, getViewByPosition(pos + 1,listview).findViewById(R.id.rereply_btn));
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String id = pref.getString("id", "");
        PopupChoice(id, pos, popup);
//        if (boardID.equals(BoardID.QNA)) {
//            if (myContent) { // 내가 쓴 글인경우
//                if (id.equals(mList.get(pos).getMember_id())) {
//                    // 자기 댓글인 경우 팝업
//                    popup.getMenuInflater().inflate(R.menu.reply_popup_mine, popup.getMenu());
//                } else {
//                    // 타인 댓글인 경우 팝업
//                    if (mList.get(pos).getAnswer().equals("답변 채택")) { // answer grade 1
//                        popup.getMenuInflater().inflate(R.menu.reply_popup_answer2, popup.getMenu());
//                    } else if (mList.get(pos).getAnswer().equals("답변 채택 + 감사 내공")) {
//                        popup.getMenuInflater().inflate(R.menu.reply_popup, popup.getMenu());
//                    } else {
//                        popup.getMenuInflater().inflate(R.menu.reply_popup_answer1, popup.getMenu());
//                    }
//                }
//            } else { // 남이 쓴 글인경우
//                if (id.equals(mList.get(pos).getMember_id())) {
//                    // 자기 댓글인 경우 팝업
//                    popup.getMenuInflater().inflate(R.menu.reply_popup_mine, popup.getMenu());
//                } else {
//                    // 타인 댓글인 경우 팝업
//                    popup.getMenuInflater().inflate(R.menu.reply_popup, popup.getMenu());
//                }
//            }
//        } else {
//            if (id.equals(mList.get(pos).getMember_id())) {
//                // 자기 댓글인 경우 팝업
//                popup.getMenuInflater().inflate(R.menu.reply_popup_mine, popup.getMenu());
//            } else {
//                // 타인 댓글인 경우 팝업
//                popup.getMenuInflater().inflate(R.menu.reply_popup, popup.getMenu());
//            }
//        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_rereply) {
                    rType = ReplyType.REREPLY;
                    ChangeReplyLayout(true);
                    mReplyTxt.setHint(getString(R.string.reply_write_hint2, mAdapter.getItem(pos).getMember_name()));
                } else if (id == R.id.action_modify) {
                    rType = ReplyType.MODIFY;
                    mReplyTxt.setText(mList.get(pos).getContent().replaceAll("\nⓜ", ""));
                    ChangeReplyLayout(true);
                } else if (id == R.id.action_delete) {
                    FragmentManager fm = getSupportFragmentManager();
                    MyDialog dialog = new MyDialog(R.string.write_delete_msg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteReplyTask task = new deleteReplyTask();
                            task.execute();
                        }
                    });
                    dialog.show(fm, "delete_reply");
                } else if (id == R.id.action_answer1) {
                    FragmentManager fm = getSupportFragmentManager();
                    MyDialog dialog = new MyDialog(R.string.reply_answer_msg1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ReplyAnswerTask task = new ReplyAnswerTask();
                            task.execute(1);
                        }
                    });
                    dialog.show(fm, "reply_answer1");
                } else if (id == R.id.action_answer2) {
                    FragmentManager fm = getSupportFragmentManager();
                    MyDialog dialog = new MyDialog(R.string.reply_answer_msg2, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ReplyAnswerTask task = new ReplyAnswerTask();
                            task.execute(2);
                        }
                    });
                    dialog.show(fm, "reply_answer2");
                }
                return false;
            }
        });
        popup.show();
    }

    public void PopupChoice(String myid, int pos, PopupMenu popup) {
        if (boardID.equals(BoardID.QNA)) {
            if (myContent) { // 내가 쓴 글인경우
                if (myid.equals(mList.get(pos).getMember_id())) {
                    // 자기 댓글인 경우 팝업
                    popup.getMenuInflater().inflate(R.menu.reply_popup_mine, popup.getMenu());
                } else {
                    // 타인 댓글인 경우 팝업
                    if (mList.get(pos).getAnswer().equals("답변 채택")) { // answer grade 1
                        popup.getMenuInflater().inflate(R.menu.reply_popup_answer2, popup.getMenu());
                    } else if (mList.get(pos).getAnswer().equals("답변 채택 + 감사 내공")) {
                        popup.getMenuInflater().inflate(R.menu.reply_popup, popup.getMenu());
                    } else {
                        popup.getMenuInflater().inflate(R.menu.reply_popup_answer1, popup.getMenu());
                    }
                }
            } else { // 남이 쓴 글인경우
                if (myid.equals(mList.get(pos).getMember_id())) {
                    // 자기 댓글인 경우 팝업
                    popup.getMenuInflater().inflate(R.menu.reply_popup_mine, popup.getMenu());
                } else {
                    // 타인 댓글인 경우 팝업
                    popup.getMenuInflater().inflate(R.menu.reply_popup, popup.getMenu());
                }
            }
        } else {
            if (myid.equals(mList.get(pos).getMember_id())) {
                // 자기 댓글인 경우 팝업
                popup.getMenuInflater().inflate(R.menu.reply_popup_mine, popup.getMenu());
            } else {
                // 타인 댓글인 경우 팝업
                popup.getMenuInflater().inflate(R.menu.reply_popup, popup.getMenu());
            }
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private class writeReplyTask extends AsyncTask<ReplyType, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ContentActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.write_dlg_msg));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(ReplyType... params) {
            mUtil.WriteReply(r_content, boardID, uID, rID, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            mReplyTxt.setText("");
            reLoad();
            super.onPostExecute(aVoid);
        }
    }

    private class deleteReplyTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ContentActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.write_dlg_msg));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mUtil.DeleteReply(boardID, uID, rID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            mReplyTxt.setText("");
            reLoad();
            super.onPostExecute(aVoid);
        }
    }

    private class getContentTask extends AsyncTask<Void, Void, Element[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.d(TAG, "onPreExecute");
        }

        @Override
        protected Element[] doInBackground(Void... params) {
            //Log.d(TAG, "doInBackground");
            try {
                HttpPost httpPost = new HttpPost(urlString);
                if (Repo.getInstance(getApplicationContext()).getHttpClient() == null) {
                    mUtil.reLogin();
                }
                HttpResponse response = Repo.getInstance(getApplicationContext()).getHttpClient().execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
                Source HTMLSource = new Source(responseString);
                Element eContent = HTMLSource.getElementById("ContentsLayer" + uID);
                Element eComment = HTMLSource.getElementById("Comment_Layer");
                Element[] eArray = {eContent, eComment};
                return eArray;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Element[] e) {
            super.onPostExecute(e);
            //Log.d(TAG, "onPostExecute");
            if (e[0] != null) {
                wb.post(new Runnable() {
                    @Override
                    public void run() {
//                        wb.loadData("<iframe max-width=\"100%\" width=\"auto\" height=\"auto\" src=\"https://www.youtube.com/embed/VjcDHVg-u-c\" frameborder=\"0\" allowfullscreen>",  "text/html; charset=utf-8", "utf-8");
                        String html = getHtmlData(e[0].toString());
                        if (html.indexOf("www.youtube.com/v/") >= 0) {
                            html = ChangeYouTube(e[0]);
                        }
                        wb.loadData(html, "text/html; charset=utf-8", "utf-8");
                    }
                });

            }
            replyTask = new getReplyTask();
            replyTask.execute(e[1]);
        }

        private String ChangeYouTube(Element e) {
            Source source = new Source(e.toString().replaceAll("</embed>", ""));
            OutputDocument doc = new OutputDocument(source);
//            List<Element> elements = source.getAllElements(HTMLElementName.OBJECT);
//            for (Element el : elements) {
//                doc.remove(el.getStartTag());
////                if (!el.getStartTag().isSyntacticalEmptyElementTag()) {
////                    doc.remove(el.getEndTag());
////                }
//            }
//            elements = source.getAllElements(HTMLElementName.PARAM);
//            for (Element el : elements) {
//                doc.remove(el.getStartTag());
////                if (!el.getStartTag().isSyntacticalEmptyElementTag()) {
////                    doc.remove(el.getEndTag());
////                }
//            }
            List<Element> elements = source.getAllElements(HTMLElementName.EMBED);
            String youtubeID;
            for (Element el : elements) {
                youtubeID = "";
                String src = el.getAttributeValue("src");
                if(src.indexOf("www.youtube.com/v/") >= 0) {
                    int start = src.indexOf("/v/");
                    youtubeID = src.substring(start + 3, start + 14);
                    doc.replace(el, "<iframe max-width=\"100%\" width=\"auto\" height=\"auto\" src=\"https://www.youtube.com/embed/" + youtubeID + "\" frameborder=\"0\" allowfullscreen></iframe>");
//                    if (!el.getStartTag().isSyntacticalEmptyElementTag()) {
//                        doc.remove(el.getEndTag());
//                    }
                }
            }
            return doc.toString();
        }
    }

    private class getReplyTask extends AsyncTask<Element, Void, IPARSER> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.d(TAG, "onPreExecute1");
        }

        @Override
        protected IPARSER doInBackground(Element... params) {
            //Log.d(TAG, "doInBackground1 " + params[0]);
            if (params == null) {
                cancel(true);
                finishAffinity();
                return null;
            } else {
                List<Element> EList = params[0].getAllElements(HTMLElementName.SPAN);
                IPARSER parser = ParserFactory.getParser(boardID);
                parser.ParsingReply(getApplicationContext(), EList, this);
//            if (parser.ParsingReply(getApplicationContext(), EList)) {
//                for (ReplyItem item : parser.getReplyList()) {
//                    mList.add(item);
//                }
//            }
                return parser;
            }
        }

        @Override
        protected void onPostExecute(IPARSER parser) {
            super.onPostExecute(parser);
            for (ReplyItem item : parser.getReplyList()) {
                mList.add(item);
            }
            mAdapter.notifyDataSetChanged();
            //Log.d(TAG, "onPostExecute1");
        }
    }

    private class DeleteContentTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ContentActivity.this);
        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.write_dlg_msg));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mUtil.DeleteContent(boardID, uID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.setClass(ContentActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("bbs", boardID);
            startActivity(intent);
            super.onPostExecute(aVoid);
        }
    }

    private class ScrabTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mUtil.Scrab(boardID, uID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplicationContext(), R.string.content_scrab_complete, Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }
    }

    private class ReplyAnswerTask extends AsyncTask<Integer, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ContentActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.write_dlg_msg));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            mUtil.ReplyAnswer(boardID, uID, rID, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            reLoad();
            super.onPostExecute(aVoid);
        }
    }

    public String getHtmlData(String bodyHTML) {

        String head = "<head>" +
                "<style type=\"text/css\">img{max-width: 100%; width:auto; height: auto; !important;}" +
//                "div{max-width: 100%; width:auto; height: auto; !important;}" +
                "embed{max-width: 100%; width:auto; height: auto; !important;}" +
                "iframe{max-width: 100%; width:auto; height: auto; !important;}" +
                "object{max-width: 100%; width:auto; height: auto; !important;}</style>" +
                "<script type=\"text/javascript\">" +
                "function removeStyle() {" +
                // + 이미지 태그 내 속성 제거
                "var l = document.getElementsByTagName(\"IMG\");" +
                "for(var i = 0; i < l.length; i++) {" +
                "l[i].removeAttribute(\"style\");" +
                "l[i].removeAttribute(\"onload\");" +
                "l[i].removeAttribute(\"onmouseover\");" +
                "}" +
                // - img 태그 내 속성 제거

                // + div 태그 내 속성 제거
                "l = document.getElementsByTagName(\"div\");" +
                "for(var i = 0; i < l.length; i++) {" +
                "l[i].removeAttribute(\"style\");" +
                "}" +
                //- div 태그 내 속성 제거

                // + 스크린샷/만화 란에 이미지 첨부한 경우 url이 일부만 표시되어 변경
                "var y = document.getElementsByTagName(\"img\");" +
                "for(var i = 0; i < y.length; i++) {" +
                "var z = y[i].getAttribute(\"src\");" +
                "if ((z.indexOf(\"bbs/table/shaphwa/upload/\") >= 0)" +
                "|| z.indexOf(\"bbs/table/screenshot/upload/\") >= 0) {" +
                "y[i].setAttribute(\"src\", \"https://www.ac3korea.com/\" + z);" +
                "}" +
                "}" +
                // - 스크린샷/만화 란에 이미지 첨부한 경우 url이 일부만 표시되어 변경
                "}" +
                "</script>" +
                "</head>";
        return "<html>" + head + "<body onload=removeStyle()>" + bodyHTML + "<br><br></body></html>";
    }

    @Override
    protected void onDestroy() {
        //Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mReplyTxt, 0);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mReplyTxt.getWindowToken(), 0);
    }
}
