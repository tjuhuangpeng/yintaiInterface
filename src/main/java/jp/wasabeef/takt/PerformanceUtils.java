package jp.wasabeef.takt;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class PerformanceUtils {
    private static final String TAG = "PerformanceUtils";
    private static long sTotalMemo = 0;
    private PerformanceUtils() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static long getFreeMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / 1024;
    }

    public static long getTotalMemory() {
        if (sTotalMemo == 0) {
            String str1 = "/proc/meminfo";
            String str2;
            String[] arrayOfString;
            long initial_memory = -1;
            FileReader localFileReader = null;
            try {
                localFileReader = new FileReader(str1);
                BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
                str2 = localBufferedReader.readLine();

                if (str2 != null) {
                    arrayOfString = str2.split("\\s+");
                    initial_memory = Integer.valueOf(arrayOfString[1]);
                }
                localBufferedReader.close();

            } catch (IOException e) {
                Log.e(TAG, "getTotalMemory exception = ", e);
            } finally {
                if (localFileReader != null) {
                    try {
                        localFileReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close localFileReader exception = ", e);
                    }
                }
            }
            sTotalMemo = initial_memory;
        }
        return sTotalMemo;
    }
}