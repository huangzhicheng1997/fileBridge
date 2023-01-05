package com.github.fileBridge.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author ZhiCheng
 * @date 2022/12/29 18:08
 */
public class HashUtil {
    public static String MD5(String data) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(data.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
