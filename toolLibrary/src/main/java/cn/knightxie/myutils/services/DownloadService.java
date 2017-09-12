package cn.knightxie.myutils.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.knightxie.myutils.utils.LogUtils;
import cn.knightxie.myutils.utils.OkHttpUtils;
import okhttp3.Request;


/**
 * Created by wt on 17/7/24.
 * 以后可以做成前台服务下载。
 */

public class DownloadService
        extends Service
{
    private final static String TAG = "DownloadService";
    // 启动下载服务的action
    public final static String ACTION_DOWNLOAD = "android.intent.action.DOWNLOAD";
    public final static String EXTRA_URL = "url";
    public final static String EXTRA_FILE_DIR = "fileDir";
    public final static String EXTRA_FILE_NAME = "fileName";
    public final static String EXTRA_OFFSET = "offset";

    public final static String ACTION_UPDATE_PROGRESS = "picsjoin.intent.action.UPDATE_PROGRESS";
    public final static String EXTRA_DOWNLOAD_PROGRESS = "progress";
    public final static String EXTRA_DOWNLOAD_STATE = "state";
    /**
     * 新建线程池。
     */
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(5);
    /**
     * 服务重启intent为null，计数
     */
    private int mRestartCount = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
        {
            final String url = intent.getStringExtra(EXTRA_URL);
            final String fileDir = intent.getStringExtra(EXTRA_FILE_DIR);
            final String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
            final long offset = intent.getLongExtra(EXTRA_OFFSET, 0);
            mExecutorService.submit(new DownloadTask(this, url, fileDir, fileName, offset));
        }
        else
        {
            if (++mRestartCount < 4)
                return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private class DownloadTask
            implements Runnable, OkHttpUtils.ProgressListener, OkHttpUtils.ResponseCallback
    {
        private WeakReference<Context> contextWeakReference;
        private final String url;
        private final String fileDir;
        private final String fileName;
        private final long offset;

        DownloadTask(Context context, String url, String fileDir, String fileName, long offset)
        {
            this.contextWeakReference = new WeakReference<>(context);
            this.url = url;
            this.fileDir = fileDir;
            this.fileName = fileName;
            this.offset = offset;
        }

        @Override
        public void run()
        {
            OkHttpUtils.download(url, fileDir, fileName, offset, this, this);
        }

        @Override
        public void update(long byteRead, long contentLength, boolean done)
        {
            LogUtils.i(TAG, "byteRead = " + byteRead + ", contentLength = " + contentLength);
            Intent intent = new Intent(ACTION_UPDATE_PROGRESS);
            intent.setPackage(contextWeakReference.get()
                    .getPackageName());
            // 用url作为广播的判断依据。
            intent.putExtra(EXTRA_URL, url);
            int progress;
            if (contentLength == 0)
                progress = 100;
            else
                progress = (int) (byteRead * 100 / contentLength);
            intent.putExtra(EXTRA_DOWNLOAD_PROGRESS, progress);
            intent.putExtra(EXTRA_DOWNLOAD_STATE, done);
            intent.putExtra(EXTRA_FILE_DIR, fileDir);
            intent.putExtra(EXTRA_FILE_NAME, fileName);
            LocalBroadcastManager.getInstance(contextWeakReference.get())
                    .sendBroadcast(intent);
        }

        @Override
        public void onFailure(Request request, Exception e)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(DownloadService.this, "download failed, retry",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });
            Intent intent = new Intent(ACTION_UPDATE_PROGRESS);
            intent.setPackage(contextWeakReference.get()
                    .getPackageName());
            // 用url作为广播的判断依据。
            intent.putExtra(EXTRA_URL, url);
            // Added by xy on 17/9/5.将progress = -1作为下载失败的判断依据.
            intent.putExtra(EXTRA_DOWNLOAD_PROGRESS, -1);
            LocalBroadcastManager.getInstance(contextWeakReference.get())
                    .sendBroadcast(intent);
        }

        @Override
        public void onResponse(String response)
        {

        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
