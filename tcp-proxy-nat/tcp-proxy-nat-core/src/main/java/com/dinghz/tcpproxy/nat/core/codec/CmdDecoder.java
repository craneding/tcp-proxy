package com.dinghz.tcpproxy.nat.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * CmdDecoder
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class CmdDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < Codec.HEAD_LENGTH) {
            return;
        }

        final int readerIndex = in.readerIndex();

        final int length = Codec.decodeBodyLength(in);

        if (length != 0 && in.readableBytes() < length) {
            in.readerIndex(readerIndex);

            return;
        }

        ByteBuf msg = in.slice(readerIndex, length + Codec.HEAD_LENGTH).retain();

        in.readerIndex(readerIndex + length + Codec.HEAD_LENGTH);

        out.add(msg);
    }

}
