package com.dinghz.tcpproxy.tcp.ctrl;

import com.dinghz.tcpproxy.tcp.core.Cache;
import com.dinghz.tcpproxy.tcp.core.SessionInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * SessionController
 *
 * @author dinghz
 * @date 2019-04-21
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@RestController
@RequestMapping("sessions")
public class SessionController {

    @GetMapping
    public List<SessionInfo> list() {
        return new ArrayList<>(Cache.SESSION_MAP.values());
    }

}
