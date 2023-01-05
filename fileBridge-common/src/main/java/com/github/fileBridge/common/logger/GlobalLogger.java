package com.github.fileBridge.common.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import com.github.fileBridge.common.config.AgentConfig;
import com.github.fileBridge.common.utils.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author ZhiCheng
 * @date 2022/11/1 10:30
 */
public class GlobalLogger {
    private static boolean isInit = false;

    private Logger logger;

    public void init(AgentConfig config) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ROOT");
        Context context = (Context) LoggerFactory.getILoggerFactory();
        logger = root;
        if (config.getLoglevel().equalsIgnoreCase("info")) {
            logger.setLevel(Level.INFO);
        } else if (config.getLoglevel().equalsIgnoreCase("debug")) {
            logger.setLevel(Level.DEBUG);
        } else if (config.getLoglevel().equalsIgnoreCase("error")) {
            logger.setLevel(Level.ERROR);
        } else {
            logger.setLevel(Level.INFO);
        }

        logger.setAdditive(false);

        String logDir = config.getLogDir();
        String logFile;
        if (logDir.endsWith(File.separator)) {
            logFile = logDir + "agent.log";
        } else {
            logFile = logDir + File.separator + "agent.log";
        }
        if (StringUtils.isBlank(logDir)) {
            return;
        }

        AgentLogLayout agentLogLayout = new AgentLogLayout();
        agentLogLayout.start();
        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) logger.getAppender("console");
        consoleAppender.setLayout(agentLogLayout);

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile(logFile);
        fileAppender.setContext(context);
        fileAppender.setAppend(true);
        fileAppender.setName("FILE");
        fileAppender.setLayout(agentLogLayout);
        fileAppender.start();
        logger.addAppender(fileAppender);

    }

    private enum Singleton {
        INSTANCE;
        final GlobalLogger globalLogger = new GlobalLogger();
    }

    public static void initSingleton(AgentConfig config) {
        if (isInit) {
            return;
        }
        Singleton.INSTANCE.globalLogger.init(config);
        isInit = true;
    }

    public static Logger getLogger() {
        //如果必要的话，未来可以返回代理对象对logger功能增强
        return Singleton.INSTANCE.globalLogger.logger;
    }
}
