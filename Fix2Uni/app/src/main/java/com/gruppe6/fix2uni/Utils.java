package com.gruppe6.fix2uni;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Utils {
    /**
     * Converts a base64-encoded image to a bitmap
     * @param base64Str
     * @return converted bitmap
     * @throws IllegalArgumentException
     */
    public static Bitmap base64toBitmap(final String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode( base64Str.substring(base64Str.indexOf(",") + 1), Base64.DEFAULT );
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * Converts a bitmap to a base64-encoded string
     * @param bitmap
     * @return string representing the image
     */
    public static String bitmapToBase64(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }
}
