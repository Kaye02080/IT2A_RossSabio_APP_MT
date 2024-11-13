package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class IT2A_Rosssabio {
    private static final String DATABASE_URL = "jdbc:sqlite:DataAppv2.db";

    public static void main(String[] args) {
        try (Connection conn = connect()) {

            createTablesIfNotExists(conn);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Money Remittance System");
                System.out.println("1. Add Account");
                System.out.println("2. Send Money");
                System.out.println("3. Check Balance");
                System.out.println("4. Update Balance");
                System.out.println("5. Delete Account");
                System.out.println("6. View All Accounts");
                System.out.println("7. View Transaction History");
                System.out.println("8. Exit");

                System.out.print("Select an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  

                switch (choice) {
                    case 1:
                        addAccount(conn, scanner); 
                        break;
                    case 2:
                        sendMoney(conn, scanner);
                        break;
                    case 3:
                        checkBalance(conn, scanner);
                        break;
                    case 4:
                        updateBalance(conn, scanner);
                        break;
                    case 5:
                        deleteAccount(conn, scanner);
                        break;
                    case 6:
                        viewAllAccounts(conn);
                        break;
                    case 7:
                        viewTransactionHistory(conn);
                        break;
                    case 8:
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

    // Add account method
    private static void addAccount(Connection conn, Scanner scanner) throws SQLException {
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

        // Insert new account into the database with a balance of 0.0
        String insertQuery = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, name);
            stmt.setDouble(2, 0.0);  // New accounts start with a balance of 0.0
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Account created successfully for " + name);
            } else {
                System.out.println("Failed to create account.");
            }
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

    private static double getBalance(Connection conn, String accountName) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    return -1; // Account not found
                }
            }
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
                String response = "";
                while (true) {
                    System.out.print("Would you like to perform another transaction? (yes/no): ");
                    response = scanner.nextLine().trim().toLowerCase();
                    if (response.equals("yes") || response.equals("no")) {
                        break;  // Exit the loop if the response is valid
                    } else {
                        System.out.println("Invalid response. Please enter 'yes' or 'no'.");
                    }
                }

                if (response.equals("no")) {
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

    private static void viewAllAccounts(Connection conn) throws SQLException {
        String query = "SELECT id, name, balance FROM accounts";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Accounts and Balances:");
            if (!rs.next()) {
                System.out.println("No accounts found.");
            } else {
                do {
                    System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Balance: " + rs.getDouble("balance"));
                } while (rs.next());
            }
        }
    }

    private static void checkBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter account name: ");
        String accountName = scanner.nextLine().trim();
        double balance = getBalance(conn, accountName);
        if (balance == -1) {
            System.out.println("Account not found.");
        } else {
            System.out.println("Balance for " + accountName + ": " + balance);
        }
    }

    private static void updateBalance(Connection conn, Scanner scanner) throws SQLException {
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

    private static void deleteAccount(Connection conn, Scanner scanner) throws SQLException {
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

    private static void viewTransactionHistory(Connection conn) throws SQLException {
        String query = "SELECT t.transaction_id, t.sender_id, t.recipient_id, t.amount, t.transaction_date, "
                     + "s.name AS sender_name, r.name AS recipient_name "
                     + "FROM transactions t "
                     + "JOIN accounts s ON t.sender_id = s.id "
                     + "JOIN accounts r ON t.recipient_id = r.id";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Transaction History:");
            if (!rs.next()) {
                System.out.println("No transactions found.");
            } else {
                do {
                    System.out.println("Transaction ID: " + rs.getInt("transaction_id")
                            + ", Sender: " + rs.getString("sender_name")
                            + ", Recipient: " + rs.getString("recipient_name")
                            + ", Amount: " + rs.getDouble("amount")
                            + ", Date: " + rs.getString("transaction_date"));
                } while (rs.next());
            }
        }
    }
}
