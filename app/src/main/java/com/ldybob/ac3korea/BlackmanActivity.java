package com.ldybob.ac3korea;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class BlackmanActivity extends AppCompatActivity {
    private final String TAG = "BlackmanActivity";
    private String filename = "user_list";

    private EditText edittext;
    private Button btn_add;
    private Button btn_remove;

    private ListView listview;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black);
        Toolbar toolbar = (Toolbar) findViewById(R.id.view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.action_hide);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "execute NavigationOnClickListener");
                finish();
            }
        });

        listview = (ListView)findViewById(R.id.hide_listview);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);
        edittext = (EditText)findViewById(R.id.input_id);
        btn_add = (Button)findViewById(R.id.btn_add);
        btn_remove = (Button)findViewById(R.id.btn_remove);
        btn_add.setOnClickListener(onClickListener);
        btn_remove.setOnClickListener(onClickListener);

        File file = new File(getFilesDir(), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setAdapter();
    }

    public void setAdapter() {
        adapter.clear();
        String list = Read();
        String[] lists = list.split(";");
        for (int i = 0; i < lists.length; i++) {
            if (lists != null && lists.length >0 && !lists[i].isEmpty())
                adapter.add(lists[i]);
        }
        listview.setAdapter(adapter);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btn_add) {
                if (edittext.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_edit_text), Toast.LENGTH_SHORT).show();
                } else {
                    Write(edittext.getText().toString(), false);
                    //Log.d(TAG, Read());
                }
            } else if (v == btn_remove) {
                SparseBooleanArray sba = listview.getCheckedItemPositions();
                for (int i = 0; i < listview.getCount(); i++) {
                    if(sba.size() > 0 && sba.get(i)) {
                        Write(adapter.getItem(i), true);
//                        Log.d(TAG, "" + adapter.getItem(i));
                    }
                }
                //Log.d(TAG, Read());
            }
            setAdapter();
            Repo.getInstance(getApplicationContext()).setBlackList(Read());
            edittext.setText("");
        }
    };

    public void Write(String id, boolean isRemove) {
        String ID = id + ";";
        if (isRemove) {
            ID = Read().replaceAll(ID, "");
        } else {
            String string = Read();
            if (string.indexOf(ID) >= 0) {
                return;
            } else {
                ID = string + ID;
            }
        }
        try {
            FileOutputStream fout = openFileOutput(filename, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fout));
            writer.append(ID);
//            fout.write(ID.getBytes());
            writer.flush();
            writer.close();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String Read() {
        String readString = "";
        try {
            FileInputStream fIn = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fIn));
            readString = reader.readLine();
            reader.close();
//            InputStreamReader isr = new InputStreamReader(fIn);
//            char[] inputBuffer = new char[fIn.available()];
//            isr.read(inputBuffer);
//            readString = new String(inputBuffer);
            fIn.close();
//            isr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readString == null ? "" : readString;
    }
}
