package com.github.fileBridge.transport;

import com.github.fileBridge.common.functions.NoResultFunc;
import com.github.fileBridge.common.logger.GlobalLogger;
import com.github.fileBridge.common.utils.CasBitSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhiCheng
 * @date 2022/11/29 10:21
 */
public class TransportTableInfo {
    private final List<TransportInfo> connections = new ArrayList<>();
    /**
     * 用bitset作为可用标识，确保List处于不可变模式。那么list则不会出现线程安全性问题，
     * 相比于List，bitset的线程安全更容易得到保障。
     */
    private final CasBitSet bitSet = new CasBitSet();
    private final AtomicInteger loopCount = new AtomicInteger(0);

    public TransportTableInfo() {

    }

    public void add(TransportInfo... transportInfos) {
        for (TransportInfo transportInfo : transportInfos) {
            connections.add(transportInfo);
            bitSet.set(connections.size() - 1, true);
        }
    }

    public boolean recoverAndThen(NoResultFunc func) {
        boolean success = false;
        for (int i = 0; i < connections.size(); i++) {
            if (!bitSet.get(i)) {
                TransportInfo transportInfo = connections.get(i);
                try {
                    boolean isHealth = true;
                    for (int j = 0; j < 3; j++) {
                        long startTime = System.currentTimeMillis();
                        boolean ret = transportInfo.transportClient.healthCheck();
                        long endTime = System.currentTimeMillis();
                        if (ret && endTime - startTime >= 200) {
                            isHealth = false;
                            break;
                        }
                    }
                    if (isHealth) {
                        bitSet.set(i, true);
                        success = true;
                        GlobalLogger.getLogger().info("recover success " + transportInfo.transportClient.ip + ":" + transportInfo.transportClient.port);
                    }
                } catch (Exception e) {
                    GlobalLogger.getLogger().warn("recover client  failed addr is " + transportInfo.transportClient.ip + ":" + transportInfo.transportClient.port, e.getMessage());
                }
            }
        }
        if (success) {
            func.invoke();
        }
        return success;
    }

    public void recoverIfNecessary() {
        for (int i = 0; i < connections.size(); i++) {
            if (!bitSet.get(i)) {
                try {
                    boolean isHealth = true;
                    TransportInfo transportInfo = connections.get(i);
                    for (int j = 0; j < 3; j++) {
                        long startTime = System.currentTimeMillis();
                        boolean ret = transportInfo.transportClient.healthCheck();
                        long endTime = System.currentTimeMillis();
                        if (ret && endTime - startTime >= 200) {
                            isHealth = false;
                            break;
                        }
                    }
                    if (isHealth) {
                        bitSet.set(i, true);
                        GlobalLogger.getLogger().info("recover success " + transportInfo.transportClient.ip + ":" + transportInfo.transportClient.port);
                    }
                } catch (Exception e) {
                    GlobalLogger.getLogger().warn("recover client  failed", e);
                }
            }
        }
    }

    public TransportClient next() {
        int index = nextIdx();
        if (bitSet.get(index)) {
            return connections.get(index).transportClient;
        }
        return next(index);
    }

    private TransportClient next(int startIdx) {
        int index = nextIdx();
        if (bitSet.get(index)) {
            return connections.get(index).transportClient;
        }
        //所有的客户端查完了一遍发现所有的客户端不可用此时返回null
        if (index == startIdx) {
            return null;
        }
        return next(startIdx);
    }

    private int nextIdx() {
        int step = loopCount.getAndIncrement();
        if (loopCount.get() < 0) {
            loopCount.set(0);
        }
        return step % connections.size();
    }

    public boolean reportFailed(TransportClient transportClient) {
        boolean isBreak = false;
        for (int i = 0; i < connections.size(); i++) {
            TransportInfo transportInfo = connections.get(i);
            if (transportInfo.transportClient == transportClient) {
                if (System.currentTimeMillis() - transportInfo.metrics.lastUpdateTime < 3000) {
                    //三秒钟内失败三次
                    if (transportInfo.metrics.numberOfFailures + 1 >= 3) {
                        bitSet.set(i, false);
                        isBreak = true;
                    }
                    transportInfo.metrics = new Metrics(transportInfo.metrics.lastUpdateTime, transportInfo.metrics.numberOfFailures + 1);
                } else {
                    transportInfo.metrics = new Metrics(System.currentTimeMillis(), 1);
                }
            }
        }
        return isBreak;
    }


    public void closeConnections() {
        for (TransportInfo connection : connections) {
            connection.transportClient.shutdown();
        }
    }


    static class TransportInfo {
        private final TransportClient transportClient;
        private Metrics metrics;

        public TransportInfo(TransportClient transportClient) {
            this.transportClient = transportClient;
            this.metrics = new Metrics(System.currentTimeMillis(), 0);
        }
    }

    record Metrics(Long lastUpdateTime, int numberOfFailures) {

    }


}
