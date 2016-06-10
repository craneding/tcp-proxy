/**
 * 
 */
package com.dinghz.tcpproxy.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpCache {

	static final Map<String, TcpProxy> tcpProxys = new ConcurrentHashMap<>();
	
	public static void newTcpProxy(String tcpid, TcpProxy tcpProxy) throws IOException {
		removeTcpProxy(tcpid);
		
		tcpProxys.put(tcpid, tcpProxy);
	}
	
	public static TcpProxy getTcpProxy(String tcpid) throws IOException {
		return tcpProxys.get(tcpid);
	}
	
	public static void removeTcpProxy(String tcpid) throws IOException {
		TcpProxy oldTcpProxy = tcpProxys.remove(tcpid);
		
		if(oldTcpProxy != null) {
			oldTcpProxy.disconnect();
		}
	}

}
