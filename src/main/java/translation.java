import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;


/**
 * Created with IDEA
 * author:wangcan
 * Date:4/28/2018
 * Time:8:36 AM
 */
public class translation {
    //OIO 阻塞
    private static void oio(int port) throws Exception{
        final ServerSocket socket=new ServerSocket(port);
        System.out.println("start");
        try{
            for(;;){
                final Socket clentStock=socket.accept();
                System.out.println("recivey:"+socket);
                new Thread((new Runnable() {
                    public void run() {
                        try{
                            OutputStream out;
                            out=clentStock.getOutputStream();
                            out.write("hi".getBytes());
                            out.flush();
                            clentStock.close();
                        }catch (Exception e){
                            System.out.println(e);
                        }

                    }
                })).start();
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        final int port=8888;
        try {
//            oio(port);
            nio(port);
        }catch (Exception e){

        }
    }

    //NIO 异步
    public static void nio(int port) throws Exception{
        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket socket=serverSocketChannel.socket();
        InetSocketAddress address=new InetSocketAddress(port);
        socket.bind(address);

        Selector selector=Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        final ByteBuffer msg=ByteBuffer.wrap("hi".getBytes());
        for(;;){
            try{
                selector.select();
            }catch (Exception e){
                System.out.println(e);
            }

            Set<SelectionKey> readKeys=selector.selectedKeys();
            Iterator<SelectionKey> iterable=readKeys.iterator();
            while (iterable.hasNext()){
                SelectionKey key=iterable.next();
                iterable.remove();
                try{
                    if(key.isAcceptable()){
                        ServerSocketChannel server=(ServerSocketChannel)key.channel();
                        SocketChannel client=server.accept();
                        client.configureBlocking(false);
                        client.register(selector,SelectionKey.OP_WRITE|SelectionKey.OP_READ,msg.duplicate());
                        System.out.println("recive:clinet");

                        if(key.isWritable()){
                            SocketChannel clien=(SocketChannel)key.channel();
                            ByteBuffer buffer=(ByteBuffer)key.attachment();
                            while (buffer.hasRemaining()){
                                if(clien.write(buffer)==0){
                                    break;
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    key.cancel();
                    try{
                        key.channel().close();
                    }catch (Exception ex){
                        System.out.println(ex);
                    }
                }
            }
        }
    }
}
