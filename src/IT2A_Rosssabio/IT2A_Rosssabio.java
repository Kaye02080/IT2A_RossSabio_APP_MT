package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class IT2A_Rosssabio {
    private static final String DATABASE_URL = "jdbc:sqlite:DataAppv2.db";

    public static void main(String[] args) {
        try (Connection conn = connect()) {
            createTables(conn);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Money Remittance System");
                System.out.println("1. Send Money");
                System.out.println("2. Check Balance");
                System.out.println("3. Update Balance");
                System.out.println("4. Delete Account");
                System.out.println("5. Exit");

                System.out.print("Select an option: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        sendMoney(conn, scanner);
                        break;
                    case 2:
                        checkBalance(conn, scanner);
                        break;
                    case 3:
                        updateBalance(conn, scanner);
                        break;
                    case 4:
                        deleteAccount(conn, scanner);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static void createTables(Connection conn) throws SQLException {
        String createAccountTable = "CREATE TABLE IF NOT EXISTS accounts ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL UNIQUE,"
                + "balance REAL NOT NULL DEFAULT 0.0"
                + ");";
        try (PreparedStatement stmt = conn.prepareStatement(createAccountTable)) {
            stmt.execute();
        }
    }

    private static void sendMoney(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter sender's name: ");
        String sender = scanner.next();

        System.out.print("Enter recipient's name: ");
        String recipient = scanner.next();

        System.out.print("Enter amount to send: ");
        double amount = scanner.nextDouble();

        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
            
           String sql = "INSERT INTO Money_Remittance(MR_SenderID, MR_RecipientName, MR_Amount, MR_Balance) VALUES (?, ?, ?, ?)";


            config addRecord = config.addRecord(sql, sender, recipient, amount, balance);
            
        }

        conn.setAutoCommit(false);  // Begin transaction

        try {
            double senderBalance = getBalance(conn, sender);
            if (senderBalance < amount) {
                System.out.println("Insufficient funds.");
                conn.rollback();  // Rollback the transaction in case of failure
            }

            
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Transaction failed. Rolled back.");
        } finally {
            conn.setAutoCommit(true);  // Reset auto-commit
        }
    }

    private static void updateBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String name = scanner.next();

        System.out.print("Enter new balance: ");
        double newBalance = scanner.nextDouble();

        if (newBalance < 0) {
            System.out.println("Balance cannot be negative.");
            return;
        }

        String updateRecord = "UPDATE accounts SET balance = ? WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateRecord)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, name);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Balance updated successfully for " + name);
            } else {
                System.out.println("Account not found.");
            }
        }
       String sql = "INSERT INTO Account(MR_SenderID, MR_RecipientName, MR_Amount, MR_Balance) VALUES (?, ?, ?, ?)";
       config updateBalance = config.updateBalance(sql, sender, recipient, amount, balance);
    }

    private static void deleteAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account ID to delete: ");
        String name = scanner.next();

        String deleteQuery = "DELETE FROM accounts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setString(1, name);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Account deleted successfully.");
            } else {
                System.out.println("");
            }
        }
    }

    private static void checkBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String name = scanner.next();

        double balance = getBalance(conn, name);
        if (balance >= 0) {
            System.out.println(name + "'s balance: $" + balance);
        } else {
            System.out.println("Account not found.");
        }
    }

    private static double getBalance(Connection conn, String name) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                createAccount(conn, name);  // Create account if it doesn't exist
                return 0.0;
            }
        }
    }

    private static void createAccount(Connection conn, String name) throws SQLException {
        String insertQuery = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, name);
            stmt.setDouble(2, 0.0);
            stmt.executeUpdate();
            System.out.println("Account created for " + name);
        }
    }
}
