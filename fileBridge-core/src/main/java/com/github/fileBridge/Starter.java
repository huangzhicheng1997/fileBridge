package com.github.fileBridge;


import com.github.fileBridge.common.logger.GlobalLogger;
import sun.misc.Signal;

import java.io.IOException;

/**
 * @author ZhiCheng
 * @date 2022/9/28 11:45
 */
public class Starter {


    public static void main(String[] args) throws IOException {
        BootLoader bootLoader = new BootLoader();
        bootLoader.start();
        GlobalLogger.getLogger().info("fileBridge started");
    }


}
