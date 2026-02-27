package com.preet.wallvistaar;

import android.app.Application;

import com.preet.wallvistaar.ai.OpenCVHelper;
import com.preet.wallvistaar.utils.FileUtils;

public class WallVistaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize OpenCV
        OpenCVHelper.init(this);

        // Create required folders
        FileUtils.getOriginalFolder();
        FileUtils.getMaskFolder();
        FileUtils.getEditedFolder();
    }
}
