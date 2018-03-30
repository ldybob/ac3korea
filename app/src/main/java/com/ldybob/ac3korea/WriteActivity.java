package com.ldybob.ac3korea;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 새 글 작성 시 사용되는 Activity
 */
public class WriteActivity extends AppCompatActivity {
    private final String TAG = "WriteActivity";

    final int REQ_CODE_SELECT_IMAGE=100;

    Button mWriteBtn; // 글 작성 버튼
    Button mAttachBtn; // 파일 첨부 버튼
    EditText mTitle; // 글 제목 EditText
    EditText mContent; // 글 본문 EditText
    TextView mFileName; // 첨부파일 이름표시하기 위한 TextView

    private Util mUtil;
    private String bbsID;
    private String upFileName = "";
    private String upFilePath = "";
    private String uploadedUri = "";
    private boolean nowUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        bbsID = getIntent().getStringExtra("bbs");

        Toolbar toolbar = (Toolbar) findViewById(R.id.write_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "execute NavigationOnClickListener");
                finish();
            }
        });

        mWriteBtn = (Button)findViewById(R.id.write_btn);
        mAttachBtn = (Button)findViewById(R.id.fileupload_btn);
        mTitle = (EditText)findViewById(R.id.title_txt);
        mContent = (EditText)findViewById(R.id.content_txt);
        mFileName = (TextView)findViewById(R.id.filename);

        mUtil = new Util(this);

        mWriteBtn.setOnClickListener(onClickListener);
        mAttachBtn.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (nowUploading) {
                Toast.makeText(getApplicationContext(), getString(R.string.write_file_uploading), Toast.LENGTH_SHORT).show();
                return;
            }
            if (v == mWriteBtn) {
                String title = mTitle.getText().toString();
                String content = mContent.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.write_empty_title), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (content.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.write_empty_content), Toast.LENGTH_SHORT).show();
                    return;
                }
                WriteTask task = new WriteTask();
                task.execute(title, content);
            } else if(v == mAttachBtn) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
//                Log.d(TAG, getImageNameToUri(data.getData()));
                upFileName = getImageNameToUri(data.getData());
                mFileName.setText(upFileName);
                uploadFile task = new uploadFile();
                task.execute();
            }
        }
    }

    public String getImageNameToUri(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        upFilePath = cursor.getString(column_index);
        String imgName = upFilePath.substring(upFilePath.lastIndexOf("/")+1);
//        Log.d(TAG, "data = " + data);
//        Log.d(TAG, "datapath = " + upFilePath);

        return imgName;
    }

    public class WriteTask extends AsyncTask<String, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(WriteActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.write_dlg_msg));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            mUtil.Write(params[0], params[1], bbsID, uploadedUri);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.setClass(WriteActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("bbs", bbsID);
            startActivity(intent);
            super.onPostExecute(aVoid);
        }
    }

    public class uploadFile extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            nowUploading = true;
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
//            mUtil.uploadFile(upFilePath, bbsID);
            uploadedUri = mUtil.uploadFile(upFilePath);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            nowUploading = false;
            super.onPostExecute(aVoid);
        }
    }
}
