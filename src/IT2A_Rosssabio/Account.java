package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class Account {
   public static void addAccount(Connection conn, Scanner scanner) throws SQLException {
    System.out.print("Enter account name: ");
    String name = scanner.nextLine().trim();

    // Validate name to ensure it only contains letters and spaces
    if (name.isEmpty() || name.length() > 50 || !name.matches("[a-zA-Z\\s]+")) {
        System.out.println("Name must be between 1 and 50 characters and contain only letters and spaces. Try again.");
        return;
    }

    // Check if the account already exists
    String checkQuery = "SELECT COUNT(*) FROM accounts WHERE LOWER(name) = LOWER(?)";
    try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
        stmt.setString(1, name);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Account with this name already exists.");
                return;
            }
        }
    }

    // Collect additional account details
    System.out.print("Enter address: ");
    String address = scanner.nextLine().trim();

    int age = 0;
    while (true) {
        System.out.print("Enter age: ");
        try {
            age = Integer.parseInt(scanner.nextLine());
            if (age > 0) {
                break;
            } else {
                System.out.println("Age must be greater than zero. Try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid age.");
        }
    }

    String contactNumber;
    while (true) {
        System.out.print("Enter contact number: ");
        contactNumber = scanner.nextLine().trim();
        if (contactNumber.matches("\\d{10}")) {
            break;
        } else {
            System.out.println("Invalid contact number. It must be 10 digits.");
        }
    }

    // Insert new account into the database with provided details
    String insertQuery = "INSERT INTO accounts (name, address, age, contact_number, balance) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
        stmt.setString(1, name);
        stmt.setString(2, address);
        stmt.setInt(3, age);
        stmt.setString(4, contactNumber);
        stmt.setDouble(5, 0.0);  // New accounts start with a balance of 0.0
        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Account created successfully for " + name);
        } else {
            System.out.println("Failed to create account.");
        }
    } catch (SQLException e) {
        System.err.println("Database error: " + e.getMessage());
    }
}



    public static void checkBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String accountName = scanner.nextLine().trim();
        double balance = DatabaseManager.getBalance(conn, accountName);
        if (balance == -1) {
            System.out.println("Account not found.");
        } else {
            System.out.println("Balance for " + accountName + ": " + balance);
        }
    }

    public static void updateBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String accountName = scanner.nextLine().trim();

        double newBalance = 0;
        while (true) {
            System.out.print("Enter new balance: ");
            try {
                newBalance = Double.parseDouble(scanner.nextLine());
                if (newBalance >= 0) {
                    break;
                } else {
                    System.out.println("Balance cannot be negative. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }

        String updateQuery = "UPDATE accounts SET balance = ? WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountName);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Balance updated successfully for " + accountName);
            } else {
                System.out.println("Account not found.");
            }
        }
    }

    public static void deleteAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name to delete: ");
        String accountName = scanner.nextLine().trim();

        String deleteQuery = "DELETE FROM accounts WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setString(1, accountName);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account " + accountName + " deleted successfully.");
            } else {
                System.out.println("Account not found.");
            }
        }
    }
 public static void viewAllAccounts(Connection conn) throws SQLException {
    String query = "SELECT * FROM accounts";
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
        System.out.println("\n--- All Accounts ---");
        
        // Print table header with lines
        System.out.println("+----------------------+------------------------------+-----+-----------------+------------+");
        System.out.printf("| %-20s | %-28s | %-3s | %-15s | %-10s |\n", "Name", "Address", "Age", "Contact Number", "Balance");
        System.out.println("+----------------------+------------------------------+-----+-----------------+------------+");

        boolean hasAccounts = false;
        while (rs.next()) {
            hasAccounts = true;
            String name = rs.getString("name");
            String address = rs.getString("address");
            int age = rs.getInt("age");
            String contactNumber = rs.getString("contact_number");
            double balance = rs.getDouble("balance");

            // Print each account's details with proper column width alignment
            System.out.printf("| %-20s | %-28s | %-3d | %-15s | %-10.2f |\n", name, address, age, contactNumber, balance);
        }

        // If no accounts are found
        if (!hasAccounts) {
            System.out.println("No accounts found.");
        }

        // Closing the table with a line
        System.out.println("+----------------------+------------------------------+-----+-----------------+------------+");
    }
}

    
    
  

    

}
