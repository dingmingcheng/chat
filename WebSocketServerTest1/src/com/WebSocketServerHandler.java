package com;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONObject;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object>{
    
    private WebSocketServerHandshaker handshaker;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        //传统HTTP接入
        if (msg instanceof FullHttpRequest) { // 传统的HTTP接入
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } 
        //websocket接入
        else if (msg instanceof WebSocketFrame) { // WebSocket接入
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }
    
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
     // 判断是否是关闭链路的指令
        WebSocketService webservice = new WebSocketService();
        JedisService jedisservice = new JedisService();
        if (frame instanceof CloseWebSocketFrame) {
            System.out.println("关闭链路");           
            webservice.logout(ctx);
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            System.out.println("ping");
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 当前只支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException("当前只支持文本消息，不支持二进制消息");
        }
        String request = ((TextWebSocketFrame) frame).text();      
        //推送消息
        //webservice.pushMessage("chatroom1", request);
        //send(ctx,request);
        
        // ToDo  json
        String requestId,message,statuscode,chatroomname,username;     
        JSONObject obj = JSONObject.fromObject(request);
        requestId = obj.getString("requestId");
        message = obj.getString("message");
        statuscode = obj.getString("statuscode");
        //System.out.println(RedisUtil.getJedis().lrange(requestId, 0, -1).toString());
        username = jedisservice.getUsername(requestId);
        chatroomname = jedisservice.getchatroom(requestId);
        //System.out.println(username + " " + requestId + " " + chatroomname); 

        if(statuscode.equals("0")) { //初次连接时的注册
            chatroomname = "chatroom1";
            webservice.login(username,requestId, chatroomname, ctx);// 将房间号，sessionid，ChannelHandlerContext传入
        }
        else if(statuscode.equals("1")) { //传递消息
            webservice.pushMessage(username, requestId, chatroomname, message);
            System.out.println(username + " 发送 " + message);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        System.out.println("http");
        // 如果HTTP解码失败，返回HTTP异常
        if (!request.getDecoderResult().isSuccess() || (!"websocket".equals(request.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        
        // 正常WebSocket的Http连接请求，构造握手响应返回
        //WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HttpHeaders.Names.HOST), null, false);
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://192.168.0.104:9090/websocket", null, false);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) { // 无法处理的websocket版本
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else { // 向客户端发送websocket握手,完成握手
            handshaker.handshake(ctx.channel(), request);
            System.out.println("link");
            /*
            String userID = "dmc";
            webservice.login(userID,"chatroom1",ctx);
            */
            // 记录管道处理上下文，便于服务器推送数据到客户端
        }
    }
    
    /**
     * Http返回
     * @param ctx
     * @param request
     * @param response
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,FullHttpResponse res) {
        if(res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }
        
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if(!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
