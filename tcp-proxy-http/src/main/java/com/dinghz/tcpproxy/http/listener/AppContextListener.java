package com.dinghz.tcpproxy.http.listener;

import com.dinghz.tcpproxy.http.tcpclient.TcpClient;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * AppContextListener
 *
 * @author dinghz
 * @date 2018/6/21
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        TcpClient.instance().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        TcpClient.instance().stop();
    }

}
