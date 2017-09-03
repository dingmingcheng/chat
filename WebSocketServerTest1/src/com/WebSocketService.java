package com;

import java.util.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

public class WebSocketService {
    public static Map<String,HashMap<String, ChannelHandlerContext> > trx = new HashMap<String,HashMap<String, ChannelHandlerContext> >();
    public static Map<ChannelHandlerContext, String> trx1 = new HashMap<ChannelHandlerContext, String>();
    public static int flag = 0;
    public static int countNum = 0;
    JedisService jedisservice = new JedisService();
    WebSocketService() {
        if(flag == 0) {
            synchronized(this.getClass()) {
                if(flag == 0) {
                    trx.put("chatroom1", new HashMap<String, ChannelHandlerContext>());
                    flag = 1;
                }
            }
        }
    }

    public void login(String username,String requestId,String chatroom, ChannelHandlerContext ctx) {
        trx.get(chatroom).put(requestId, ctx);
        trx1.put(ctx, requestId);
        jedisservice.setchatroom(requestId, chatroom);
        sendAllPeo(chatroom);
        /*
         * debug
         * **/
        System.out.println(username + "进入了" + chatroom);
        System.out.println("当前数量为 :" + (++countNum));
        for(String t1 : trx.keySet()) {
            System.out.println(t1+ " : " + trx.get(t1).size() + " 人");
        }
    }
    
    public void logout(ChannelHandlerContext ctx) {
        //删除redis中的聊天室名称
        String requestId = trx1.get(ctx);
        String username, chatroom;
        username = jedisservice.getUsername(requestId);
        chatroom = jedisservice.getchatroom(requestId);

        HashMap<String, ChannelHandlerContext> ls = trx.get(chatroom);
        for(String t:ls.keySet()) {
            if(t.equals(requestId)) {
                ls.remove(requestId);
                break;
            }
        }
        
        trx.put(chatroom, ls);
        //从redis中删除
        jedisservice.logout(requestId);
        sendAllPeo(chatroom);
        /*
         * debug
         * **/
        System.out.println(username + "离开了" + chatroom);
        for(String t1 : trx.keySet()) {
            System.out.println(t1+ " : " + trx.get(t1).size() + " 人");
        }
    }
    
    public void pushMessage(String username, String requestId, String chatroom, String message) {
        HashMap<String, ChannelHandlerContext> ls = trx.get(chatroom);        
        ChannelHandlerContext change = null;
        JSONObject Json = null;
        for(String t:ls.keySet()) {
            Json = new JSONObject();
            //
            int flag = 0;
           
            // todo json
            //String piccode = jedisservice.getusername(t);
            String piccode = username;
            //
            Json.put("piccode", piccode);
            Json.put("message", message);
            Json.put("flag", flag);
            //
            change = ls.get(t);
            
            send(change, Json.toString());
            change.flush();
        }
       /* for(ChannelHandlerContext t:ls) {
            //            
            //    todo :String to json
            //
            System.out.println(message);
            send(t, message);
            t.flush();
        }*/
    }
    public static void sendAllPeo(String chatroom) {
        HashMap<String, ChannelHandlerContext> ls = trx.get(chatroom);
        List<String> peo = new ArrayList<String>();
        JedisService s1 = new JedisService();
        String username;
        for(String t:ls.keySet()) {
            username = s1.getUsername(t);
            peo.add(username);
        }
        JSONObject Json = new JSONObject();
        String flag = "1";
        Json.put("flag", flag);
        Json.put("peo", peo);
        System.out.println(Json.toString());
        for(String t:ls.keySet()) {
            send(ls.get(t), Json.toString());
            ls.get(t).flush();
        }
    }
    private static void send(ChannelHandlerContext ctx, String request) {
        ctx.channel().write(new TextWebSocketFrame(request));
    }
}
