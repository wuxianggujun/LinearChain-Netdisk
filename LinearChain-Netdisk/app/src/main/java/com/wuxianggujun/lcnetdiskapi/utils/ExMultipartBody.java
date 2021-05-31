package com.wuxianggujun.lcnetdiskapi.utils;

import okhttp3.RequestBody;
import okhttp3.MediaType;
import okio.BufferedSink;
import java.io.IOException;
import okhttp3.MultipartBody;
import android.util.Log;
import okio.ForwardingSink;
import okio.Buffer;
import okio.Okio;

public class ExMultipartBody extends RequestBody {

    private RequestBody mRequestBody;
    private long  mCurrentLength;
    private UploadProgressListener mProgressListener;

    public ExMultipartBody(MultipartBody mRequestBody,UploadProgressListener mProgressListener) {
        this.mRequestBody = mRequestBody;
        this.mProgressListener = mProgressListener;
    }
    
    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        //获取总长度
        final long contentLength = contentLength();
        //写入多少条数据
        ForwardingSink forwardingSink = new ForwardingSink(sink){
            
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                mCurrentLength += byteCount;
                if(mProgressListener != null){
                    mProgressListener.onProgress(contentLength,mCurrentLength);
                }
                Log.e("TAG","文件总长度:"+ contentLength + " / " +"文件上传进度:"+ mCurrentLength);
                super.write(source,byteCount);           
           }        
        };
       BufferedSink bufferedSink = Okio.buffer(forwardingSink);
       mRequestBody.writeTo(bufferedSink);
        //刷新一下,否则会发现上传不成功
        bufferedSink.flush();
    }

    
    
    
}
