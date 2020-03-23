package com.dinghz.tcpproxy.nat.core.cmd;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CmdConnect
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CmdConnect extends AbstractCmd {

    private String realHost;
    private Integer realPort;

}
