package com.preet.wallvistaar.ai;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.preet.wallvistaar.utils.FileUtils;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WallSegmentationManager {

    public static String generateWallMask(String imagePath) {

        // 1️⃣ Load original bitmap
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);

        Mat originalMat = new Mat();
        Utils.bitmapToMat(originalBitmap, originalMat);

        // Convert to BGR (important for some devices)
        Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_RGBA2BGR);

        // --------------------------------------------------
        // 2️⃣ Better Segmentation Using Brightness Threshold
        // --------------------------------------------------

        Mat hsv = new Mat();
        Imgproc.cvtColor(originalMat, hsv, Imgproc.COLOR_BGR2HSV);

        List<Mat> hsvChannels = new ArrayList<>();
        Core.split(hsv, hsvChannels);

        Mat valueChannel = hsvChannels.get(2);

        // Blur to remove noise
        Imgproc.GaussianBlur(valueChannel, valueChannel, new Size(7, 7), 0);

        // Threshold (tune this value if needed)
        Mat thresh = new Mat();
        Imgproc.threshold(valueChannel, thresh, 180, 255, Imgproc.THRESH_BINARY);


        /*Imgproc.adaptiveThreshold(
                valueChannel,
                thresh,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV,
                21,
                10
        );*/

        // Morphological close to fill gaps
        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                new Size(15, 15)
        );

        Imgproc.morphologyEx(thresh, thresh,
                Imgproc.MORPH_CLOSE, kernel);

        // --------------------------------------------------
        // 3️⃣ Find Contours
        // --------------------------------------------------

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(thresh, contours, hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);

        double imageArea = originalMat.size().area();
        double maxArea = 0;
        MatOfPoint largestContour = null;

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);

            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }

        Log.d("SEGMENT", "Contours found: " + contours.size());
        Log.d("SEGMENT", "Largest contour area: " + maxArea);
        Log.d("SEGMENT", "Image area: " + imageArea);

        // --------------------------------------------------
        // 4️⃣ Create Mask
        // --------------------------------------------------

        Mat mask = Mat.zeros(originalMat.size(), CvType.CV_8UC1);

        if (largestContour != null) {
            List<MatOfPoint> drawList = new ArrayList<>();
            drawList.add(largestContour);

            Imgproc.drawContours(mask, drawList, -1, new Scalar(255), -1);
        }

        // Debug mask values
        Core.MinMaxLocResult result = Core.minMaxLoc(mask);
        Log.d("MASK_DEBUG", "Min: " + result.minVal + " Max: " + result.maxVal);

        // --------------------------------------------------
        // 5️⃣ Convert to RGBA for Android
        // --------------------------------------------------

        Mat maskColor = new Mat();
        Imgproc.cvtColor(mask, maskColor, Imgproc.COLOR_GRAY2RGBA);

        // Force alpha channel = 255
        List<Mat> channels = new ArrayList<>();
        Core.split(maskColor, channels);
        channels.get(3).setTo(new Scalar(255));
        Core.merge(channels, maskColor);

        // --------------------------------------------------
        // 6️⃣ Convert to Bitmap
        // --------------------------------------------------

        Bitmap maskBitmap = Bitmap.createBitmap(
                maskColor.cols(),
                maskColor.rows(),
                Bitmap.Config.ARGB_8888
        );

        Utils.matToBitmap(maskColor, maskBitmap);

        // --------------------------------------------------
        // 7️⃣ Save Mask File
        // --------------------------------------------------

        File maskFile = new File(
                FileUtils.getMaskFolder(),
                "MASK_" + System.currentTimeMillis() + ".png"
        );

        try {
            FileOutputStream fos = new FileOutputStream(maskFile);
            maskBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maskFile.getAbsolutePath();
    }
}
