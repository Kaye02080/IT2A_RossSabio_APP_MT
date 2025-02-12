 package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class IT2A_Rosssabio {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.connect();
             Scanner scanner = new Scanner(System.in)) {

            if (conn != null) {
                TransactionOperations.createTablesIfNotExists(conn);
                UserOperations.createUsersTableIfNotExists(conn);

                // Login or register
                System.out.println("Welcome to the Money Remittance System");
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                String role = UserOperations.login(conn, username, password);

                if (role == null) {
                    System.out.println("Invalid credentials.");
                    return;
                }

                System.out.println("Logged in as: " + role);

                while (true) {
                    System.out.println("\n*** Money Remittance System ***");
                    System.out.println("1. Send Money");
                    if ("Admin".equalsIgnoreCase(role)) {
                        System.out.println("2. View All Customers");
                        System.out.println("3. View Transaction History");
                    }
                    System.out.println("4. Individual Report");
                    System.out.println("5. Exit");

                    int choice = -1;
                    while (choice == -1) {
                        System.out.print("Enter your choice: ");
                        String input = scanner.nextLine();

                        if (input.matches("\\d+")) {
                            choice = Integer.parseInt(input);
                        } else {
                            System.out.println("Invalid input. Please enter a number.");
                        }
                    }

                    switch (choice) {
                        case 1:
                            TransactionOperations.sendMoney(conn, scanner);
                            break;
                        case 2:
                            if ("Admin".equalsIgnoreCase(role)) {
                                CustomerOperations.viewAllCustomers(conn);
                            } else {
                                System.out.println("Access denied.");
                            }
                            break;
                        case 3:
                            if ("Admin".equalsIgnoreCase(role)) {
                                TransactionOperations.viewTransactionHistory(conn);
                            } else {
                                System.out.println("Access denied.");
                            }
                            break;
                        case 4:
                            CustomerOperations.viewIndividualReport(conn, scanner);
                            break;
                        case 5:
                            System.out.println("Exiting system...");
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class UserOperations {

    public static void createUsersTableIfNotExists(Connection conn) throws SQLException {
        String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                                 "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                 "username TEXT NOT NULL UNIQUE, " +
                                 "password TEXT NOT NULL, " +
                                 "role TEXT NOT NULL CHECK(role IN ('Admin', 'Teller'))" +
                                 ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUserTable);
        }

        // Insert default admin and teller
        String insertDefaultUsers = "INSERT OR IGNORE INTO users (username, password, role) VALUES " +
                                    "('admin', 'admin123', 'Admin'), " +
                                    "('teller', 'teller123', 'Teller')";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(insertDefaultUsers);
        }
    }

public static String login(Connection conn, String username, String password) throws SQLException {
    String query = "SELECT role FROM users WHERE LOWER(username) = LOWER(?) AND password = ?";

    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, username);
        stmt.setString(2, password);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String role = rs.getString("role");
                System.out.println("Retrieved role: " + role);
                return role;
            }
        }
    }

    System.out.println("Invalid credentials for username: " + username);
    return null;
}

}
