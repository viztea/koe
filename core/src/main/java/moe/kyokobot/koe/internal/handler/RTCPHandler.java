package moe.kyokobot.koe.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTCPHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger LOG = LoggerFactory.getLogger("koe.udp");

    // https://tools.ietf.org/html/rfc3550#section-6
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        LOG.trace("Received UDP packet: {}", packet);
    }
}
