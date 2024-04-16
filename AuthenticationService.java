import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationService {

    public void processLogin(String email, String password) {
        String storedHash = retrieveUserPasswordHash(email);
        if (storedHash == null) {
            System.out.println("User not found.");
            return;
        }
        
        if (UserAuthorization.checkPassword(password, storedHash)) {
            String mfaCode = generateMFAcode();
            UserAuthorization.sendMFA(email, mfaCode);
            System.out.println("MFA code has been sent. Please check your email.");
        } else {
            System.out.println("Invalid login attempt.");
        }
    }

    private String generateMFAcode() {
        // Simple MFA code generation logic. This generates a 4-digit code.
        return Integer.toString((int) (Math.random() * 9000) + 1000);
    }

    private String retrieveUserPasswordHash(String email) {
        String query = "SELECT password_hash FROM users WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
}

class UserAuthorization {
    
    public static String hashPassword(String plainTextPassword){
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String storedHash){
        return BCrypt.checkpw(password, storedHash);
    }

    public static void sendMFA(String userEmail, String mfaCode) {
        String subject = "Your MFA Code";
        String message = "Your MFA code is: " + mfaCode;
        EmailUtility.sendEmail(userEmail, subject, message);
        System.out.println("MFA code sent to: " + userEmail);
    }
    
    public static boolean verifyMFA(String inputCode, String realCode) {
        return inputCode.equals(realCode);
    }
}