package aaron.geist.myreader.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Aaron on 2017/1/2.
 */

public class FileUtil {

    // only when folder is empty then delete folder will work.
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null || children.length == 0) {
                file.delete();
                return;
            }

            for (File child : children) {
                delete(child);
            }

            file.delete();
        }
    }

    public static void move(File src, File dest) {
        try {
            InputStream inputStream = new FileInputStream(src);

            OutputStream outputStream = new FileOutputStream(dest);
            int byteCount;
            byte[] bytes = new byte[1024];
            while ((byteCount = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, byteCount);
            }
            inputStream.close();
            outputStream.close();

            delete(src);
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }
    }
}
