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

    }

}
