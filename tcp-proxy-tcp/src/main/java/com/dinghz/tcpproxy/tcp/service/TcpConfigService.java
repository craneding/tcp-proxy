package com.dinghz.tcpproxy.tcp.service;

import com.dinghz.tcpproxy.tcp.core.Cache;
import com.dinghz.tcpproxy.tcp.core.TcpServer;
import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import com.dinghz.tcpproxy.tcp.repository.TcpConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;

/**
 * TcpConfigService
 *
 * @author dinghz
 * @date 2019-04-20
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
@Service
public class TcpConfigService {

    @Autowired
    private TcpConfigRepository configRepository;

    @PostConstruct
    public void init() {
        CompletableFuture.runAsync(() -> {
            for (TcpConfig tcpConfig : configRepository.findAll()) {
                TcpServer tcpServer = new TcpServer(tcpConfig);
                tcpServer.start();

                Cache.SERVER_MAP.put(tcpConfig.getId(), tcpServer);
            }
        });
    }

    public boolean startService(TcpConfig tcpConfig) {
        Boolean starting = Cache.STATUS_MAP.get(tcpConfig.getId());
        if (starting != null) {
            if (starting) {
                return true;
            } else {
                if (!Cache.SERVER_MAP.get(tcpConfig.getId()).start()) {
                    return false;
                }
            }
        }

        final TcpServer tcpServer = new TcpServer(tcpConfig);

        if (!tcpServer.start()) {
            Cache.SERVER_MAP.put(tcpConfig.getId(), tcpServer);
            Cache.STATUS_MAP.put(tcpConfig.getId(), false);

            return false;
        } else {
            Cache.SERVER_MAP.put(tcpConfig.getId(), tcpServer);
            Cache.STATUS_MAP.put(tcpConfig.getId(), true);

            return true;
        }
    }

    public boolean stopService(TcpConfig tcpConfig) {
        if (!Cache.SERVER_MAP.containsKey(tcpConfig.getId())) {
            return false;
        }

        Cache.SERVER_MAP.get(tcpConfig.getId()).stop();
        Cache.STATUS_MAP.remove(tcpConfig.getId());
        Cache.SERVER_MAP.remove(tcpConfig.getId());

        return true;
    }

    @PreDestroy
    public void destory() {
        for (TcpServer server : Cache.SERVER_MAP.values()) {
            server.stop();
        }

        Cache.SERVER_MAP.clear();
        Cache.STATUS_MAP.clear();
    }
}
