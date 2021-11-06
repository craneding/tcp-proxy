package com.dinghz.tcpproxy.domain;

/**
 * WriteRequest
 *
 * @author dinghz
 * @date 2021/11/6
 * @company 广州车小Y科技有限公司
 * @email dinghz@gzyztd.com
 */
public class WriteRequest {

    private String tcpId;
    private byte[] body;

    public String getTcpId() {
        return tcpId;
    }

    public void setTcpId(String tcpId) {
        this.tcpId = tcpId;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
