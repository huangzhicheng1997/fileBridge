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

    private final ByteBuffer readBuffer;

    //4kb
    public static final int readBufferSize = 4 * (1 << 10);

    private final FileChannel fileChannel;

    private long committedOffset;

    public Selector(File file) throws IOException {
        this(file, readBufferSize, 0, 0);
    }


    public Selector(File file, long seekFrom) throws IOException {
        this(file, readBufferSize, seekFrom, 0);
    }

    public Selector(File file, int readBufferSize, long seekFrom, long readTimes) throws IOException {
        if (readBufferSize <= 0) {
            throw new IllegalArgumentException("porterCap<=0 is Illegal");
        }
        this.readBuffer = ByteBuffer.allocate(readBufferSize);
        this.fileChannel = new RandomAccessFile(file, "r").getChannel();
        this.fileChannel.position(seekFrom);
        this.committedOffset = seekFrom;
    }


    public List<Line> selectLine(ByteBuf outputBuffer) throws IOException {
        long fileSize = fileChannel.size();
        //fileSize有时候为0?
        if (fileSize != 0 && fileChannel.position() != fileSize) {
            ByteBuffer sliceBuffer = readBuffer.slice();
            fileChannel.read(sliceBuffer);
            sliceBuffer.flip();
            outputBuffer.writeBytes(sliceBuffer);
        }
        List<Line> lines = new ArrayList<>();
        while (true) {
            int index = outputBuffer.forEachByte(outputBuffer.readerIndex(), outputBuffer.writerIndex() - outputBuffer.readerIndex(), ByteProcessor.FIND_LF);
            if (index == -1) {
                break;
            } else {
                this.committedOffset += index + 1;
                byte[] strBytes = new byte[index + 1];
                outputBuffer.readBytes(strBytes);
                outputBuffer.discardReadBytes();
                lines.add(new Line(new String(strBytes, StandardCharsets.UTF_8), committedOffset));
            }
        }
        return lines;
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

    public boolean isEOF() {
        try {
            return pos() == fileSize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long fileSize() throws IOException {
        return fileChannel.size();
    }

    public long getCommittedOffset() {
        return this.committedOffset;
    }

}

