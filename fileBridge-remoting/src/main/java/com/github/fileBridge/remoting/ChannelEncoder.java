package com.github.fileBridge.remoting;

import com.github.fileBridge.common.proto.EventOuterClass;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author ZhiCheng
 * @date 2023/3/17 16:46
 */
public class ChannelEncoder extends MessageToByteEncoder<EventOuterClass.Event> {

    @Override
    protected void encode(ChannelHandlerContext ctx, EventOuterClass.Event msg, ByteBuf out) throws Exception {
        byte[] bytes = msg.toByteArray();
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
