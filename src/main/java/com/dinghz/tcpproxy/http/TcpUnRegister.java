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

public class TcpUnRegister extends HttpServlet {
	private static final long serialVersionUID = 102635370400698962L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String tcpId = Util.getTcpId(req);
		
		TcpCache.removeTcpProxy(tcpId);
	}
	
}
