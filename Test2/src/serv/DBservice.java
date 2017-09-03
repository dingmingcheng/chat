package serv;

import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import serv.ConnectionPool.PooledConnection;

public class DBservice {
    ResultSet rs;
    private static String jdbcDriver = "com.mysql.jdbc.Driver"; // 数据库驱动
    private static String dbUrl = "jdbc:mysql://localhost:3306/websocket?useUnicode=true&amp;characterEncoding=UTF-8"; // 数据 URL
    private static String dbUsername = "root"; // 数据库用户名
    private static String dbPassword = "dmcdmc123";
    private Connection conn;
    public boolean login(String username, String password)
    {
        String sql = "select username from user where username=? and password=?";
        try {
            Class.forName(jdbcDriver);        
            conn = (Connection) DriverManager.getConnection(dbUrl, dbUsername, dbPassword); 
            PreparedStatement stmt = (PreparedStatement) conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if(rs != null) return true;
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
