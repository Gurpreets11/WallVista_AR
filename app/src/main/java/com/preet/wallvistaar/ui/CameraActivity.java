package com.preet.wallvistaar.ui;

import android.content.Intent;
import android.os.Bundle;



import com.preet.wallvistaar.R;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.preet.wallvistaar.ai.WallSegmentationManager;
import com.preet.wallvistaar.base.BaseActivity;
import com.preet.wallvistaar.utils.FileUtils;


import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends BaseActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_camera);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
       /* ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        previewView = findViewById(R.id.previewView);
        Button btnCapture = findViewById(R.id.btnCapture);

        requestAppPermissions();

        btnCapture.setOnClickListener(v -> takePhoto());
    }

    @Override
    protected void onAllPermissionsGranted() {
        startCamera();
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {

        if (imageCapture == null) return;

        File photoFile = new File(
                FileUtils.getOriginalFolder(),
                "IMG_" + System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Log.d("Camera", "Saved: " + photoFile.getAbsolutePath());

                        // TODO: Move to PreviewActivity

                        new Thread(() -> {

                            String maskPath = WallSegmentationManager
                                    .generateWallMask(photoFile.getAbsolutePath());

                            runOnUiThread(() -> {

                                // TODO: Open PreviewActivity
                                Log.d("Mask", "Mask saved at: " + maskPath);

                                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                                intent.putExtra("originalPath", photoFile.getAbsolutePath());
                                intent.putExtra("maskPath", maskPath);
                                startActivity(intent);

                            });

                        }).start();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("Camera", "Error: " + exception.getMessage());
                    }
                }
        );
    }
}