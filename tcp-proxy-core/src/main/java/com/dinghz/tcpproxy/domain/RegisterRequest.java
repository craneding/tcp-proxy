package com.dinghz.tcpproxy.domain;

/**
 * RegisterRequest
 *
 * @author dinghz
 * @date 2021/11/6
 * @company 广州车小Y科技有限公司
 * @email dinghz@gzyztd.com
 */
public class RegisterRequest {

    private String tcpId;
    private String tcpHost;
    private Integer tcpPort;
    private String tcpIp;
    private String tcpUser;
    private String tcpPasswd;

    public String getTcpId() {
        return tcpId;
    }

    public void setTcpId(String tcpId) {
        this.tcpId = tcpId;
    }

    public String getTcpHost() {
        return tcpHost;
    }

    public void setTcpHost(String tcpHost) {
        this.tcpHost = tcpHost;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getTcpIp() {
        return tcpIp;
    }

    public void setTcpIp(String tcpIp) {
        this.tcpIp = tcpIp;
    }

    public String getTcpUser() {
        return tcpUser;
    }

    public void setTcpUser(String tcpUser) {
        this.tcpUser = tcpUser;
    }

    public String getTcpPasswd() {
        return tcpPasswd;
    }

    public void setTcpPasswd(String tcpPasswd) {
        this.tcpPasswd = tcpPasswd;
    }
}
