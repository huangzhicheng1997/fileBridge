import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author ZhiCheng
 * @date 2023/1/11 14:50
 */
public class LogGen {

    static int counter = 0;

    static int logCounter = 0;

    static File file = new File("/Users/huangzhicheng/logs/rocketmqlogs/test.log");
    static FileChannel channel;

    static {
        try {
            file.createNewFile();
            channel = new RandomAccessFile(file, "rw").getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        var log = """
                %s INFO StoreStatsService - %s\n                  
                """;

        for (; ; ) {
            if (channel.size() >= 1 << 20) {
                channel.close();
                File newFile = new File("/Users/huangzhicheng/logs/rocketmqlogs/test." + logCounter++ + ".log");
                LogGen.file.renameTo(newFile);
                file = new File("/Users/huangzhicheng/logs/rocketmqlogs/test.log");
                file.createNewFile();
                channel = new RandomAccessFile(file, "rw").getChannel();
            }
            String line = String.format(log, DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(LocalDateTime.now(ZoneOffset.of("+8"))), ++counter);
            channel.write(ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8)));
            Thread.sleep(1);
        }


    }

}
