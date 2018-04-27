import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("prot");
            return;
        }
        int port=Integer.parseInt(args[0]);
        new EchoServer(port).start();
        System.out.println("server:run()");
    }

    public void start() throws Exception{
        final EchoServerHandler serverHandler=new EchoServerHandler();
        //创建LOOPGROUP
        EventLoopGroup group=new NioEventLoopGroup();
       try{
           //创建SERVER
           ServerBootstrap b=new ServerBootstrap();
           b.group(group)
                   .channel(NioServerSocketChannel.class)
                   .localAddress(new InetSocketAddress(port))
                   .childHandler(new ChannelInitializer<SocketChannel>() {
                       @Override
                       public void initChannel(SocketChannel socketChannel) throws  Exception {
                           socketChannel.pipeline().addLast(new EchoClientHandler());
                       };
                   });
           ChannelFuture f=b.bind().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
