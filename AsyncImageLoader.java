package com.easemob.easeui.oss;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncImageLoader {

	  private MemoryCache mMemoryCache;
	  private FileCache mFileCache;
	  private ExecutorService mExecutorService;
      private Context context;
	  private Map<ImageView, String> mImageViews = Collections
	      .synchronizedMap(new WeakHashMap<ImageView, String>());

	  private List<LoadPhotoTask> mTaskQueue = new ArrayList<LoadPhotoTask>();

	  public AsyncImageLoader(Context context, MemoryCache memoryCache, FileCache fileCache) {
		  this.context = context;
	    mMemoryCache = memoryCache;
	    mFileCache = fileCache;
	    mExecutorService = Executors.newFixedThreadPool(5);
	  }

	  public Bitmap loadBitmap(ImageView imageView, String url) {

	    mImageViews.put(imageView, url);
	    Bitmap bitmap = mMemoryCache.get(url);
	    if(bitmap == null) {
	      enquequeLoadPhoto(url, imageView);
	    }
	    return bitmap;
	  }

	  private void enquequeLoadPhoto(String url, ImageView imageView) {
	    if(isTaskExisted(url))
	      return;
	    LoadPhotoTask task = new LoadPhotoTask(url, imageView);
	    synchronized (mTaskQueue) {
	      mTaskQueue.add(task);
	    }
	    mExecutorService.execute(task);
	  }

	  private boolean isTaskExisted(String url) {
	    if(url == null)
	      return false;
	    synchronized (mTaskQueue) {
	      int size = mTaskQueue.size();
	      for(int i=0; i<size; i++) {
	        LoadPhotoTask task = mTaskQueue.get(i);
	        if(task != null && task.getUrl().equals(url))
	          return true;
	      }
	    }
	    return false;
	  }

	  private Bitmap getBitmapByUrl(String url) {
	    File f = mFileCache.getFile(url);
	    Bitmap b = ImageUtil.decodeFile(f);
	    if (b != null)
	      return b;
	    return ImageUtil.loadBitmapFromWeb(context,url, f);
	  }

	  private boolean imageViewReused(ImageView imageView, String url) {
	    String tag = mImageViews.get(imageView);
	    if (tag == null || !tag.equals(url))
	      return true;
	    return false;
	  }
	  private void removeTask(LoadPhotoTask task) {
	    synchronized (mTaskQueue) {
	      mTaskQueue.remove(task);
	    }
	  }
	  class LoadPhotoTask implements Runnable {
	    private String url;
	    private ImageView imageView;  
	    LoadPhotoTask(String url, ImageView imageView) {
	      this.url = url;
	      this.imageView = imageView;
	    }
	    @Override
	    public void run() {
	      if (imageViewReused(imageView, url)) {
	        removeTask(this);
	        return;
	      }
	      Bitmap bmp = getBitmapByUrl(url);
	      mMemoryCache.put(url, bmp);
	      if (!imageViewReused(imageView, url)) {
	      BitmapDisplayer bd = new BitmapDisplayer(bmp, imageView, url);
	      Activity a = (Activity) imageView.getContext();
	      a.runOnUiThread(bd);
	      }
	      removeTask(this);
	    }
	    public String getUrl() {
	      return url;
	    }
	  }

	  class BitmapDisplayer implements Runnable {
	    private Bitmap bitmap;
	    private ImageView imageView;
	    private String url;
	    public BitmapDisplayer(Bitmap b, ImageView imageView, String url) {
	      bitmap = b;
	      this.imageView = imageView;
	      this.url = url;
	    }
	    public void run() {
	      if (imageViewReused(imageView, url))
	        return;
	      if (bitmap != null)
	        imageView.setImageBitmap(bitmap);
	    }
	  }

	  public void destroy() {
	    mMemoryCache.clear();
	    mMemoryCache = null;
	    mImageViews.clear();
	    mImageViews = null;
	    mTaskQueue.clear();
	    mTaskQueue = null;
	    mExecutorService.shutdown();
	    mExecutorService = null;
	  }
}
