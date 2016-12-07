package com.ldybob.ac3korea;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Util {
    private final String TAG = "Util";
    private Context mContext;
    private AES256Util aes256;
    private final String FILENAME = "user_list";

    public Util(Context context){
        mContext = context;
        String key = "aes256-test-key!!";
        try {
            aes256 = new AES256Util(key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public boolean Login(String id, String pwd) {

        String urlString = Const.http + "www.ac3korea.com/lib/login";
        String responseString = "";
        try {
            HttpPost httpPost = new HttpPost(urlString);
            httpPost.setHeader("referer", Const.http + "www.ac3korea.com");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("MB_ID", id));
            nameValuePairs.add(new BasicNameValuePair("MB_PW", pwd));
            nameValuePairs.add(new BasicNameValuePair("x", "0"));
            nameValuePairs.add(new BasicNameValuePair("y", "0"));
            nameValuePairs.add(new BasicNameValuePair("pwd", Const.http + "ac3korea.com"));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));

            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
                reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, responseString);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if((responseString.indexOf("아이디나 비밀번호 입력이 잘못 되었습니다.") != -1)
                || (responseString.indexOf("location.href='" + Const.http + "ac3korea.com';") == -1
                || (responseString.indexOf("alert('탈퇴 회원입니다. 일주일이 지난 후 재가입 가능합니다") != -1))) {
            return false; // 로그인실패
        } else if(responseString.indexOf("location.href='" + Const.http + "ac3korea.com';") > -1) {
            return true; // 로그인성공
        } else {
            return false;
        }
    }

    public void reLogin() {
        SharedPreferences pref = mContext.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String id = pref.getString("id", "");
        String pw = decrypt(pref.getString("pw", ""));
        Login(id, pw);
    }

    public boolean NetworkCheck() {
        ConnectivityManager cManager;
        NetworkInfo mobile;
        NetworkInfo wifi;

        cManager=(ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mobile = cManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        wifi = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(mobile.isConnected() || wifi.isConnected()) {
            return true; //3G/LTE 또는 WiFi 에 연결되어 있을 경우
        } else {
            return false; //3G/LTE 또는 WiFi 에 연결되어있지 않은 경우
        }
    }

    public Bitmap LoadImageFromWebOperations(String url) {
        try {
            URL uurl = new URL(url);
            InputStream is = uurl.openConnection().getInputStream();
            Bitmap bitMap = BitmapFactory.decodeStream(is);
            is.close();
            return bitMap;
        } catch (Exception e) {
            return null;
        }
    }

    public String RoadHideList() {
        String readString = "";
        try {
            FileInputStream fIn = mContext.openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[fIn.available()];
            isr.read(inputBuffer);
            readString = new String(inputBuffer);
            fIn.close();
            isr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readString;
    }

    public boolean Write(String title, String content, String bbsID, String uploadedUri) {
        String Content = strHelper.bbsContent(content) + "<br><br>ⓜ";
        String bb_content;
        if (uploadedUri.isEmpty()) {
            bb_content = Content;
        } else {
            bb_content = "<P><IMG src=\"" + uploadedUri + "\" align=bottom><br><br>" + Content + "</P>";
        }
        String responseString = "";
        SharedPreferences pref = mContext.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String id = pref.getString("id", "");
        String pw = decrypt(pref.getString("pw", ""));
        HttpPost httpPost = new HttpPost(Const.http + "www.ac3korea.com/bbs.php");
        httpPost.setHeader("referer", Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&query=write");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("action", "write_data"));
        nameValuePairs.add(new BasicNameValuePair("mbid", id));
        nameValuePairs.add(new BasicNameValuePair("isRoot", "0"));
        nameValuePairs.add(new BasicNameValuePair("table", bbsID));
        nameValuePairs.add(new BasicNameValuePair("write_type", ""));
        nameValuePairs.add(new BasicNameValuePair("uid", ""));
        nameValuePairs.add(new BasicNameValuePair("p", ""));
        nameValuePairs.add(new BasicNameValuePair("useup", "1"));
        nameValuePairs.add(new BasicNameValuePair("uselk", "1"));
        nameValuePairs.add(new BasicNameValuePair("Nparam", ""));
        nameValuePairs.add(new BasicNameValuePair("BB_NAME", ""));
        nameValuePairs.add(new BasicNameValuePair("BB_EMAIL", ""));
        nameValuePairs.add(new BasicNameValuePair("BB_CATEGORY", ""));
        nameValuePairs.add(new BasicNameValuePair("is_category", ""));
        nameValuePairs.add(new BasicNameValuePair("BB_HOME_URL", Const.http));
        nameValuePairs.add(new BasicNameValuePair("BB_PREVIEW", id));
        nameValuePairs.add(new BasicNameValuePair("BB_HTML", "HTML"));
        nameValuePairs.add(new BasicNameValuePair("BB_FILE", ""));
        nameValuePairs.add(new BasicNameValuePair("BB_PASS", pw));
        nameValuePairs.add(new BasicNameValuePair("x", "0"));
        nameValuePairs.add(new BasicNameValuePair("y", "0"));
        nameValuePairs.add(new BasicNameValuePair("BB_SUBJECT", title));
        nameValuePairs.add(new BasicNameValuePair("BB_CONTENT", bb_content));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));
            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
                reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, "write response = " + responseString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseString.indexOf("alert('정상적인 접근이 아닙니다") >= 0) {
            return false;
        }
        return true;
    }

    public boolean WriteReply(String content, String bbsID, String uID, String rID, ReplyType type) {
        String Content = content + "\nⓜ";
        String responseString = "";

        HttpPost httpPost = new HttpPost(Const.http + "www.ac3korea.com/ac3korea");
        httpPost.setHeader("referer", Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&query=view&p=1&uid=" + uID);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("table", bbsID));
        if (type == ReplyType.NEW) {
            nameValuePairs.add(new BasicNameValuePair("action", type.toString()));
        } else if (type == ReplyType.MODIFY) {
            nameValuePairs.add(new BasicNameValuePair("action", type.toString()));
        } else if (type == ReplyType.REREPLY) {
            nameValuePairs.add(new BasicNameValuePair("action", type.toString()));
        }
//        nameValuePairs.add(new BasicNameValuePair("action", rID.isEmpty() ? "comment_regis" : "creply"));
        nameValuePairs.add(new BasicNameValuePair("useup", "1"));
        nameValuePairs.add(new BasicNameValuePair("uselink", "1"));
        nameValuePairs.add(new BasicNameValuePair("write_perm", "1"));
        nameValuePairs.add(new BasicNameValuePair("RP_PARENT", uID));
        nameValuePairs.add(new BasicNameValuePair("RP_UID", rID)); // reply id 를 넣으면 댓댓글 작성
        nameValuePairs.add(new BasicNameValuePair("RP_EMOTION", "0"));
        nameValuePairs.add(new BasicNameValuePair("Nparam", "./ac3korea?table=" + bbsID + "&query=view&uid=" + uID + "&p=1"));
        nameValuePairs.add(new BasicNameValuePair("page", "" + "1"));
        nameValuePairs.add(new BasicNameValuePair("RP_HTML", "TEXT"));
        nameValuePairs.add(new BasicNameValuePair("RP_CONTENT", Content));
        nameValuePairs.add(new BasicNameValuePair("RP_NAME", "1"));
        nameValuePairs.add(new BasicNameValuePair("isPass", "1"));
        nameValuePairs.add(new BasicNameValuePair("x", "0"));
        nameValuePairs.add(new BasicNameValuePair("y", "0"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));
            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
                reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, "WriteReply response = " + responseString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseString.indexOf("alert('정상적인 접근이 아닙니다") >= 0) {
            return false;
        }
        return true;
    }

    public boolean DeleteContent(String bbsID, String uID) {
        String responseString = "";
        HttpPost httpPost = new HttpPost(Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&action=delete&uid=" + uID + "&p=1");
        httpPost.setHeader("referer", Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&query=view&uid=" + uID + "&p=1");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("table", bbsID));
        nameValuePairs.add(new BasicNameValuePair("action", "delete"));
        nameValuePairs.add(new BasicNameValuePair("uid", uID));
        nameValuePairs.add(new BasicNameValuePair("p", "1"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));
            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
                reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, "DeleteContent response = " + responseString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseString.indexOf("alert('정상적인 접근이 아닙니다") >= 0) {
            return false;
        }
        return true;
    }

    public boolean DeleteReply(String bbsID, String uID, String rID) {
        String responseString = "";
        HttpPost httpPost = new HttpPost(Const.http + "www.ac3korea.com/ac3korea");
        httpPost.setHeader("referer", Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&query=view&uid=" + uID + "&p=1");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("table", bbsID));
        nameValuePairs.add(new BasicNameValuePair("action", "cdelete"));
        nameValuePairs.add(new BasicNameValuePair("uid", uID));
        nameValuePairs.add(new BasicNameValuePair("ruid", rID));
        nameValuePairs.add(new BasicNameValuePair("p", "1"));
        nameValuePairs.add(new BasicNameValuePair("Nparam", ".ac3korea.com/ac3korea?table=" + bbsID + "&query=view&p=1&uid=" + uID));
        nameValuePairs.add(new BasicNameValuePair("COMP+PASS", ""));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));
            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
                reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, "DeleteReply response = " + responseString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseString.indexOf("alert('정상적인 접근이 아닙니다") >= 0) {
            return false;
        }
        return true;
    }

//    public boolean uploadFile(String path, String bbsID) {
//        String responseString = "";
//        HttpPost httpPost = new HttpPost(Const.http + "www.ac3korea.com/bbs/lib/module/upload/upload.php");
//        httpPost.setHeader("referer", Const.http + "www.ac3korea.com/bbs/lib/module/upload/upform.php?table=" + bbsID + "&orign_files=&orign_size=&html=HTML");
//        File file = new File(path);
//        Log.d(TAG, "----file " + file.getName());
//        //Multipart 객체를 선언한다.
////        MultipartEntityBuilder builder = MultipartEntityBuilder.create() //객체 생성...
////                .setCharset(Charset.forName("UTF-8")) //인코딩을 UTF-8로.. 다들 UTF-8쓰시죠?
////                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
////        builder.addPart("upFile", new FileBody(file)); //빌더에 FileBody 객체에 인자로 File 객체를 넣어준다.
//
//        try {
//            ContentBody bin = new FileBody(file);
//            MultipartEntity ent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//            ent.addPart("upFile", bin);
//            ent.addPart("filename", new StringBody(file.getName(), Charset.forName("euc-kr")));
//            ent.addPart("filesize", new StringBody(String.valueOf(file.length()), Charset.forName("euc-kr")));
//            httpPost.setEntity(ent);
//            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
//            }
//            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
//            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
//            Log.d(TAG, "uploadFile response = " + responseString);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (responseString.indexOf("alert('정상적인 접근이 아닙니다") >= 0) {
//            return false;
//        }
//        return true;
//    }

    public String uploadFile(String path) {
        String uploadedImageUrl = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Const.http + "api.imgur.com/3/upload.json");
        httpPost.setHeader("Authorization", "Client-ID " + "855d30528c4d56b");

        try {
            File file = new File(path);
            //Log.d(TAG, "----file " + file.getName());
            ContentBody bin = new FileBody(file);
            MultipartEntity ent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,
                    null, Charset.forName("euc-kr"));
            ent.addPart("image", bin);
            httpPost.setEntity(ent);
            HttpResponse response = httpclient.execute(httpPost);
            final String response_string = EntityUtils.toString(response.getEntity());

            final JSONObject json = new JSONObject(response_string);

            //Log.d(TAG, json.toString());

            JSONObject data = json.optJSONObject("data");
            boolean success = json.getBoolean("success");
            uploadedImageUrl = data.optString("link");
//            Log.d(TAG, "uploaded success : " + success);
//            Log.d(TAG, "uploaded image url : " + uploadedImageUrl);
//            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
//            Log.d(TAG, "uploadFile response = " + responseString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uploadedImageUrl;
    }

    public boolean Scrab(String bbsID, String uID) {
        String responseString = "";
        HttpPost httpPost = new HttpPost(Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&action=scrab&uid=" + uID);
        httpPost.setHeader("referer", Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&query=view&uid=" + uID);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("table", bbsID));
        nameValuePairs.add(new BasicNameValuePair("action", "scrab"));
        nameValuePairs.add(new BasicNameValuePair("uid", uID));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));
            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
                reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, "DeleteReply response = " + responseString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseString.indexOf("alert('정상적인 접근이 아닙니다") >= 0) {
            return false;
        }
        return true;
    }

    public void download(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        try {
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            List<String> pathSegmentList = Uri.parse(url).getPathSegments();
            request.setTitle(pathSegmentList.get(pathSegmentList.size() - 1));
            request.setDescription(mContext.getString(R.string.content_image_saving));

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + "ac3korea").mkdirs();
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/" + "ac3korea" + "/", pathSegmentList.get(pathSegmentList.size() - 1));

            downloadManager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean ReplyAnswer(String bbsID, String uID, String rID, int grade) {
        String responseString = "";
        HttpPost httpPost = new HttpPost(Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&action=setanswer&grade=" + grade + "&uid=" + uID + "&ruid=" + rID + "&p=1");
        httpPost.setHeader("referer", Const.http + "www.ac3korea.com/ac3korea?table=" + bbsID + "&query=view&uid=" + uID + "&p=1");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("table", bbsID));
        nameValuePairs.add(new BasicNameValuePair("action", "setanswer"));
        nameValuePairs.add(new BasicNameValuePair("grade", "" + grade));
        nameValuePairs.add(new BasicNameValuePair("uid", uID));
        nameValuePairs.add(new BasicNameValuePair("ruid", rID));
        nameValuePairs.add(new BasicNameValuePair("p", "1"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));
            if (Repo.getInstance(mContext).getHttpClient() == null) {
//                Repo.getInstance(mContext).setHttpclient();
                reLogin();
            }
            HttpResponse response = Repo.getInstance(mContext).getHttpClient().execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity(), "euc-kr");
            //Log.d(TAG, "DeleteReply response = " + responseString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseString.indexOf("alert('정상적인 접근이 아닙니다") >= 0) {
            return false;
        }
        return true;
    }

    public String encrypt(String str) {
        try {
            return aes256.aesEncode(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return str;
    }

    public String decrypt(String str) {
        try {
            return aes256.aesDecode(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return str;
    }
}
