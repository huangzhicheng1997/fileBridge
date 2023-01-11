package com.github.fileBridge.common.utils;

import com.github.fileBridge.common.logger.GlobalLogger;
import io.netty.buffer.ByteBufInputStream;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
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
            var MD5 = MessageDigest.getInstance("MD5");
            var digest = MD5.digest(data.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            GlobalLogger.getLogger().error("error", e);
        }
        return null;
    }

    public static String MD5(File file, long offset, long length) {
        try (var readChannel = new FileInputStream(file).getChannel()) {
            if (length < 0) {
                throw new IllegalArgumentException();
            }
            readChannel.position(offset);
            var MD5 = MessageDigest.getInstance("MD5");
            var buffer = ByteBuffer.allocate(128);
            while (readChannel.position() < length) {
                readChannel.read(buffer);
                buffer.flip();
                MD5.update(buffer.array(), 0, buffer.limit());
                buffer.clear();
            }
            byte[] digest = MD5.digest();
            return new BigInteger(1, digest).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            GlobalLogger.getLogger().error("error", e);
        }
        return null;
    }

    /*
     * 计算日志文件的hash。
     * 计算hash的方式是读取第一行日志，然后对其进行hash。
     * 依据是：日志数据基本上包含"时间"那么可以保证每个日志文件开头第一条日志肯定都是不一样的，
     * 依据这个特性可以保证不同的日志文件拥有不同的唯一hash
     */
    public static String logHash(File logFile) throws IOException {
        if (logFile.length() == 0 || !logFile.exists()) {
            throw new FileNotFoundException(logFile.getAbsolutePath());
        }
        try (var reader = new BufferedReader(new FileReader(logFile))) {
            String line = reader.readLine();
            var MD5 = MessageDigest.getInstance("MD5");
            var digest = MD5.digest(line.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
