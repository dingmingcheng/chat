package com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import net.sf.json.JSONObject;

public class test {
    public static void main(String[] args) {
        List<String> peo = new ArrayList<String>();
        for(int i = 0; i <= 10; i ++) {            
            peo.add(Integer.toString(i));
        }
        JSONObject Json = new JSONObject();
        String flag = "1";
        Json.put("flag", flag);
        Json.put("peo", peo);
        System.out.println(Json.toString());
    }
}
