package com.preet.wallvistaar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.preet.wallvistaar.R;
import com.preet.wallvistaar.base.BaseActivity;
import com.preet.wallvistaar.utils.FileUtils;
import com.preet.wallvistaar.utils.PermissionManager;

import org.opencv.core.Mat;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends BaseActivity {

    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Mat mat = new Mat();
        Log.d("OpenCV", "Mat created: " + mat.toString());

        requestAppPermissions();
        Button openCamera = findViewById(R.id.openCameraBT);
        openCamera.setOnClickListener(view -> {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        });


    }

    @Override
    protected void onAllPermissionsGranted() {
        FileUtils.getOriginalFolder();
        FileUtils.getMaskFolder();
        FileUtils.getEditedFolder();
    }
}