package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class Account {
    public static void addAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String name = scanner.nextLine().trim();

        // Check if the account already exists
        String checkQuery = "SELECT COUNT(*) FROM accounts WHERE name = ?";
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

        System.out.print("Enter contact number: ");
        String contactNumber = scanner.nextLine().trim();

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
}
