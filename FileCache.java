package com.easemob.easeui.oss;

import android.content.Context;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FileCache {

	  //�����ļ�Ŀ¼ 
	  private File mCacheDir;
	  /**
	   * ���������ļ�Ŀ¼�������SD������ʹ��SD�����û����ʹ��ϵͳ�Դ��Ŀ¼
	   * @param context
	   * @param cacheDir ͼƬ�����һ��Ŀ¼
	   */
	public FileCache(Context context, File cacheDir, String dir){
	if(android.os.Environment.getExternalStorageState()
			.equals(android.os.Environment.MEDIA_MOUNTED))
	      mCacheDir = new File(cacheDir, dir);
	   else
	    mCacheDir = context.getCacheDir();// ��λ�ȡϵͳ���õĻ���洢·��
	   if(!mCacheDir.exists()) mCacheDir.mkdirs();
	  }
	  public File getFile(String url){
	    File f=null;
	    try {
	//��url���б༭���������·������
	      String filename = URLEncoder.encode(url,"utf-8");
	      f = new File(mCacheDir, filename);
	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	    }
	    return f;
	  }
	  public void clear(){//�����ļ�
	    File[] files = mCacheDir.listFiles();
	    for(File f:files)f.delete();
	}
}
