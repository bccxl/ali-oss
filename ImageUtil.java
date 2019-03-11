package com.easemob.easeui.oss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtil {

	static String endpoint = "http://img.lesgoapp.cc";
	private static String name = "lesgo";
	static  Bitmap bitmap = null;
	static  InputStream inputStream  = null;
	static  OutputStream os = null;

	  public static Bitmap loadBitmapFromWeb(Context context,String url, final File file) {

	    try {
			OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider("LTAIvkmoS1lN2mCd", "3KLqFPW20hpkXUmFvBwY70P5sxiDcG");
			ClientConfiguration conf = new ClientConfiguration();
			conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
			conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
			conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
			conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
			OSS oss = new OSSClient(context, endpoint, credentialProvider, conf);
			GetObjectRequest get = new GetObjectRequest(name, url);
			OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
				@Override
				public void onSuccess(GetObjectRequest request, GetObjectResult result) {
					// 请求成功
					 inputStream = result.getObjectContent();
					try {
						os = new FileOutputStream(file);
						copyStream(inputStream, os);//
						bitmap = decodeFile(file);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
					// 请求异常
					if (clientExcepion != null) {
						// 本地异常如网络异常等
						clientExcepion.printStackTrace();
					}
					if (serviceException != null) {
						// 服务异常
						Log.e("ErrorCode", serviceException.getErrorCode());
						Log.e("RequestId", serviceException.getRequestId());
						Log.e("HostId", serviceException.getHostId());
						Log.e("RawMessage", serviceException.getRawMessage());
					}
				}
			});
	      return bitmap;
	    } catch (Exception ex) {
	      ex.printStackTrace();
	      return null;
	    } finally {
	      try {
	        if(os != null) os.close();
	        if(inputStream != null) inputStream.close();
	      } catch (IOException e) {  }
	    }
	  }
	  public static Bitmap decodeFile(File f) {
	    try {
	      return BitmapFactory.decodeStream(new FileInputStream(f), null, null);
	    } catch (Exception e) { } 
	    return null;
	  }
	  private static void copyStream(InputStream is, OutputStream os) {
	    final int buffer_size = 1024;
	    try {
	      byte[] bytes = new byte[buffer_size];
	      for (;;) {
	        int count = is.read(bytes, 0, buffer_size);
	        if (count == -1)
	          break;
	        os.write(bytes, 0, count);
	      }
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	  }
}
