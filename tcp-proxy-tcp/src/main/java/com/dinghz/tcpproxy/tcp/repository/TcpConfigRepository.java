package com.dinghz.tcpproxy.tcp.repository;

import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * TcpConfigRepository
 *
 * @author dinghz
 * @date 2019-04-20
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public interface TcpConfigRepository extends JpaRepository<TcpConfig, Integer> {

    boolean existsByLocalPort(Integer localPort);

    boolean existsByBaseUrlAndRemoteHostAndRemotePort(String baseUrl, String remoteHost, Integer remotePort);

}
