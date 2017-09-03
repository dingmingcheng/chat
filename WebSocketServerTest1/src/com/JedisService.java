package com;

import redis.clients.jedis.Jedis;

public class JedisService {
    Jedis jedis = new Jedis("127.0.0.1", 6379);

    public void setrequestId(String username, String requestId) {
        jedis.set(username, requestId);
    }
    
    public void setUsername(String requestId, String username) {
        jedis.del(requestId);
        jedis.rpush(requestId, username);
    }
    public void setchatroom(String requestId, String chatroom) {
        jedis.rpush(requestId, chatroom);
    }
    
    public String getrequestId(String username) {
        return jedis.get(username);
    }
    
    public String getUsername(String requestId) {
        String ans = jedis.lrange(requestId, 0, 0).toString();
        return ans.substring(1, ans.length() - 1);
    }
    
    public String getchatroom(String requestId) {
        String ans = jedis.lrange(requestId, 1, 1).toString();
        return ans.substring(1, ans.length() - 1);
    }
    
    public void logout(String requestId) {
        jedis.del(requestId);
    }
}
