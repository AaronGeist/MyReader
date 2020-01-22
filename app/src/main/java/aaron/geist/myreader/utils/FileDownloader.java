package aaron.geist.myreader.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by maojun on 2017/1/1.
 */

public class FileDownloader {

    public static String download(String src, String path) {
        File dirFile = new File(Environment.getExternalStorageDirectory() + path);

        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        String filePath;
        try {
            String fileName = getFileName(src);
            filePath = Environment.getExternalStorageDirectory() + path + fileName;

            File myCaptureFile = new File(filePath);
            if (!myCaptureFile.exists()) {
                myCaptureFile.createNewFile();
            } else {
                if (myCaptureFile.length() > 0) {
                    // file already downloaded, skip
                    return filePath;
                }
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));

            try {
                URL url = new URL(src);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    InputStream inStream = conn.getInputStream();

                    int byteCount;
                    byte[] bytes = new byte[1024];
                    while ((byteCount = inStream.read(bytes)) != -1) {
                        bos.write(bytes, 0, byteCount);
                    }
                    inStream.close();
                    bos.flush();
                    bos.close();

                }
            } catch (Exception e) {
                Log.e("", "download exception", e);
            }

            if (ImageUtil.isGifFile(myCaptureFile)) {
                if (!fileName.toLowerCase().endsWith("gif")) {
                    filePath += ".gif";
                    FileUtil.move(myCaptureFile, new File(filePath));
                }
            } else {
//                // compression
//                InputStream inputStream = new FileInputStream(filePath);
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//
//                String newFilePath = filePath + ".jpg";
//                File newFile = new File(newFilePath);
//                if (!newFile.exists()) {
//                    newFile.createNewFile();
//                }
//                BufferedOutputStream newBos = new BufferedOutputStream(new FileOutputStream(newFile));
//
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, newBos);
//                FileUtil.delete(myCaptureFile);
//                filePath = newFilePath;
            }
        } catch (Exception e) {
            Log.e("", "download exception", e);
            return "";
        }

        return filePath;

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
