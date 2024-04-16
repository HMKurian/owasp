import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HTTPOnlyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create a new cookie
        Cookie userSessionCookie = new Cookie("userSession", "sessionValue123456");
        userSessionCookie.setMaxAge(60 * 60 * 24); 
        userSessionCookie.setHttpOnly(true); 
        userSessionCookie.setPath("/"); 
        
        // Add cookie to the response
        response.addCookie(userSessionCookie);
        
        // Further response handling
        response.getWriter().write("HttpOnly cookie set successfully!");
    }
}
