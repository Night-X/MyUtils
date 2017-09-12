package cn.knightxie.myutils.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.knightxie.myutils.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by xy on 17/7/17.
 * OkHttp Utils
 */

public class OkHttpUtils
{
    private final static String TAG = "OKHttpUtils";

    /**
     * 初始化OkHttpClient，设置超时时间
     *
     * @param listener 下载进度 {@link ProgressResponseBody}
     * @return OkHttpClient single instance
     * <p>
     * 如果OkHttpClient需要添加其他参数，可以用
     * {@link OkHttpClient#newBuilder()}添加参数信息，
     * 再用{@link OkHttpClient.Builder#build()}获取实例对象
     */
    public static OkHttpClient getOkHttpClient(final ProgressListener listener)
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS);

        if (listener != null)
            builder.addNetworkInterceptor(new Interceptor()
            {
                @Override
                public Response intercept(Chain chain) throws IOException
                {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), listener))
                            .build();
                }
            });
        return builder.build();
    }

    /**
     * 同步get请求
     *
     * @param url url string
     * @return response
     * @throws IOException
     */
    public static Response getSync(String url) throws IOException
    {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = getOkHttpClient(null).newCall(request);
        return call.execute();
    }

    /**
     * 同步get请求
     *
     * @param url url string
     * @return response body
     * @throws IOException
     */
    public static String getSyncString(String url) throws IOException
    {
        ResponseBody body = getSync(url).body();
        if (body != null)
            return body.string();
        return "";
    }

    /**
     * 异步get请求
     *
     * @param url      url string
     * @param callback response callback
     */
    public static void getAsync(String url, final ResponseCallback callback)
    {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        getOkHttpClient(null).newCall(request)
                .enqueue(new CallbackImpl(request, callback));
    }

    /**
     * 同步post请求
     *
     * @param url    url string
     * @param params 携带参数
     * @return response
     * @throws IOException
     */
    public static Response postSync(String url, Map<String, String> params) throws IOException
    {
        Request request = new Request.Builder().url(url)
                .post(buildRequestBody(params))
                .build();
        return getOkHttpClient(null).newCall(request)
                .execute();
    }

    /**
     * 同步post请求
     *
     * @param url    url string
     * @param params 携带参数
     * @return response string
     * @throws IOException
     */
    public static String postSyncString(String url, Map<String, String> params) throws IOException
    {
        ResponseBody body = postSync(url, params).body();
        if (body != null)
            return body.string();
        return "";
    }

    /**
     * 异步post请求
     *
     * @param url      url string
     * @param params   携带参数
     * @param callback response callback
     */
    public static void postAsync(String url, Map<String, String> params, ResponseCallback callback)
    {
        final Request request = new Request.Builder().url(url)
                .post(buildRequestBody(params))
                .build();
        getOkHttpClient(null).newCall(request)
                .enqueue(new CallbackImpl(request, callback));
    }

    /**
     * 异步get请求下载，文件名为url的MD5散列值加密后的字符串
     *
     * @param url      url string
     * @param fileDir  下载存储路径
     * @param listener 下载进度监听
     * @param callback response callback
     */
    public static void download(String url, String fileDir, final ProgressListener listener,
            final ResponseCallback callback)
    {
        download(url, fileDir, null, 0, listener, callback);
    }

    /**
     * Modify by xy on 17/9/5.
     * 异步get请求下载，文件名自定义，当fileName为空时（包括null和""），以url的MD5散列值作为文件名
     *
     * @param url      url string
     * @param fileDir  下载存储路径
     * @param fileName 自定义下载文件名
     * @param listener 下载进度监听
     * @param callback response callback
     */
    public static void download(String url, String fileDir, String fileName,
            final ProgressListener listener,
            final ResponseCallback callback)
    {
        download(url, fileDir, fileName, 0, listener, callback);
    }

    /**
     * 断点续传。
     * Modify by xy on 17/9/5.
     * 文件名自定义，当fileName为空时（包括null和""），以url的MD5散列值作为文件名
     *
     * @param url      url string
     * @param fileDir  下载存储路径
     * @param fileName 自定义下载文件名
     * @param offset   下载偏移值，即已下载大小
     * @param listener 下载进度监听
     * @param callback response callback
     */
    public static void download(String url, String fileDir, String fileName, long offset,
            final ProgressListener listener,
            final ResponseCallback callback)
    {
        Headers headers = Headers.of("Range", "bytes=" + String.valueOf(offset) + "-");
        final Request request = new Request.Builder().url(url)
                .headers(headers)
                .build();
        getOkHttpClient(listener).newCall(request)
                .enqueue(new DownloadCallbackImpl(request, callback, url, fileDir, fileName));
    }

    private static RequestBody buildRequestBody(Map<String, String> params)
    {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> stringStringEntry : params.entrySet())
        {
            builder.add(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        return builder.build();
    }


    /**
     * 实现下载进度方案
     * {@code https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java}
     */
    private static class ProgressResponseBody
            extends ResponseBody
    {
        private ResponseBody responseBody;
        private ProgressListener progressListener;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody,
                ProgressListener progressListener)
        {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Nullable
        @Override
        public MediaType contentType()
        {
            return responseBody.contentType();
        }

        @Override
        public long contentLength()
        {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source()
        {
            if (bufferedSource == null)
                bufferedSource = Okio.buffer(source(responseBody.source()));
            return bufferedSource;
        }

        private Source source(Source source)
        {
            return new ForwardingSource(source)
            {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException
                {
                    final long bytesRead = super.read(sink, byteCount);
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, contentLength(),
                            bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    public interface ResponseCallback
    {
        void onFailure(Request request, Exception e);

        void onResponse(String response);
    }

    public interface ProgressListener
    {
        void update(long byteRead, long contentLength, boolean done);
    }

    private static class CallbackImpl
            implements Callback
    {
        private Request mRequest;
        ResponseCallback mCallback;

        CallbackImpl(Request request, ResponseCallback callback)
        {
            this.mRequest = request;
            this.mCallback = callback;
        }

        @Override
        public void onFailure(Call call, final IOException e)
        {
            if (mCallback != null)
            {
                mCallback.onFailure(mRequest, e);
            }
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException
        {
            if (mCallback != null)
            {
                try
                {
                    mCallback.onResponse(response.body()
                            .string());
                }
                catch (Exception e)
                {
                    mCallback.onFailure(response.request(), e);
                }
            }
        }
    }

    /**
     * 将下载的文件以url的hash key作为文件名保存至fileDir
     */
    private static class DownloadCallbackImpl
            extends CallbackImpl
    {
        final private String mUrl;
        final private String mFileDir;
        private String mFileName;

        DownloadCallbackImpl(Request request,
                ResponseCallback callback, String url, String fileDir, String fileName)
        {
            super(request, callback);
            mUrl = url;
            mFileDir = fileDir;
            mFileName = fileName;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException
        {
            if (TextUtils.isEmpty(mFileName))
                mFileName = StringUtils.hashKeyForExternal(mUrl);
            if (TextUtils.isEmpty(mFileDir))
            {
                onFailure(call, new IOException("Save path does not exist"));
                return;
            }
            File folder = new File(mFileDir);
            if (folder.exists() || folder.mkdirs())
            {
                File file = new File(mFileDir, mFileName);
                ResponseBody body = response.body();
                if (body != null)
                {
                    InputStream inputStream = body.byteStream();
                    FileUtils.saveStreamToFile(inputStream, file, body.contentLength());
                }
            }
        }
    }

    /**
     * 检查网络连接状态
     *
     * @param context
     */
    public static boolean checkConnection(Context context)
    {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting())
        {
            Toast.makeText(context, R.string.no_network_connection_toast, Toast.LENGTH_LONG)
                    .show();
            LogUtils.e(TAG, "checkConnection - no connection found");
            return false;
        }
        return true;
    }
}
