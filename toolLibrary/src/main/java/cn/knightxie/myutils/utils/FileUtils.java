package cn.knightxie.myutils.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by xy on 17/7/18.
 *
 */

public class FileUtils
{
    public static void saveStreamToFile(InputStream inputStream, File file, long size)
    {
        RandomAccessFile randomAccessFile = null;
        FileChannel channelOut = null;
        byte[] bytes = new byte[2048];
        int length;
        try
        {
            randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "rwd");
            channelOut = randomAccessFile.getChannel();
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, file.length(), size);
            randomAccessFile.seek(file.length());
            while ((length = inputStream.read(bytes)) != -1)
            {
                randomAccessFile.write(bytes, 0, length);
                mappedBuffer.put(bytes, 0, length);
            }
        }
        catch (IOException e)
        {
            try
            {
                if (inputStream != null)
                    inputStream.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            try
            {
                if (channelOut != null)
                    channelOut.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            try
            {
                if (randomAccessFile != null)
                    randomAccessFile.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }
}
