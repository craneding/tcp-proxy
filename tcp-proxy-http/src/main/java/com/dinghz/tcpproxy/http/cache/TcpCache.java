/**
 *
 */
package com.dinghz.tcpproxy.http.cache;

import io.netty.channel.ChannelFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpCache {

    static final Map<String, ChannelFuture> tcpProxys = new ConcurrentHashMap<>();

    public static void newTcpProxy(String tcpid, ChannelFuture channelFuture) {
        removeTcpProxy(tcpid);

        tcpProxys.put(tcpid, channelFuture);
    }

    public static ChannelFuture getTcpProxy(String tcpid) {
        return tcpProxys.get(tcpid);
    }

    public static void removeTcpProxy(String tcpid) {
        ChannelFuture channelFuture = tcpProxys.remove(tcpid);

        if (channelFuture != null && channelFuture.channel().isOpen()) {
            channelFuture.channel().close();
        }
    }

}
