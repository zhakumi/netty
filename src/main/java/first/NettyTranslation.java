package first;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.util.ByteProcessor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import jdk.nashorn.internal.runtime.linker.Bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created with IDEA
 * author:wangcan
 * Date:4/28/2018
 * Time:10:47 PM
 */
public class NettyTranslation {

    /**
     * 阻塞
     * @param port
     * @throws Exception
     */
    public void nettyOioServer(int port)throws Exception{
            final ByteBuf buf= Unpooled.unreleasableBuffer(
                    Unpooled.copiedBuffer("hi", Charset.forName("utf-8")));
        EventLoopGroup group=new OioEventLoopGroup();//阻塞
        try{
            ServerBootstrap boot=new ServerBootstrap();
            boot.group(group)
                    .channel(OioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                   ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture f= boot.bind().sync();
            f.channel().closeFuture().sync();
        }catch (Exception e){
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            new NettyTranslation().nettyOioServer(8888);

            //ByteBuf buf=new ByteBuf();


        }catch (Exception e){

        }
    }
}
