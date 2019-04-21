package com.dinghz.tcpproxy.tcp.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * TcpConfig
 *
 * @author dinghz
 * @date 2018/6/20
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Data
@Entity
@Table(name = "tcp_config")
public class TcpConfig {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Basic
    @Column(name = "local_port", nullable = false)
    private Integer localPort;

    @Basic
    @Column(name = "remote_host", nullable = false, length = 64)
    private String remoteHost;

    @Basic
    @Column(name = "remote_port", nullable = false)
    private Integer remotePort;

    @Basic
    @Column(name = "base_url", nullable = false, length = 128)
    private String baseUrl;

}
