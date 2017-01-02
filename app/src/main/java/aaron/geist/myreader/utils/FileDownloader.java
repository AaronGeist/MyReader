package aaron.geist.myreader.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import aaron.geist.myreader.activity.MainActivity;

/**
 * Created by maojun on 2017/1/1.
 */

public class FileDownloader {

    public static Bitmap downloadImage(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream inStream = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inStream);
                return bitmap;
            }
        } catch (Exception e) {
            Log.e("", "download exception", e);
        }
        return null;
    }

    public static String download(String src, String path) {
        File dirFile = new File(Environment.getExternalStorageDirectory() + path);

        if (!dirFile.exists()) {
            // TODO check before write files
            dirFile.mkdirs();
        }

        String fileName = "";
        try {
            fileName = getFileName(src);
            File myCaptureFile = new File(Environment.getExternalStorageDirectory() + path + fileName);
            if (!myCaptureFile.exists()) {
                // TODO check?
                myCaptureFile.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            downloadImage(src).compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.e("", "download exception", e);
            return "";
        }

        return "file://" + Environment.getExternalStorageDirectory() + path + fileName;

    }

    private static String getFileName(String filePath) {

        int start = filePath.lastIndexOf("/");
        int end = filePath.length();
        if (start != -1) {
            return filePath.substring(start + 1, end);
        } else {
            return null;
        }

    }
}
