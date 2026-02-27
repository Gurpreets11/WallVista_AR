package com.preet.wallvistaar.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.preet.wallvistaar.R;
import com.preet.wallvistaar.base.BaseActivity;
import com.preet.wallvistaar.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.annotation.Nullable;

public class PreviewActivity extends BaseActivity {

    private String originalPath;
    private String maskPath;

    private Bitmap originalBitmap;
    private Bitmap maskBitmap;
    private Bitmap editedBitmap;

   // private ImageView imagePreview;
    private LinearLayout colorContainer;
    private Button btnToggle;
    private Button btnSave;

    private boolean isShowingOriginal = true;


    private float currentOpacity = 1.0f;

    private static final int MODE_NONE = 0;
    private static final int MODE_COLOR = 1;
    private static final int MODE_WALLPAPER = 2;

    private int lastMode = MODE_NONE;
    private int lastSelectedColor;
    private Bitmap lastSelectedWallpaper;

    ImageView originalImage;
    ImageView effectImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);



        originalImage = findViewById(R.id.originalImage);
        effectImage = findViewById(R.id.effectImage);

        originalImage.setImageBitmap(originalBitmap);

        //imagePreview = findViewById(R.id.imagePreview);
        colorContainer = findViewById(R.id.colorContainer);

        btnToggle = findViewById(R.id.btnToggle);
        btnSave = findViewById(R.id.btnSave);

        originalPath = getIntent().getStringExtra("originalPath");
        maskPath = getIntent().getStringExtra("maskPath");

        loadImages();
        setupColorOptions();
        setupButtons();

        wallpaperContainer = findViewById(R.id.wallpaperContainer);
        loadWallpapers();

        SeekBar opacitySeekBar = findViewById(R.id.opacitySeekBar);

        opacitySeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        currentOpacity = progress / 100f;

                        // Reapply last effect
                        if (lastMode == MODE_COLOR) {
                            applyColorToWall(lastSelectedColor);
                        } else if (lastMode == MODE_WALLPAPER) {
                            applyWallpaperToWall(lastSelectedWallpaper);
                        }
                    }

                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });
    }

    private LinearLayout wallpaperContainer;

    private final String[] wallpaperList = {
            "brick.jpg",
            "wood.jpg",
            "marble.jpg",
            "fabric.jpg",
            "pattern.jpg"
    };

    @Override
    protected void onAllPermissionsGranted() {
        // Not required here
    }

    // ---------------------------------------------------
    // LOAD WALLPAPER
    // ---------------------------------------------------

    private void loadWallpapers() {

        for (String wallpaperName : wallpaperList) {

            ImageView imageView = new ImageView(this);

            int size = dpToPx(80);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(size, size);

            params.setMargins(16, 0, 16, 0);

            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(android.R.color.darker_gray);

            // Load preview image
            Bitmap wallpaperBitmap = loadWallpaperFromAssets(wallpaperName);
            imageView.setImageBitmap(wallpaperBitmap);

            imageView.setOnClickListener(v -> {
//                applyWallpaperToWall(wallpaperBitmap);

                lastMode = MODE_WALLPAPER;
                lastSelectedWallpaper = wallpaperBitmap;
                applyWallpaperToWall(wallpaperBitmap);

            });

            wallpaperContainer.addView(imageView);
        }
    }

    private Bitmap loadWallpaperFromAssets(String fileName) {
        try {
            InputStream is = getAssets().open("wallpapers/" + fileName);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
    // ---------------------------------------------------
    // LOAD IMAGES
    // ---------------------------------------------------

    private void loadImages() {
        originalBitmap = BitmapFactory.decodeFile(originalPath);
        maskBitmap = BitmapFactory.decodeFile(maskPath);

//        imagePreview.setImageBitmap(originalBitmap);
        originalImage.setImageBitmap(originalBitmap);
//        imagePreview.setImageBitmap(maskBitmap);

        Log.d("PREVIEW_DEBUG", "Original path: " + originalPath);
        Log.d("PREVIEW_DEBUG", "Mask path: " + maskPath);
    }

    // ---------------------------------------------------
    // COLOR OPTIONS
    // ---------------------------------------------------

    private void setupColorOptions() {

        int[] colors = {
                Color.RED,
                Color.BLUE,
                Color.GREEN,
                Color.YELLOW,
                Color.GRAY,
                Color.CYAN,
                Color.MAGENTA,
                Color.parseColor("#795548") // brown
        };

        for (int color : colors) {

            View colorView = new View(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(150,150);
            params.setMargins(20,20,20,20);

            colorView.setLayoutParams(params);
            colorView.setBackgroundColor(color);

            colorView.setOnClickListener(v -> {
                Log.d("COLOR_CLICK", "Color clicked: " + color);
                //applyColorToWall(color);
                lastMode = MODE_COLOR;
                lastSelectedColor = color;
                applyColorToWall(color);

            });

            colorContainer.addView(colorView);
        }
    }

    // ---------------------------------------------------
    // APPLY COLOR USING MASK
    // ---------------------------------------------------

    private void applyColorToWall(int color) {
        Log.d("DEBUG", "Original: " + (originalBitmap != null));
        Log.d("DEBUG", "Mask: " + (maskBitmap != null));
        if (originalBitmap == null || maskBitmap == null) return;

        Bitmap resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        /*Bitmap maskScaled = Bitmap.createScaledBitmap(
                maskBitmap,
                resultBitmap.getWidth(),
                resultBitmap.getHeight(),
                false
        );*/

        Bitmap maskScaled = maskBitmap;

        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();

        int[] originalPixels = new int[width * height];
        int[] maskPixels = new int[width * height];

        resultBitmap.getPixels(originalPixels, 0, width, 0, 0, width, height);
        maskScaled.getPixels(maskPixels, 0, width, 0, 0, width, height);

        int paintR = Color.red(color);
        int paintG = Color.green(color);
        int paintB = Color.blue(color);

        for (int i = 0; i < originalPixels.length; i++) {

            int maskPixel = maskPixels[i];

            //if (Color.red(maskPixel) > 128) {
//            int maskValue = maskPixel & 0xFF;
//            Log.d("DEBUG", "maskValue: " + maskValue);
//            if (maskValue > 10) {
            int rMask = Color.red(maskPixel);
            int gMask = Color.green(maskPixel);
            int bMask = Color.blue(maskPixel);

//            Log.d("DEBUG", "rMask: " + rMask);
//            Log.d("DEBUG", "gMask: " + gMask);
//            Log.d("DEBUG", "bMask: " + bMask);
            //if (rMask > 10 || gMask > 10 || bMask > 10) {
            if (maskPixels[i] != Color.BLACK) {
                int origColor = originalPixels[i];

                // old code without opacity
                /*int r = Color.red(origColor);
                int g = Color.green(origColor);
                int b = Color.blue(origColor);

                // Blend 60% original + 40% paint
                r = (int)(r * 0.6 + paintR * 0.4);
                g = (int)(g * 0.6 + paintG * 0.4);
                b = (int)(b * 0.6 + paintB * 0.4);

                originalPixels[i] = Color.rgb(r, g, b);*/

                // new code for opacity..
                int rBlend = (Color.red(origColor) * Color.red(color)) / 255;
                int gBlend = (Color.green(origColor) * Color.green(color)) / 255;
                int bBlend = (Color.blue(origColor) * Color.blue(color)) / 255;

                int r = (int)((1 - currentOpacity) * Color.red(origColor) + currentOpacity * rBlend);
                int g = (int)((1 - currentOpacity) * Color.green(origColor) + currentOpacity * gBlend);
                int b = (int)((1 - currentOpacity) * Color.blue(origColor) + currentOpacity * bBlend);

                originalPixels[i] = Color.rgb(r, g, b);
            }
        }

        resultBitmap.setPixels(originalPixels, 0, width, 0, 0, width, height);

        editedBitmap = resultBitmap;
//        imagePreview.setImageBitmap(editedBitmap);

        effectImage.setImageBitmap(resultBitmap);
        effectImage.setAlpha(currentOpacity);

        isShowingOriginal = false;


    }

    // ---------------------------------------------------
    // BUTTON SETUP
    // ---------------------------------------------------

    private void setupButtons() {

        btnToggle.setOnClickListener(v -> toggleImage());

        btnSave.setOnClickListener(v -> saveEditedImage());
    }

    // ---------------------------------------------------
    // TOGGLE ORIGINAL / EDITED
    // ---------------------------------------------------

    private void toggleImage() {

        if (editedBitmap == null) return;

        /*if (isShowingOriginal) {
            imagePreview.setImageBitmap(editedBitmap);
        } else {
            imagePreview.setImageBitmap(originalBitmap);
        }*/

        isShowingOriginal = !isShowingOriginal;
    }

    // ---------------------------------------------------
    // SAVE EDITED IMAGE
    // ---------------------------------------------------

    private void saveEditedImage() {

        if (editedBitmap == null) return;

        try {

            File editedFile = new File(
                    FileUtils.getEditedFolder(),
                    "EDITED_" + System.currentTimeMillis() + ".png"
            );

            FileOutputStream fos =
                    new FileOutputStream(editedFile);

            editedBitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    fos
            );

            fos.flush();
            fos.close();

            Toast.makeText(
                    this,
                    "Saved: " + editedFile.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void applyWallpaperToWall(Bitmap wallpaperBitmap) {

        if (originalBitmap == null || maskBitmap == null) return;

        Bitmap resultBitmap =
                originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();

        Bitmap scaledWallpaper = Bitmap.createScaledBitmap(
                wallpaperBitmap,
                width,
                height,
                true
        );

        int[] originalPixels = new int[width * height];
        int[] maskPixels = new int[width * height];
        int[] wallpaperPixels = new int[width * height];

        resultBitmap.getPixels(originalPixels, 0, width, 0, 0, width, height);
        maskBitmap.getPixels(maskPixels, 0, width, 0, 0, width, height);
        scaledWallpaper.getPixels(wallpaperPixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < originalPixels.length; i++) {

            if (maskPixels[i] != Color.BLACK) {

                int orig = originalPixels[i];
                int wall = wallpaperPixels[i];

                // old code without opacity
                // Multiply blend (realistic)
                /*int r = (Color.red(orig) * Color.red(wall)) / 255;
                int g = (Color.green(orig) * Color.green(wall)) / 255;
                int b = (Color.blue(orig) * Color.blue(wall)) / 255;

                originalPixels[i] = Color.rgb(r, g, b);*/

                // new code for opacity..
                int rBlend = (Color.red(orig) * Color.red(wall)) / 255;
                int gBlend = (Color.green(orig) * Color.green(wall)) / 255;
                int bBlend = (Color.blue(orig) * Color.blue(wall)) / 255;

                int r = (int)((1 - currentOpacity) * Color.red(orig) + currentOpacity * rBlend);
                int g = (int)((1 - currentOpacity) * Color.green(orig) + currentOpacity * gBlend);
                int b = (int)((1 - currentOpacity) * Color.blue(orig) + currentOpacity * bBlend);

                originalPixels[i] = Color.rgb(r, g, b);
            }
        }

        resultBitmap.setPixels(originalPixels, 0, width, 0, 0, width, height);
//        imagePreview.setImageBitmap(resultBitmap);

        effectImage.setImageBitmap(resultBitmap);
        effectImage.setAlpha(currentOpacity);

    }

    /*private Bitmap loadWallpaperFromAssets(String fileName) {
        try {
            InputStream is = getAssets().open("wallpapers/" + fileName);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/
}