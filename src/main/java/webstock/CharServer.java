package webstock;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.net.InetSocketAddress;

/**
 * Created with IDEA
 * author:wangcan
 * Date:4/30/2018
 * Time:10:21 AM
 */
public class CharServer {
    //保存所有已链接的channel
    private final ChannelGroup channelGroup= new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    EventLoopGroup bossGroup=new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;

    private void start(InetSocketAddress address){
        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(createInializer(channelGroup));
            System.out.println("WebsocketChatServer 启动了");
            ChannelFuture future = boot.bind(address).sync();
            future.syncUninterruptibly();
            channel = future.channel();
             future.channel().closeFuture().sync();
        }catch (Exception e){

        }
    }

    private ChannelInitializer<Channel> createInializer(ChannelGroup channelGroup) {
        return  new ChatServerInitizlizer(channelGroup);
    }

//    private void destory(){
//        if(channel!=null){
//            channel.close();
//        }
//        channelGroup.close();
//        group.shutdownGracefully();
//    }

    public static void main(String[] args) {
//     int port=8888;
//     final CharServer server=new CharServer();
//     ChannelFuture future=server.start(new InetSocketAddress(port));
//     Runtime.getRuntime().addShutdownHook(new Thread(){
//         @Override
//         public void run() {
//             server.destory();
//         }
//     });
//     future.channel().closeFuture().syncUninterruptibly();
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new CharServer().start(new InetSocketAddress(port));
    }
}
