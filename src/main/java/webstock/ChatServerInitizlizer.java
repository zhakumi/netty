package webstock;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IDEA
 * author:wangcan
 * Date:4/30/2018
 * Time:10:11 AM
 */
public class ChatServerInitizlizer extends ChannelInitializer<Channel>{
    private  final ChannelGroup group;


    public ChatServerInitizlizer(ChannelGroup group) {
        this.group = group;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline=channel.pipeline();
        pipeline.addLast(new HttpServerCodec());//字节码解码为httprequest httpcontent
        pipeline.addLast(new ChunkedWriteHandler());//写入一个文件的内容
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        pipeline.addLast(new HttpRequestHandler("/ws"));//处理Fullhttprequest
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));//按照webstock的规范，处理webstock的升级
        pipeline.addLast(new TextWebStockFrameHandler(group));//处理握手
    }
}
