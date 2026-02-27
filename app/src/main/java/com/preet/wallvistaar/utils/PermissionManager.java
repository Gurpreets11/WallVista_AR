package com.preet.wallvistaar.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    private final AppCompatActivity activity;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private PermissionCallback callback;

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public PermissionManager(AppCompatActivity  activity) {
        this.activity = activity;
    }

    public void initLauncher(PermissionCallback callback) {
        this.callback = callback;

        permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {

                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        callback.onPermissionGranted();
                    } else {
                        callback.onPermissionDenied();
                    }
                }
        );
    }

    public void checkAndRequestPermissions() {
        List<String> permissionList = new ArrayList<>();

        // Camera (required for all versions)
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            permissionList.add(Manifest.permission.CAMERA);
        }

        // Storage based on version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)) {
                permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionList.isEmpty()) {
            permissionLauncher.launch(permissionList.toArray(new String[0]));
        } else {
            callback.onPermissionGranted();
        }
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
