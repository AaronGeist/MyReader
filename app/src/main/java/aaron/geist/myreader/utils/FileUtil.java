package aaron.geist.myreader.utils;

import java.io.File;

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

    public static void createFile(File file) {

    }

    public static void createFolder(File file) {

    }
}
