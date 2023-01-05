package com.github.fileBridge;

import com.github.fileBridge.common.Event;
import com.github.fileBridge.common.utils.FileUtil;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author ZhiCheng
 * @date 2022/12/24 20:43
 */
public class OffsetRecorder {

    private final boolean needCommit;

    private MappedByteBuffer mmap;

    private final long readOffset;

    private Unsafe unsafe;

    public OffsetRecorder(File file, String readStrategy) throws IOException, NoSuchFieldException, IllegalAccessException {
        this.needCommit = switch (readStrategy) {
            case "latest" -> {
                readOffset = file.length();
                yield false;
            }
            case "offset" -> {
                mmap = tryMmap(file);
                readOffset = mmap.position(0).getLong();
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                this.unsafe = (Unsafe) theUnsafe.get(Unsafe.class);
                yield true;
            }
            case "fromHead" -> {
                readOffset = 0;
                yield false;
            }
            default -> throw new IllegalStateException("Unexpected value: " + readStrategy);
        };

    }


    public long readOffset() {
        return readOffset;
    }

    public void commitOffset(Event event) {
        if (needCommit) {
            //todo 是否添加强制落盘？
            mmap.position(0).putLong(event.offset());
        }
    }

    /**
     * 映射一个文件用于存储所有日志文件的offset
     */
    private MappedByteBuffer tryMmap(File file) throws IOException {
        File offsetRecord = FileUtil.newFile(file.getParent() + "/offset", file.getName() + ".offset");
        FileChannel fileChannel = new RandomAccessFile(offsetRecord, "rw").getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, Long.BYTES);
    }

    public void release() {
        if (!needCommit) {
            return;
        }
        unsafe.invokeCleaner(mmap);
    }
}
