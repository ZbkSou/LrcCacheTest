package com.example.bkzhou.lrccache;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

/**
 * Created by bkzhou on 16-3-2.
 */
public class KaleApplication extends Application {
  public int getMemoryCacheSize() {

    final int memClass = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    return 1024 * 1024 * memClass / 8;
  }
}
