package com.preet.wallvistaar.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtils {

    public static File getAppRootFolder() {
        File picturesDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File appFolder = new File(picturesDir, Constants.APP_FOLDER_NAME);

        if (!appFolder.exists()) {
            appFolder.mkdirs();
        }

        return appFolder;
    }

    public static File getOriginalFolder() {
        File original = new File(getAppRootFolder(), Constants.ORIGINAL_FOLDER);
        if (!original.exists()) {
            original.mkdirs();
        }
        return original;
    }

    public static File getMaskFolder() {
        File mask = new File(getAppRootFolder(), Constants.MASK_FOLDER);
        if (!mask.exists()) {
            mask.mkdirs();
        }
        return mask;
    }

    public static File getEditedFolder() {
        File edited = new File(getAppRootFolder(), Constants.EDITED_FOLDER);
        if (!edited.exists()) {
            edited.mkdirs();
        }
        return edited;
    }
}
