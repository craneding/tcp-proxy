/**
 * 
 */
package com.dinghz.tcpproxy.http;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TcpWrite extends HttpServlet {
	private static final long serialVersionUID = -7461565608023272179L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//req.setCharacterEncoding("utf-8");
		
		//String tcpId = Util.getTcpId(req);

		String tcpId = req.getHeader("tcpid");
		
		ServletInputStream inputStream = req.getInputStream();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		do {
			int read = inputStream.read();
			
			if(read == -1)
				break;
		
			os.write(read);
		} while (true);
		os.close();
		
		byte[] tcpData = os.toByteArray();
		
		/*
		byte[] tcpData;
		try {
			tcpData = Util.getTcpDataByHex(req);
		} catch (DecoderException e) {
			throw new ServletException(e.getMessage());
		}
		*/
		
		//byte[] tcpData = Util.getTcpDataByBase64(req);
		
		TcpProxy tcpProxy = TcpCache.getTcpProxy(tcpId);
		
		tcpProxy.write(tcpData);
	}
}
