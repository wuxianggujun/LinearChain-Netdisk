package com.wuxianggujun.lcnetdiskapi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Context;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Callback;
import java.io.IOException;
import okhttp3.Response;
import android.util.Log;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okio.BufferedSink;
import okhttp3.Headers;
import okhttp3.internal.http2.Header;
import org.json.JSONObject;
import org.json.JSONException;
import android.os.Looper;
import okhttp3.MultipartBody;
import java.io.File;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import com.wuxianggujun.lcnetdiskapi.utils.ExMultipartBody;
import com.wuxianggujun.lcnetdiskapi.utils.UploadProgressListener;
import com.wuxianggujun.lcnetdiskapi.view.HorizontalProgressBarWithNumber;
import android.os.Handler;
import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.os.Build;
import android.provider.DocumentsContract;
import android.os.Environment;
import android.content.ContentUris;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.text.SpannableString;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.wuxianggujun.lcnetdiskapi.utils.TextClickSpan;
import android.text.Spanned;
import org.apache.commons.lang3.StringEscapeUtils;
import android.os.Message;

/*****************************************                                       *
*                                        *
*           @author 无相孤君               *
*           @联系方式 3344207732           *                      
*                                         *
*******************************************/
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView tv_Parameters;//显示返回参数文本
    private EditText ed_Password;//是否设置密码输入框
    private TextView tv_FileInfo;//显示文件信息文本
    private Button bt_Positive;//上传按钮
    private String url = "http://ssdlearn.top/wangpan/api.php";
    private HorizontalProgressBarWithNumber mProgressBar;
  
   
    private Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
         switch(msg.what){
             case 1:
                 
                 String content = msg.getData().getString("content");
                 final String downurl = msg.getData().getString("downurl");
                 Log.i(TAG,"downurl"+downurl);
                 setTextHighLightWithClick(tv_Parameters,content,"查看", new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             Toast.makeText(getApplication(), "跳转浏览器!", Toast.LENGTH_SHORT).show();
                             Intent intent= new Intent();        
                             intent.setAction("android.intent.action.VIEW");    
                             Uri content_url = Uri.parse(downurl);   
                             intent.setData(content_url);  
                             startActivity(intent);                           
                         }
                     });
                     
                 break;
         }
        }      
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_Parameters = findViewById(R.id.showTitle);
        ed_Password = findViewById(R.id.showPassword);
        tv_FileInfo = findViewById(R.id.showContent);
        bt_Positive = findViewById(R.id.showPositive);
        mProgressBar = findViewById(R.id.id_progressbar01);
        bt_Positive.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View p1) {
                    Toast.makeText(getApplication(), "跳转选择文件!", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent,1);                 
             
                }
            });      
            
        
        
   
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {                                     //使用第三方应用打开
                Toast.makeText(this, uri.getPath() + "11111", Toast.LENGTH_SHORT).show();
                return;
            }
            //4.4以后
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
              String  mFileNamePath = getPath(this, uri);

                if (mFileNamePath != null) {
                  UploadFile(mFileNamePath,url);
                    
                } else {
                    Toast.makeText(this, "文件路径异常 222", Toast.LENGTH_SHORT).show();
                }

                Log.d(TAG, "所选择的文件路径:  " + mFileNamePath);
            } else {//4.4以下系统调用方法
                Toast.makeText(MainActivity.this, getRealPathFromURI(uri) + "222222", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 4.4的从Uri获取文件绝对路径
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                                                        null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    

    private void UploadFile(String filePath,String url) {
        
        File file = new File(filePath);
        String fileName = file.getName();       
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        OkHttpClient okHttpClient = new OkHttpClient();

        MultipartBody.Builder mBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("show","1")
            .addFormDataPart("ispwd","0")
            .addFormDataPart("pwd","")
            .addFormDataPart("format","json")
            .addFormDataPart("file", fileName, fileBody);
           
        ExMultipartBody exMultipartBody = new ExMultipartBody(mBody.build(), new UploadProgressListener(){

                @Override
                public void onProgress(long total, long current) {
                    mProgressBar.setMax(100);
                    int 辣鸡百分比 =(int) (current * 1.0f / total * 100);
                    //(int)(current * 100 / total);
                    //int baifenbi = ((int)(current / total) * 100);
                    Log.i(TAG,"总数:"+total+"当前上传进度:"+current+"当前百分比:"+辣鸡百分比);
                    mProgressBar.setProgress(辣鸡百分比);                  
                    //showToast(total,current);
                }              
            });

        Request request = new Request.Builder()
            .url(url)
            .post(exMultipartBody)
            .build();

        Call call =  okHttpClient.newCall(request);
        call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    Log.d(TAG, response.protocol() + " " + response.code() + " " + response.message());
                    if (response.isSuccessful()) {
                        String data = StringEscapeUtils.unescapeJava(response.body().string());
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                             String name = jsonObject.optString("name");
                             String msg = jsonObject.optString("msg");
                             String downUrl = jsonObject.optString("downurl","空值");  
                             
                             String str = null;
                             str= String.format("[%s]-->%s!%n点击查看下载文件",name,msg,downUrl);
                             if(str!=null && downUrl!=null){
                                 Message message= new Message();
                                 Bundle bundle = new Bundle();
                                 bundle.putString("content",str);
                                 bundle.putString("downurl",downUrl);
                                 message.setData(bundle);
                                 message.what = 1;
                                 myHandler.sendMessage(message);
                             }                         
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    
                   /* Headers headers = response.headers();
                    for (int i=0;i < headers.size();i++) {
                        Log.d(TAG, headers.name(i) + ":" + headers.value(i));       
                    }*/
   
                }

            });

        
    }
    
    public static void setTextHighLightWithClick(TextView tv, String text, String keyWord, View.OnClickListener listener)
    {
        tv.setClickable(true);
        tv.setHighlightColor(Color.TRANSPARENT);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        SpannableString s = new SpannableString(text);
        Pattern p = Pattern.compile(keyWord);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            int start = m.start();
            int end = m.end();
            s.setSpan(new TextClickSpan(listener), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(s);
    }

    
}
