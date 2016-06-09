/**
 * 
 */
package com.dinghz.tcpproxy.http;

import com.dinghz.tcpproxy.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TcpRegister extends HttpServlet {
	private static final long serialVersionUID = -8421614927735092760L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String tcpId = Util.getTcpId(req);
		String tcpHost = Util.getTcpHost(req);
		int tcpPort = Util.getTcpPort(req);
		
		TcpProxy tcpProxy = new TcpProxy(tcpHost, tcpPort);
		
		tcpProxy.connect();
		
		TcpCache.newTcpProxy(tcpId, tcpProxy);
	}
	
}
