package com.dinghz.tcpproxy;

import org.apache.commons.codec.digest.Md5Crypt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Config
 *
 * @author dinghz
 * @date 2017/5/17
 * @company  丁小样同学工作室
 * @email crane.ding@163.com
 */
public class Config {

    public static String salt = "******";
    public static String passwd = Md5Crypt.md5Crypt("******".getBytes(), Config.salt);
    public static final Map<String, String> ips = new ConcurrentHashMap<>();

    static {
        //测试
        ips.put("127.0.0.1", "localhost");
        ips.put("192.168.30.43", "310终端机");
        ips.put("192.168.31.31", "丁鹤泽");
        ips.put("192.168.30.246", "丁鹤泽");
        ips.put("192.168.30.248", "李振棠");
        ips.put("192.168.30.243", "黄炽桓");
        ips.put("192.168.30.247", "陈健");
        ips.put("192.168.30.58", "陈迪笋");
        ips.put("192.168.30.249", "陈迪笋");
        ips.put("192.168.31.123", "何智锋");
        ips.put("192.168.31.48", "杨剑珍");
        ips.put("192.168.31.129", "黄子霞");
        ips.put("192.168.31.107", "贺智华");
        ips.put("192.168.30.241", "王炳荣");
        ips.put("192.168.31.86", "谢泽");
        ips.put("192.168.30.105", "张炳伦");
        ips.put("192.168.31.16", "310测试机");
        ips.put("192.168.30.242", "林培鑫");
        ips.put("172.17.0.34", "proxy");
    }

}
