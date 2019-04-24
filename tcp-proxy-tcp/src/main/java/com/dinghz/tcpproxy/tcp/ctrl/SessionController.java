package com.dinghz.tcpproxy.tcp.ctrl;

import com.dinghz.tcpproxy.tcp.core.Cache;
import com.dinghz.tcpproxy.tcp.core.SessionInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        ArrayList<SessionInfo> infos = new ArrayList<>(Cache.SESSION_MAP.values());

        for (SessionInfo info : infos) {
            LocalDateTime time = LocalDateTime.parse(info.getActiveTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss S"));

            final Duration duration = Duration.between(time, LocalDateTime.now());
            final String liveTime = String.format("%d天%d小时%d分钟",
                    duration.toDays(),
                    duration.toHours() - duration.toDays() * 24,
                    duration.toMinutes() - duration.toHours() * 60);
            info.setLiveTime(liveTime);
        }

        return infos;
    }

}
