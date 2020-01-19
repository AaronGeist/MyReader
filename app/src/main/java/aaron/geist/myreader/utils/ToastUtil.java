package aaron.geist.myreader.utils;

import android.widget.Toast;

import aaron.geist.myreader.activity.MainActivity;


public class ToastUtil {

    public static void toastShort(String msg) {
        Toast.makeText(MainActivity.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(String msg) {
        Toast.makeText(MainActivity.getContext(), msg, Toast.LENGTH_LONG).show();
    }
}
