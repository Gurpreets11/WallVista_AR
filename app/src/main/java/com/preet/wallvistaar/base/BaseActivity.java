package com.preet.wallvistaar.base;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.preet.wallvistaar.R;
import com.preet.wallvistaar.utils.FileUtils;
import com.preet.wallvistaar.utils.PermissionManager;


public abstract class BaseActivity extends AppCompatActivity {

    private PermissionManager permissionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupPermissionManager();
    }

    private void setupPermissionManager() {

        permissionManager = new PermissionManager(this);

        permissionManager.initLauncher(new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionGranted() {

                // Create folders after permission granted
                FileUtils.getOriginalFolder();
                FileUtils.getMaskFolder();
                FileUtils.getEditedFolder();

                onAllPermissionsGranted();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(BaseActivity.this,
                        "Permissions required for app functionality",
                        Toast.LENGTH_LONG).show();

                onPermissionsDenied();
            }
        });
    }

    protected void requestAppPermissions() {
        permissionManager.checkAndRequestPermissions();
    }

    // Child activities override this
    protected abstract void onAllPermissionsGranted();

    protected void onPermissionsDenied() {
        // Optional override
    }
}