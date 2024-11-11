package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class IT2A_Rosssabio {
    private static final String DATABASE_URL = "jdbc:sqlite:DataAppv2.db";

    public static void main(String[] args) {
        try (Connection conn = connect()) {
            // Ensure required tables exist
            createTablesIfNotExists(conn);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Money Remittance System");
                System.out.println("1. Send Money");
                System.out.println("2. Check Balance");
                System.out.println("3. Update Balance");
                System.out.println("4. Delete Account");
                System.out.println("5. View All Accounts");
                System.out.println("6. View Transaction History");
                System.out.println("7. Exit");

                System.out.print("Select an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Clear the newline character

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
                        viewAllAccounts(conn);
                        break;
                    case 6:
                        viewTransactionHistory(conn);
                        break;
                    case 7:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    private static void createTablesIfNotExists(Connection conn) throws SQLException {
        // Create accounts table if it doesn't exist
        String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts ("
                                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                    + "name TEXT NOT NULL, "
                                    + "balance REAL DEFAULT 0.0)";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createAccountsTable);
        }

        // Create transactions table if it doesn't exist
        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                                        + "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                        + "sender_id INTEGER NOT NULL, "
                                        + "recipient_id INTEGER NOT NULL, "
                                        + "amount REAL NOT NULL, "
                                        + "transaction_date TEXT NOT NULL, "
                                        + "FOREIGN KEY (sender_id) REFERENCES accounts(id), "
                                        + "FOREIGN KEY (recipient_id) REFERENCES accounts(id))";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTransactionsTable);
        }
    }

    private static void sendMoney(Connection conn, Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("Enter sender's name: ");
            String sender = scanner.nextLine().trim();

            System.out.print("Enter recipient's name: ");
            String recipient = scanner.nextLine().trim();

            double amount = 0;
            while (true) {
                System.out.print("Enter amount to send: ");
                try {
                    amount = Double.parseDouble(scanner.nextLine());
                    if (amount <= 0) {
                        System.out.println("Amount must be greater than zero.");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
            }

            conn.setAutoCommit(false);  // Begin transaction

            try {
                double senderBalance = getBalance(conn, sender);
                if (senderBalance < amount) {
                    System.out.println("Insufficient funds.");
                    conn.rollback();  // Rollback the transaction in case of failure
                    return;
                }

                double recipientBalance = getBalance(conn, recipient);

                // Update sender's balance
                String updateSender = "UPDATE accounts SET balance = ? WHERE name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSender)) {
                    stmt.setDouble(1, senderBalance - amount);  // Deduct the amount from sender's balance
                    stmt.setString(2, sender);
                    stmt.executeUpdate();
                }

                // Update recipient's balance
                String updateRecipient = "UPDATE accounts SET balance = ? WHERE name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateRecipient)) {
                    stmt.setDouble(1, recipientBalance + amount);  // Add the amount to recipient's balance
                    stmt.setString(2, recipient);
                    stmt.executeUpdate();
                }

                // Insert transaction record
                String insertTransaction = "INSERT INTO transactions (sender_id, recipient_id, amount, transaction_date) "
                        + "SELECT (SELECT id FROM accounts WHERE name = ?), "
                        + "(SELECT id FROM accounts WHERE name = ?), ?, CURRENT_TIMESTAMP";
                try (PreparedStatement stmt = conn.prepareStatement(insertTransaction)) {
                    stmt.setString(1, sender);
                    stmt.setString(2, recipient);
                    stmt.setDouble(3, amount);
                    stmt.executeUpdate();
                }

                conn.commit();
                System.out.println("Transaction successful. " + amount + " sent from " + sender + " to " + recipient);

                // Ask if the user wants to perform another transaction
                System.out.print("Would you like to perform another transaction? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes")) {
                    System.out.println("Exiting transaction process.");
                    break;  // Exit the loop and method
                }
            } catch (SQLException e) {
                conn.rollback();  // Rollback the transaction in case of any error
                System.out.println("Transaction failed. Rolled back.");
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);  // Reset auto-commit
            }
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
                createAccount(conn, name);  // Create the account if it doesn't exist
                return 0.0;
            }
        }
    }

    private static void createAccount(Connection conn, String name) throws SQLException {
        String insertAccount = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertAccount)) {
            stmt.setString(1, name);
            stmt.setDouble(2, 0.0);  // Set initial balance to 0
            stmt.executeUpdate();
            System.out.println("Account for " + name + " created with initial balance of $0.00");
        }
    }

    private static void viewAllAccounts(Connection conn) throws SQLException {
        String query = "SELECT id, name, balance FROM accounts";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Accounts and Balances:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                System.out.println("ID: " + id + ", Name: " + name + ", Balance: $" + balance);
            }
        }
    }

    private static void viewTransactionHistory(Connection conn) throws SQLException {
        String query = "SELECT t.transaction_id, a1.name AS sender, a2.name AS recipient, t.amount, t.transaction_date "
                     + "FROM transactions t "
                     + "JOIN accounts a1 ON t.sender_id = a1.id "
                     + "JOIN accounts a2 ON t.recipient_id = a2.id "
                     + "ORDER BY t.transaction_date DESC";  // Add ordering for better clarity

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Transaction History:");
            if (!rs.next()) {
                System.out.println("No transactions found.");
                return; // No transactions
            }
            do {
                int transactionId = rs.getInt("transaction_id");
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                double amount = rs.getDouble("amount");
                String date = rs.getString("transaction_date");
                System.out.println("Transaction ID: " + transactionId 
                                   + ", Sender: " + sender 
                                   + ", Recipient: " + recipient 
                                   + ", Amount: $" + amount 
                                   + ", Date: " + date);
            } while (rs.next());
        }
    }

    private static void updateBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String name = scanner.nextLine().trim();

        double newBalance = 0;
        while (true) {
            System.out.print("Enter new balance: ");
            try {
                newBalance = Double.parseDouble(scanner.nextLine());
                if (newBalance < 0) {
                    System.out.println("Balance cannot be negative.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
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
    }

    private static void deleteAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name to delete: ");
        String name = scanner.nextLine().trim();

        String deleteQuery = "DELETE FROM accounts WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setString(1, name);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Account deleted successfully.");
            } else {
                System.out.println("Account not found.");
            }
        }
    }

    private static void checkBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String name = scanner.nextLine().trim();

        double balance = getBalance(conn, name);
        if (balance >= 0) {
            System.out.println(name + "'s balance: $" + balance);
        } else {
            System.out.println("Account not found.");
        }
    }
}
