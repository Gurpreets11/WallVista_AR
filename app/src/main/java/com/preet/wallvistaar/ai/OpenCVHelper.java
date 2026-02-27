package com.preet.wallvistaar.ai;


import android.content.Context;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class OpenCVHelper {

    public static boolean init(Context context) {
        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV initialized successfully");
            return true;
        } else {
            Log.e("OpenCV", "OpenCV initialization failed");
            return false;
        }
    }
}
