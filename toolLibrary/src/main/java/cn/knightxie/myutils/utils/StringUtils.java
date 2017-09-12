package cn.knightxie.myutils.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by xy on 17/7/14.
 */

public class StringUtils
{
    /**
     * 为url加密
     *
     * @param key
     * @return
     */
    public static String hashKeyForExternal(String key)
    {
        String cacheKey;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.trim()
                    .getBytes());
            cacheKey = bytesToHexString(digest.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes)
        {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1)
                hex = "0";
            builder.append(hex);
        }
        return builder.toString();
    }
}
