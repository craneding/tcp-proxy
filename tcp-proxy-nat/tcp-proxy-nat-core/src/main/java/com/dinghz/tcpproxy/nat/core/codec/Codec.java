package com.dinghz.tcpproxy.nat.core.codec;

import com.alibaba.fastjson.JSON;
import com.dinghz.tcpproxy.nat.core.cmd.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;

/**
 * Codec
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class Codec {

    private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    public static final int HEAD_LENGTH = 5;

    public static enum TypeEnum {
        Connect, Heartbeat, NetData, Close;
    }

    public static int decodeBodyLength(ByteBuf buf) {
        buf.skipBytes(1);

        return buf.readInt();
    }

    public static <T extends AbstractCmd> ByteBuf encode(Channel channel, T obj) {
        byte[] jsonBytes = JSON.toJSONString(obj).getBytes(DEFAULT_CHARSET);

        int type = TypeEnum.Connect.ordinal();
        if (obj instanceof CmdConnect) {
            type = TypeEnum.Connect.ordinal();
        } else if (obj instanceof CmdHeartbeat) {
            type = TypeEnum.Heartbeat.ordinal();
        } else if (obj instanceof CmdNetData) {
            type = TypeEnum.NetData.ordinal();
        } else if (obj instanceof CmdClose) {
            type = TypeEnum.Close.ordinal();
        }

        int size = 1 + Integer.BYTES + jsonBytes.length;
        ByteBuf buf = getBuffer(channel, size);
        buf.writeByte(type);
        buf.writeInt(jsonBytes.length);
        buf.writeBytes(jsonBytes);
        return buf;
    }

    public static <T extends AbstractCmd> ByteBuf encode(ChannelHandlerContext context, T obj) {
        byte[] jsonBytes = JSON.toJSONString(obj).getBytes(DEFAULT_CHARSET);

        int type = TypeEnum.Connect.ordinal();
        if (obj instanceof CmdConnect) {
            type = TypeEnum.Connect.ordinal();
        } else if (obj instanceof CmdHeartbeat) {
            type = TypeEnum.Heartbeat.ordinal();
        } else if (obj instanceof CmdNetData) {
            type = TypeEnum.NetData.ordinal();
        } else if (obj instanceof CmdClose) {
            type = TypeEnum.Close.ordinal();
        }

        int size = 1 + Integer.BYTES + jsonBytes.length;
        ByteBuf buf = getBuffer(context, size);
        buf.writeByte(type);
        buf.writeInt(jsonBytes.length);
        buf.writeBytes(jsonBytes);
        return buf;
    }

    private static ByteBuf getBuffer(Channel channel, int size) {
        return channel.alloc().buffer(size);
    }

    private static ByteBuf getBuffer(ChannelHandlerContext context, int size) {
        return context.alloc().buffer(size);
    }

    public static AbstractCmd decode(ByteBuf buf) {
        final int type = buf.readByte();
        final int length = buf.readInt();

        final byte[] jsonBytes = new byte[length];
        buf.readBytes(jsonBytes);
        final String json = new String(jsonBytes, DEFAULT_CHARSET);

        if (type == TypeEnum.Connect.ordinal()) {
            return JSON.parseObject(json, CmdConnect.class);
        } else if (type == TypeEnum.Heartbeat.ordinal()) {
            return JSON.parseObject(json, CmdHeartbeat.class);
        } else if (type == TypeEnum.NetData.ordinal()) {
            return JSON.parseObject(json, CmdNetData.class);
        } else if (type == TypeEnum.Close.ordinal()) {
            return JSON.parseObject(json, CmdClose.class);
        } else {
            return JSON.parseObject(json, AbstractCmd.class);
        }
    }

}
