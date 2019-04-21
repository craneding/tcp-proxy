package com.dinghz.tcpproxy.tcp.core;

import lombok.Data;

/**
 * SessionInfo
 *
 * @author dinghz
 * @date 2019-04-21
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Data
public class SessionInfo {

    /**
     * 编号
     */
    private String id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 远程IP
     */
    private String remoteIp;

    /**
     * 远程端口
     */
    private Integer remotePort;

    /**
     * 激活时间
     */
    private String activeTime;

    /**
     * 备注
     */
    private String remark;

}
