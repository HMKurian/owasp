package edu.cs;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/FileUploadServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
		maxFileSize = 1024 * 1024 * 50, // 50 MB
		maxRequestSize = 1024 * 1024 * 100) // 100 MB
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 205242440643911308L;
	/**
	 * Directory where uploaded files will be saved, its relative to the web
	 * application directory.
	 */
	private static final String UPLOAD_DIR = "uploads";

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String action = request.getParameter("action");
		if("Check Balance".equals(action)) {
			checkBalance(request, response);
		} else {
			uploadFile(request, response);
		}
	}
	
	private void uploadFile(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    String applicationPath = request.getServletContext().getRealPath("");
	    String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
	    File fileSaveDir = new File(uploadFilePath);
	    if (!fileSaveDir.exists()) {
	        fileSaveDir.mkdirs();
	    } 
	    
	    for (Part part : request.getParts()) {
	        if ("fileName".equals(part.getName())) {
	            String fileName = getFileName(part);
	            part.write(uploadFilePath + File.separator + fileName);
	            out.println("<p>File '" + fileName + "' uploaded successfully!</p>");
	        }
	    }
	    out.println("<p><a href='/'>Return to form</a></p>");
	}
	
	private void checkBalance(HttpServletRequest request, HttpServletResponse response)
	        throws IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    String firstName = request.getParameter("firstName");
	    String lastName = request.getParameter("lastName");
	    try (Connection conn = DatabaseUtil.getConnection()) {
	        String sql = "SELECT Balance FROM Accounts WHERE FirstName = ? AND LastName = ?";
	        try (PreparedStatement statement = conn.prepareStatement(sql)) {
	            statement.setString(1, firstName);
	            statement.setString(2, lastName);
	            ResultSet resultSet = statement.executeQuery();
	            if (resultSet.next()) {
	                double balance = resultSet.getDouble("Balance");
	                out.println("<p>Balance for " + firstName + " " + lastName + ": " + balance + "</p>");
	            } else {
	                out.println("<p>No account found for " + firstName + " " + lastName + "</p>");
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        out.println("<p>Database error: " + e.getMessage() + "</p>");
	    }
	    out.println("<p><a href='/'>Return to form</a></p>");
	}
	

/**
* Utility method to get file name from HTTP header content-disposition
*/
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
//	private String getFileName(Part part) {
//		String contentDisp = part.getHeader("content-disposition");
//		System.out.println("content-disposition header= " + contentDisp);
//		String[] tokens = contentDisp.split(";");
//		for (String token : tokens) {
//			if (token.trim().startsWith("filename")) {
//				return token.substring(token.indexOf("=") + 2, token.length() - 1);
//			}
//	}
//	return "";
//}

	private void writeToResponse(HttpServletResponse resp, String results) throws IOException {
		PrintWriter writer = new PrintWriter(resp.getOutputStream());
		resp.setContentType("text/plain");
		if (results.isEmpty()) {
			writer.write("No results found.");
		} else {
			writer.write(results);
		}
		writer.close();
	}
	public class DatabaseUtil {
	    private static final String DATABASE_URL = "jdbc:mysql://172.31.27.52:3306/FinTech";
	    private static final String DATABASE_USER = "db_user";
	    private static final String DATABASE_PASSWORD = "new_password";
	    
	    static {
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public static Connection getConnection() throws SQLException {
	        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
	    }
	}
}

//// gets absolute path of the web application
//String applicationPath = request.getServletContext().getRealPath("");
////constructs path of the directory to save uploaded file
//String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
////creates the save directory if it does not exists
//File fileSaveDir = new File(uploadFilePath);
//if (!fileSaveDir.exists()) {
//	fileSaveDir.mkdirs();
//}
//System.out.println("Upload File Directory=" + fileSaveDir.getAbsolutePath());
//String fileName = "";
////Get all the parts from request and write it to the file on server
//for (Part part : request.getParts()) {
//	fileName = getFileName(part);
//	fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
//	part.write(uploadFilePath + File.separator + fileName);
//}
//String message = "Result";
//String content = new Scanner(new File(uploadFilePath + File.separator + fileName)).useDelimiter("\\Z").next();
//response.getWriter().write(message + "<BR>" + content);
///******
// * Integrate remote DB connection with this servlet, uncomment and modify the
// * code below ******* //ADD YOUR CODE HERE!
// ********/
//try (Connection conn = DatabaseUtil.getConnection()) {
//    String sql = "INSERT INTO file_uploads (filename, upload_path) VALUES (?, ?)";
//    try (PreparedStatement statement = conn.prepareStatement(sql)) {
//        statement.setString(1, fileName);
//        statement.setString(2, uploadFilePath);
//        statement.executeUpdate();
//    }
//} catch (SQLException e) {
//    e.printStackTrace();
//    response.getWriter().write("Database error: " + e.getMessage());
//    return;
//}

