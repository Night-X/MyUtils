package cn.knightxie.myutils.utils;

import android.util.Log;

import static cn.knightxie.myutils.BuildConfig.LOGENABLE;

/**
 * Created by xy on 17/7/14.
 *
 */

public class LogUtils
{
    public static void v(String tag, String message)
    {
        if (LOGENABLE) Log.v(tag, message);
    }

    public static void i(String tag, String message)
    {
        if (LOGENABLE) Log.i(tag, message);
    }

    public static void d(String tag, String message)
    {
        if (LOGENABLE) Log.d(tag, message);
    }

    public static void w(String tag, String message)
    {
        if (LOGENABLE) Log.w(tag, message);
    }

    public static void e(String tag, String message)
    {
        if (LOGENABLE) Log.e(tag, message);
    }
}
