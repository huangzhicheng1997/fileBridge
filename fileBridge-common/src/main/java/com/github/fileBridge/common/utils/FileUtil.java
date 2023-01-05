package com.github.fileBridge.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ZhiCheng
 * @date 2022/9/27 17:12
 */
public class FileUtil {
    public static File newFile(String path) {
        return new File(path);
    }

    public static File newFile(String dir, String fileName) throws IOException {
        mkDir(newFile(dir));
        if (dir.endsWith(File.separator)) {
            return newFile(dir + fileName);
        }
        return newFile(dir + File.separator + fileName);
    }

    public static void mkDir(File dir) throws IOException {
        if (!dir.exists()) {
            newFile(dir.getAbsolutePath()).mkdir();
        }
    }

    public static int read(InputStream input, byte[] buffer) throws IOException {
        return read(input, buffer, 0, buffer.length);
    }

    public static int read(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        } else {
            int remaining;
            int count;
            for (remaining = length; remaining > 0; remaining -= count) {
                int location = length - remaining;
                count = input.read(buffer, offset + location, remaining);
                if (-1 == count) {
                    break;
                }
            }
            return length - remaining;
        }
    }




}
