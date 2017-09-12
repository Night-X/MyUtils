package cn.knightxie.myutils.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by xy on 17/9/1.
 */

public class DownloadUtils
{
    private final static String DATA = "data";

    /**
     * 获取资源下载存储路径。
     *
     * @param context
     * @param type
     * @return
     */
    public static String getSaveFilePath(Context context, String type)
    {
        String result = null;
        if (externalAvailable())
            result = getExternalStorageDirectory().getAbsolutePath();
        else if (internalAvailable())
            result = Environment.getDataDirectory()
                    .getAbsolutePath();
        if (result == null) return null;
        result += File.separator + context.getPackageManager()
                .getApplicationLabel(context.getApplicationInfo())
                + File.separator + DATA;
        if (type != null)
            result += File.separator + type;
        File folder = new File(result);
        if (!folder.exists())
            folder.mkdirs();
        return result;
    }

    private static boolean internalAvailable()
    {
        String filePath = Environment.getDataDirectory()
                .getAbsolutePath();
        return getAvailableSize(filePath) > 5;
    }


    private static boolean externalAvailable()
    {
        // 判断SD卡状态
        boolean result = TextUtils.equals(Environment.getExternalStorageState(),
                Environment.MEDIA_MOUNTED);
        String filePath = getExternalStorageDirectory()
                .getAbsolutePath();
        result = result && (getAvailableSize(filePath) > 5);
        return result;
    }

    /**
     * 获取剩余空间大小，单位：M
     *
     * @param filePath 路径
     * @return 该存储路径剩余大小
     */
    @SuppressWarnings("deprecation")
    private static int getAvailableSize(String filePath)
    {
        StatFs fs = new StatFs(filePath);
        long size;
        long available;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            size = fs.getBlockSizeLong();
            available = fs.getAvailableBlocksLong();
        }
        else
        {
            size = fs.getBlockSize();
            available = fs.getAvailableBlocks();
        }
        return (int) ((size * available) / 1024 / 1024);
    }
}
