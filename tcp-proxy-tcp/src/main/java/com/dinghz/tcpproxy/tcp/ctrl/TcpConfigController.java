package com.dinghz.tcpproxy.tcp.ctrl;

import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import com.dinghz.tcpproxy.tcp.repository.TcpConfigRepository;
import com.dinghz.tcpproxy.tcp.service.TcpConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AppController
 *
 * @author dinghz
 * @date 2019-04-20
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@RestController
@RequestMapping("tcpConfigs")
public class TcpConfigController {

    @Autowired
    private TcpConfigRepository configRepository;

    @Autowired
    private TcpConfigService configService;

    @GetMapping
    public List<TcpConfig> list() {
        return configRepository.findAll();
    }

    @PostMapping
    public String create(TcpConfig tcpConfig) {
        if (configRepository.existsByLocalPort(tcpConfig.getLocalPort())) {
            return "local port is exist " + tcpConfig.getLocalPort();
        }

        String baseUrl = tcpConfig.getBaseUrl();
        String remoteHost = tcpConfig.getRemoteHost();
        Integer remotePort = tcpConfig.getRemotePort();
        if (configRepository.existsByBaseUrlAndRemoteHostAndRemotePort(baseUrl, remoteHost, remotePort)) {
            return "exist same proxy";
        }

        if (!configService.startService(configRepository.save(tcpConfig))) {
            return "start fail";
        }

        return "success";
    }

    @PatchMapping("{id}")
    @Transactional(rollbackFor = Exception.class)
    public String update(@PathVariable("id") Integer id, TcpConfig tcpConfig) {
        if (configRepository.existsById(id)) {
            return "not exist " + id;
        }

        TcpConfig config = configRepository.getOne(id);
        config.setLocalPort(tcpConfig.getLocalPort());
        config.setRemoteHost(tcpConfig.getRemoteHost());
        config.setRemotePort(tcpConfig.getRemotePort());
        config.setBaseUrl(tcpConfig.getBaseUrl());

        configService.stopService(tcpConfig);
        configService.startService(tcpConfig);

        return "success";
    }

    @DeleteMapping("{id}")
    public String delete(@PathVariable("id") Integer id) {
        if (configRepository.existsById(id)) {
            configService.stopService(configRepository.getOne(id));

            configRepository.deleteById(id);

            return "success";
        } else {
            return "not exist " + id;
        }
    }

}
