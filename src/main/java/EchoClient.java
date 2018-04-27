import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


import java.net.InetSocketAddress;

public class EchoClient {
  private final String host;
  private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public  void start() throws Exception{
        EventLoopGroup group=new NioEventLoopGroup();
        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host,port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) throws  Exception {
                                socketChannel.pipeline().addLast(new EchoClientHandler());
                            };
                    });
            ChannelFuture f=b.connect().sync();
            f.channel().closeFuture().sync();

        }finally {
            group.shutdownGracefully().sync();
        }

    }

    public static void main(String[] args) throws Exception{
        new EchoClient("127.0.0.1",8888).start();
    }
}

