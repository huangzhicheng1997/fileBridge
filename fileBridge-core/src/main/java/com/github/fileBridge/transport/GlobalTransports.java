package com.github.fileBridge.transport;

import com.github.fileBridge.common.config.AgentConfig;
import com.github.fileBridge.common.config.OutputYml;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ZhiCheng
 * @date 2022/12/29 14:57
 */
public class GlobalTransports {

    private Map<String, TransportTableInfo.TransportInfo> connections = new ConcurrentHashMap<>();

    public GlobalTransports(AgentConfig config) {
        ExecutorService executorService = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("grpc-executor-%d")
                .build());

        var addresses = config.getOutput().values().stream().map(OutputYml::getAddresses).toList();
        for (String address : addresses) {
            String[] addressArray = address.split(",");
            for (String addr : addressArray) {
                String[] ipPort = addr.split(":");
                connections.put(addr, new TransportTableInfo.TransportInfo(new TransportClient(ipPort[0], Integer.parseInt(ipPort[1]), executorService)));
            }
        }

    }


    public TransportTableInfo attach(OutputYml outputYml) {
        var addresses = outputYml.getAddresses().split(",");
        TransportTableInfo transportTableInfo = new TransportTableInfo();
        for (String addr : addresses) {
            TransportTableInfo.TransportInfo transportInfo = connections.get(addr);
            if (null == transportInfo) {
                throw new IllegalArgumentException();
            }
            transportTableInfo.add(transportInfo);
        }
        return transportTableInfo;
    }
}
