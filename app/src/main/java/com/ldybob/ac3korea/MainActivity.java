package com.ldybob.ac3korea;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.ldybob.ac3korea.parser.IPARSER;
import com.ldybob.ac3korea.parser.ParserFactory;
import com.ldybob.ac3korea.parser.impl.MyInfoParser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "MainActivity";

//    private final String MAIN_URL = "http://www.ac3korea.com";
//    private final String BOARD_URL = MAIN_URL + "/ac3korea?table=";

    private mySwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView; // 파싱한 글 목록 표시하기위한 ListView
    private ListBaseAdapter adapter;
    private TextView header_title;
    private TextView header_id;
    private TextView header_msg;
    private TextView header_point;
    private TextView header_nae;
    private EditText mSearchEdit;
    private Button mCategoryBtn;
    private MenuItem mSearchAction;
    private PopupMenu mSearchPopup;

    private Context mContext;
    //    private Util mUtil;
    private ArrayList<ListItem> mBBSList;
    private int mPageNo = 1;
    private String mCurrentBoard = BoardID.FREE;
    private String mCurrentSearch = SearchCategory.TITLE;
    private String mSearchText = "";
    private boolean isSearchOpened = false;

    private ParsingListTask task;

    private boolean mExit = false;
    private int EXIT_TIME = 1;

    private Handler exitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == EXIT_TIME) {
                mExit = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCurrentBoard = getIntent().getExtras().getString("bbs", BoardID.FREE);
        setSupportActionBar(toolbar);
        setTitle();

        // 글 작성 Activity 로 이동시키는 floating button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WriteActivity.class);
                intent.putExtra("bbs", mCurrentBoard);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (isSearchOpened) {
                    hideKeyboard();
                    handleMenuSearch();
                }
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header = navigationView.getHeaderView(0);
        header_title = (TextView)nav_header.findViewById(R.id.header_title);
        header_id = (TextView)nav_header.findViewById(R.id.nav_header_id);
        header_msg = (TextView)nav_header.findViewById(R.id.nav_header_msg);
        header_point = (TextView)nav_header.findViewById(R.id.nav_header_point);
        header_nae = (TextView)nav_header.findViewById(R.id.nav_header_nae);

        mSwipeRefreshLayout = (mySwipeRefreshLayout)findViewById(R.id.swipe_layout);
//        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        mSwipeRefreshLayout.setOnLoadListener(onLoadListener);
        mListView = (ListView)findViewById(R.id.bbs_listview);
        mListView.setOnItemClickListener(onClickListener);

        mContext = this;
//        mUtil = new Util(mContext);

        ParsingMyInfoTask task = new ParsingMyInfoTask();
        task.execute();

        ContentsLoad();
    }

    /**
     * 게시판 ID에 맞는 타이틀 설정
     */
    private void setTitle() {
        int strID = R.string.nav_free;
        if (mCurrentBoard.equals(BoardID.NOTICE)) {
            strID = R.string.nav_notice;
        } else if (mCurrentBoard.equals(BoardID.FREE)) {
            strID = R.string.nav_free;
        } else if (mCurrentBoard.equals(BoardID.QNA)) {
            strID = R.string.nav_qna;
        } else if (mCurrentBoard.equals(BoardID.NEWS)) {
            strID = R.string.nav_news;
        } else if (mCurrentBoard.equals(BoardID.COMIC)) {
            strID = R.string.nav_comic;
        } else if (mCurrentBoard.equals(BoardID.SCREENSHOT)) {
            strID = R.string.nav_screenshot;
        } else if (mCurrentBoard.equals(BoardID.KOR_RELEASE)) {
            strID = R.string.nav_kor_release;
        } else if (mCurrentBoard.equals(BoardID.OTHER_RELEASE)) {
            strID = R.string.nav_other_release;
        } else if (mCurrentBoard.equals(BoardID.TV_RELEASE)) {
            strID = R.string.nav_tv_release;
        } else if (mCurrentBoard.equals(BoardID.SCRAB)) {
            strID = R.string.nav_scrab;
        }
        getSupportActionBar().setTitle(strID);
    }

    private void Init() {
        mPageNo = 1;
        mSearchText = "";
        mCurrentSearch = SearchCategory.TITLE;
        if (mBBSList != null) {
            mBBSList.clear();
        } else {
            mBBSList = new ArrayList<ListItem>();
        }
        adapter = new ListBaseAdapter(mContext, mBBSList);
        mListView.setAdapter(adapter);
    }

    /**
     * 현재 게시판의 글 목록 load
     */
    private void ContentsLoad() {
        mSwipeRefreshLayout.ProgressViewInit();
        Init();
        ParsingList();
    }

    /**
     * 게시판의 글 목록에서 검색사용 시
     */
    private void doSearch() {
        mSwipeRefreshLayout.ProgressViewInit();
        mPageNo = 1;
        if (mBBSList != null) {
            mBBSList.clear();
        } else {
            mBBSList = new ArrayList<ListItem>();
        }
        adapter = new ListBaseAdapter(mContext, mBBSList);
        mListView.setAdapter(adapter);
        ParsingList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
    }

    /**
     * 리스트 최상단 도달 시 목록 새로고침
     */
    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            ContentsLoad();
        }
    };

    AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListItem item = adapter.getItem(position);
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ContentActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("name", item.getMember_name());
            intent.putExtra("mid", item.getMember_id());
            intent.putExtra("time", item.getTime());
            intent.putExtra("boardid", mCurrentBoard);
            intent.putExtra("uid", item.getUID());
            intent.putExtra("page", mPageNo);
            startActivity(intent);
            // 검색 레이아웃이 보이는 상태에서 리스트 클릭 시 레이아웃 지우도록
            closeSearchLayout();
        }
    };

    /**
     * 리스트 최하단에 도달 시 다음 리스트 로딩
     */
    mySwipeRefreshLayout.OnLoadListener onLoadListener = new mySwipeRefreshLayout.OnLoadListener() {
        @Override
        public void onLoad() {
            //Log.d(TAG, "onLoad");
            ParsingList();
        }
    };

    /**
     * 파싱 태스크 실행
     */
    public void ParsingList() {
        mSwipeRefreshLayout.refreshing(true);
        task = new ParsingListTask();
        task.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(isSearchOpened) {
                handleMenuSearch();
                return;
            }
            if (task != null) {
                task.cancel(true);
            }
            if (mExit) {
                super.onBackPressed();
                exitHandler.removeCallbacksAndMessages(null);
            } else {
                mExit = true;
                exitHandler.sendEmptyMessageDelayed(EXIT_TIME, 2000);
                Toast.makeText(mContext, R.string.exit_message, Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("auto", false);
            editor.commit();
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_hide) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, BlackmanActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_search) {
            handleMenuSearch();
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleMenuSearch(){
        if(isSearchOpened){
            closeSearchLayout();
        } else {
            showSearchLayout();
        }
    }

    /**
     * 검색 layout 비활성화
     */
    public void closeSearchLayout() {
        ActionBar action = getSupportActionBar();
        hideKeyboard();

        action.setDisplayShowCustomEnabled(false);
        action.setDisplayShowTitleEnabled(true);

        mSearchAction.setIcon(getResources().getDrawable(R.mipmap.ic_search_white_48dp));

        isSearchOpened = false;
    }

    /**
     * 검색 layout 활성화
     */
    public void showSearchLayout() {
        ActionBar action = getSupportActionBar();
        action.setDisplayShowCustomEnabled(true);
        // custom view in the action bar.
        action.setCustomView(R.layout.search_layout);
        action.setDisplayShowTitleEnabled(false);
        mCategoryBtn = (Button) action.getCustomView().findViewById(R.id.search_category);
        mCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchPopup();
            }
        });
        mSearchEdit = (EditText) action.getCustomView().findViewById(R.id.search_edit);

        mSearchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (mSearchEdit.getText().toString().isEmpty()) {
                        Toast.makeText(mContext, R.string.search_text_empty, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    mSearchText = mSearchEdit.getText().toString();
                    doSearch();
                    handleMenuSearch();
                    return true;
                }
                return false;
            }
        });

        mSearchEdit.requestFocus();

        showKeyboard();

        mSearchAction.setIcon(getResources().getDrawable(R.mipmap.ic_close_white_48dp));

        isSearchOpened = true;
    }

    /**
     * 검색 카테고리 팝업 표시
     */
    public void showSearchPopup() {
        mSearchPopup = new PopupMenu(this, mCategoryBtn);
        mSearchPopup.getMenuInflater().inflate(R.menu.search_menu, mSearchPopup.getMenu());
        mSearchPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_search_title) {
                    setCategory(R.string.search_title, SearchCategory.TITLE);
                } else if (id == R.id.action_search_content) {
                    setCategory(R.string.search_content, SearchCategory.CONTENT);
                } else if (id == R.id.action_search_reply) {
                    setCategory(R.string.search_reply, SearchCategory.REPLY);
                } else if (id == R.id.action_search_reply_writer) {
                    setCategory(R.string.search_reply_writer, SearchCategory.REPLY_WRITER);
                } else if (id == R.id.action_search_writer) {
                    setCategory(R.string.search_writer, SearchCategory.WRITER);
                } else if (id == R.id.action_search_id) {
                    setCategory(R.string.search_id, SearchCategory.ID);
                }
                return false;
            }
        });
        mSearchPopup.show();
    }

    private void setCategory(int string, String category) {
        mCategoryBtn.setText(string);
        mCurrentSearch = category;
    }

    private void showKeyboard() {
        if (mSearchEdit != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mSearchEdit, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        if (mSearchEdit != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchEdit.getWindowToken(), 0);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id) {
            case R.id.nav_recent:
//                Toast.makeText(mContext, "나의 최근 글", Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, getString(R.string.preparing), Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_scrab:
                Toast.makeText(mContext, getString(R.string.preparing), Toast.LENGTH_SHORT).show();
                // TODO: 스크랩 기능 추가 시 아래 주석 풀어야 함.
//                navSelect(BoardID.SCRAB);
                break;
            case R.id.nav_notice:
//                navSelect(BoardID.NOTICE);
                Toast.makeText(mContext, getString(R.string.preparing), Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_free:
                navSelect(BoardID.FREE);
                break;
            case R.id.nav_qna:
                navSelect(BoardID.QNA);
                break;
            case R.id.nav_news:
                navSelect(BoardID.NEWS);
                break;
            case R.id.nav_comic:
                navSelect(BoardID.COMIC);
                break;
            case R.id.nav_screenshot:
                navSelect(BoardID.SCREENSHOT);
//                Toast.makeText(mContext, getString(R.string.preparing), Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_kor_release:
//                navSelect(BoardID.KOR_RELEASE);
                Toast.makeText(mContext, getString(R.string.preparing), Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_other_release:
//                navSelect(BoardID.OTHER_RELEASE);
                Toast.makeText(mContext, getString(R.string.preparing), Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_tv_release:
//                navSelect(BoardID.TV_RELEASE);
                Toast.makeText(mContext, getString(R.string.preparing), Toast.LENGTH_SHORT).show();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navSelect(String board_id) {
        mCurrentBoard = board_id;
        setTitle();
        ContentsLoad();
    }

    /**
     * 로그인 한 계정 사용자 정보 파싱
     */
    private class ParsingMyInfoTask extends AsyncTask<Void, Void, MyInfo> {

        @Override
        protected MyInfo doInBackground(Void... params) {
            MyInfoParser parser = new MyInfoParser(getApplicationContext());
            return parser.getMyInfo();
        }

        @Override
        protected void onPostExecute(MyInfo info) {
            header_title.setText(info.getTitle());
            header_id.setText(info.getId());
            header_msg.setText(info.getMsg());
            header_point.setText(info.getPoint());
            header_nae.setText(info.getNae());
        }
    }

    /**
     * 게시판 글 목록을 파싱
     */
    private class ParsingListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            IPARSER parser = ParserFactory.getParser(mCurrentBoard);
            if (parser.ParsingList(getApplicationContext(), mCurrentBoard, mPageNo, mCurrentSearch, mSearchText, this)) {
                mPageNo++;
                for (ListItem item : parser.getBoardList()) {
                    mBBSList.add(item);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            mSwipeRefreshLayout.refreshing(false);
            mSwipeRefreshLayout.setLoading(false);
        }
    }
}
