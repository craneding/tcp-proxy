package com.dinghz.tcpproxy.tcp.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache
 *
 * @author dinghz
 * @date 2019-04-21
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class Cache {
    public static final Map<Integer, TcpServer> SERVER_MAP = new ConcurrentHashMap<>();

    public static final Map<Integer, Boolean> STATUS_MAP = new ConcurrentHashMap<>();

    public static final Map<String, SessionInfo> SESSION_MAP = new ConcurrentHashMap<>();

}
