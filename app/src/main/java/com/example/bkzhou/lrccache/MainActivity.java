package com.example.bkzhou.lrccache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity implements View.OnClickListener{
  private final String TAG = "MainActivity";
  private TextView textView;
  private Button button;
  private ImageView imageView;
  private LruCache<String, Bitmap> mMemoryCache;
  private String url = "http://photocdn.sohu.com/20160225/Img438520288.jpeg";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    textView = (TextView) this.findViewById(R.id.text);
    textView.setText(((KaleApplication) getApplication()).getMemoryCacheSize() + "");
    button = (Button) this.findViewById(R.id.button);
    imageView = (ImageView) this.findViewById(R.id.image);
    button.setOnClickListener(this);
//    Log.d(TAG,((KaleApplication) getApplication()).getMemoryCacheSize()+"");
    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    int cacheSize = maxMemory / 8;
    Log.d(TAG, cacheSize + "");
    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount() / 1024;
      }
    };
  }

  public void addBitmapToMemoryCache(String key, Bitmap value) {

    if (getBitmapFromMemCache(key) == null) {
      Log.d(TAG,key);
      mMemoryCache.put(key, value);
    }
  }

  public Bitmap getBitmapFromMemCache(String key) {
    return mMemoryCache.get(key);
  }

  public void loadBitmap(String resId, ImageView imageView) {
    final String imageKey = String.valueOf(resId);
    final Bitmap bitmap = getBitmapFromMemCache(imageKey);
    if (bitmap != null) {
      Log.d(TAG,"用过LruCache获取");
      imageView.setImageBitmap(bitmap);

    } else {
      Log.d(TAG,"通过网络获取");
      imageView.setImageResource(R.mipmap.ic_launcher);
      BitmapWorkerTask task = new BitmapWorkerTask(imageView);
      task.execute(url);
    }
  }



  @Override
  public void onClick(View view) {
    loadBitmap(url,imageView);
  }

  public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    public BitmapWorkerTask(ImageView imageView) {

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      imageView.setImageBitmap(bitmap);
    }

    protected Bitmap doInBackground(String... string) {
      HttpClient httpClient = new HttpClient();
      GetMethod httpGet = new GetMethod(string[0]);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Bitmap bitmap = null;
      InputStream inputStream = null;
      try {
        int httpStats = httpClient.executeMethod(httpGet);
        System.out.println(httpStats);
        if (httpStats == 200) {
          inputStream = httpGet.getResponseBodyAsStream();
          int len = 0;

          byte[] responseBody = new byte[1024];
          while ((len = inputStream.read(responseBody)) != -1) {
            outputStream.write(responseBody,0,len);
          }
          byte[] result = outputStream.toByteArray();
          bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
          addBitmapToMemoryCache(url, bitmap);

        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      return bitmap;
    }
  }
}
