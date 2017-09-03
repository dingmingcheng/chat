package serv;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class test
 */
@WebServlet("/test")
public class test extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private DBservice dbservice = new DBservice();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public test() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String username = request.getParameter("username");
	    String password = request.getParameter("password");
        response.setContentType("text/plain;charset=UTF-8");
        JedisService jedisservice = new JedisService();
        Cookie[] cookie = request.getCookies();
        String requestId = cookie[0].getValue();
	    if(dbservice.login(username, password)) {
	        response.getWriter().write("yes");
	        jedisservice.setrequestId(username, requestId);
	        jedisservice.setUsername(requestId, username);	        
	    }
	    else {
	        response.getWriter().write("no");
	    }
	}

}
