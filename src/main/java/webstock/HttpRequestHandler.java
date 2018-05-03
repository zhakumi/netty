package webstock;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Random;
import java.util.RandomAccess;

/**
 * Created with IDEA
 * author:wangcan
 * Date:4/30/2018
 * Time:9:12 AM
 *  主要功能：转发任何目标的URL
 */
public class HttpRequestHandler  extends SimpleChannelInboundHandler<FullHttpRequest>{
    private final String wsUrl;
    private static  final File INDEX;

    public HttpRequestHandler(String wsUrl) {
        this.wsUrl = wsUrl;
    }
    static {
        URL location=HttpRequestHandler.class
                .getProtectionDomain()
                .getCodeSource().getLocation();
        try{
            String path=location.toURI() + "index.html";
            path=!path.contains("file:")?path:path.substring(5);
            INDEX=new File(path);
        }catch (Exception e){
            throw new IllegalStateException("not convont to html");
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(wsUrl.equalsIgnoreCase(request.getUri())){
            ctx.fireChannelRead(request.retain());
        }else{
            if(HttpHeaders.is100ContinueExpected(request)){
                send100Continue(ctx);
            }
            RandomAccessFile file=new RandomAccessFile(INDEX,"r");
            HttpResponse response=new DefaultHttpResponse(request.getProtocolVersion(),HttpResponseStatus.OK);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE,"text/html;chaset=UTF-8");
            boolean keepAlive=HttpHeaders.isKeepAlive(request);
            if(keepAlive){
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,file.length());
                response.headers().set(HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
            }
            ctx.write(response);
            if(ctx.pipeline().get(SslHandler.class)==null){
                ctx.write(new DefaultFileRegion(file.getChannel(),0,file.length()));
            }else{
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            ChannelFuture future=ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if(!keepAlive){
                future.addListener(ChannelFutureListener.CLOSE);
            }
            file.close();
        }
    }

    private void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
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
