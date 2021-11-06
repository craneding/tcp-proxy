package com.dinghz.tcpproxy.domain;

/**
 * RegisterResponse
 *
 * @author dinghz
 * @date 2021/11/6
 * @company 广州车小Y科技有限公司
 * @email dinghz@gzyztd.com
 */
public class RegisterResponse {

    private String code;

    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
