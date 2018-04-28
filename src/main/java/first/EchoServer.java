package first;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
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
        new EchoServer(8888).start();
        System.out.println("server:run()");
    }

    public void start() throws Exception{
        final EchoServerHandler serverHandler=new EchoServerHandler();
        //创建LOOPGROUP 进行事件处理
        EventLoopGroup group=new NioEventLoopGroup();
       try{
           //使用启动器创建SERVER
           ServerBootstrap b=new ServerBootstrap();
           b.group(group)//绑定事件处理组
                   .channel(NioServerSocketChannel.class)
                   .localAddress(new InetSocketAddress(8888))//绑定端口
                   .childHandler(new ChannelInitializer<SocketChannel>() {//添加一个子echo handeler到子通道去
                       @Override
                       public void initChannel(SocketChannel socketChannel) throws  Exception {
                           socketChannel.pipeline().addLast(serverHandler);//
                       };
                   });
           ChannelFuture f=b.bind().sync();//异步的绑定服务器 直到绑定完成sync
           f.channel().closeFuture().sync();//获取通道的关闭方法直到它完成
        } finally {
            group.shutdownGracefully().sync();//关闭group 直到释放所有资源
        }
    }

}
