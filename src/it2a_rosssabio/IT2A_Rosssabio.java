package IT2A_rosssabio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 *
 * @author SCC-COLLEGE
 */
public class IT2A_Rosssabio {

    //Connection Method to SQLITE
public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:DataAppv2.db"); // Establish connection
            System.out.println("Connection Successful");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }
@SuppressWarnings("empty-statement")
    public static void main(String[] args) {
       Scanner sc = new Scanner(System.in);
       System.out.print("Enter FirstName: ");
       String fname = sc.next();
       System.out.print("Enter Last Name: ");
       String lname = sc.next();
       System.out.print("Enter Email: ");
       String email = sc.next();
       System.out.print("Enter Status: ");
       String status = sc.next();
       
       String sql = "INSERT INTO Student (s_id, s_fname, s_lname, s_email, s_status) VALUES (?, ?, ?, ?)";
       
       try{
           Connection con = connectDB();
           PreparedStatement pst = con.prepareStatement(sql);
           pst.setString(2, fname);
           pst.setString(3, lname);
           pst.setString(4, email);
           pst.setString(5, status);
           pst.executeUpdate();
           System.out.println("Inserted Successfully!");
         }catch(SQLException e){
            System.out.println("Connection error: "+e.getMessage());
         };
       
        
       
        
    }
    
}