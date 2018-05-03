package webstock;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * Created with IDEA
 * author:wangcan
 * Date:4/30/2018
 * Time:10:03 AM
 */
public class TextWebStockFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>{
    private final ChannelGroup groups;

    public TextWebStockFrameHandler(ChannelGroup groups) {
        this.groups = groups;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt== WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
            ctx.pipeline().remove(HttpRequestHandler.class);
            groups.writeAndFlush(new TextWebSocketFrame("clinet"+ctx.channel()+"joined"));
            groups.add(ctx.channel());
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Client:"+incoming.remoteAddress()+"在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Client:"+incoming.remoteAddress()+"掉线");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        for (Channel channel : groups) {
            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
        }
        groups.add(ctx.channel());
        System.out.println("Client:"+incoming.remoteAddress() +"加入");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        for (Channel channel : groups) {
            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));
        }
        System.out.println("Client:"+incoming.remoteAddress() +"离开");
        groups.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel inChannel=ctx.channel();
        for (Channel channel : groups) {
            if (channel != inChannel){
                channel.writeAndFlush(new TextWebSocketFrame("[" + inChannel.remoteAddress() + "]" + msg.text()));
            } else {
                channel.writeAndFlush(new TextWebSocketFrame("[you]" + msg.text() ));
            }
        }
        groups.writeAndFlush(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Client:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
