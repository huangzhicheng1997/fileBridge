package com.github.fileBridge.event;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Selector {

    public record Line(String content, long offset) {
    }

    private final ByteBuffer porter;

    public static final int porterCap = 256;

    private final FileChannel fileChannel;

    private final File file;

    private long committedOffset;

    public Selector(File file) throws IOException {
        this(file, porterCap, 0, 0);
    }


    public Selector(File file, long seekFrom) throws IOException {
        this(file, porterCap, seekFrom, 0);
    }

    public Selector(File file, int porterCap, long seekFrom, long readTimes) throws IOException {
        if (porterCap <= 0) {
            throw new IllegalArgumentException("porterCap<=0 is Illegal");
        }
        this.file = file;
        this.porter = ByteBuffer.allocate(porterCap);
        this.fileChannel = new RandomAccessFile(file, "r").getChannel();
        this.fileChannel.position(seekFrom);
        this.committedOffset = seekFrom;
    }


    public List<Line> selectLine(ByteBuf buffer) throws IOException {
        long fileSize = fileChannel.size();
        //fileSize有时候为0?
        if (fileSize != 0 && fileChannel.position() != fileSize) {
            ByteBuffer sliceBuffer = porter.slice();
            fileChannel.read(sliceBuffer);
            sliceBuffer.flip();
            buffer.writeBytes(sliceBuffer);
        }
        List<Line> lines = new ArrayList<>();
        while (true) {
            int index = buffer.forEachByte(buffer.readerIndex(), buffer.writerIndex() - buffer.readerIndex(), ByteProcessor.FIND_LF);
            if (index == -1) {
                break;
            } else {
                this.committedOffset += index + 1;
                byte[] strBytes = new byte[index + 1];
                buffer.readBytes(strBytes);
                buffer.discardReadBytes();
                lines.add(new Line(new String(strBytes, StandardCharsets.UTF_8), committedOffset));
            }
        }
        return lines;
    }

    public String getFilePath() {
        return file.getPath();
    }

    public long pos() throws IOException {
        return this.fileChannel.position();
    }

    public long nextPos() throws IOException {
        return this.fileChannel.position();
    }

    public void close() throws IOException {
        this.fileChannel.close();
    }

    public boolean isEOF() throws IOException {
        return pos() == file.length();
    }

    public long getCommittedOffset() {
        return this.committedOffset;
    }

}

