package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class TransactionOperations {

    public static void createTablesIfNotExists(Connection conn) throws SQLException {
        String createCustomersTable = "CREATE TABLE IF NOT EXISTS customers (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "name TEXT NOT NULL, " +
                                      "age INTEGER NOT NULL, " +
                                      "email TEXT NOT NULL UNIQUE, " +
                                      "address TEXT NOT NULL)";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                                         "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                         "sender_id INTEGER NOT NULL, " +
                                         "recipient_id INTEGER NOT NULL, " +
                                         "amount REAL NOT NULL, " +
                                         "transaction_date TEXT NOT NULL, " +
                                         "status TEXT NOT NULL DEFAULT 'pending', " +
                                         "FOREIGN KEY (sender_id) REFERENCES customers(id), " +
                                         "FOREIGN KEY (recipient_id) REFERENCES customers(id))";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createCustomersTable);
            stmt.executeUpdate(createTransactionsTable);
        }
    }

    public static void sendMoney(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter sender ID: ");
        int senderId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter recipient customer ID: ");
        int recipientId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter amount to send: ");
        double amount = Double.parseDouble(scanner.nextLine());

        String customerQuery = "SELECT id FROM customers WHERE id = ?";
        String insertTransactionQuery = "INSERT INTO transactions (sender_id, recipient_id, amount, transaction_date, status) VALUES (?, ?, ?, datetime('now'), ?)";
        String updateTransactionStatusQuery = "UPDATE transactions SET status = ? WHERE transaction_id = ?";

        try (PreparedStatement customerStmt = conn.prepareStatement(customerQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {

            customerStmt.setInt(1, recipientId);
            try (ResultSet rs = customerStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Recipient customer ID not found.");
                    return;
                }

                insertStmt.setInt(1, senderId);
                insertStmt.setInt(2, recipientId);
                insertStmt.setDouble(3, amount);
                insertStmt.setString(4, "pending");
                insertStmt.executeUpdate();

                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int transactionId = generatedKeys.getInt(1);

                        System.out.println("Transaction created with status 'pending'. Do you want to confirm the transaction? (yes/no)");
                        String confirm = scanner.nextLine();

                        if ("yes".equalsIgnoreCase(confirm)) {
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateTransactionStatusQuery)) {
                                updateStmt.setString(1, "completed");
                                updateStmt.setInt(2, transactionId);
                                updateStmt.executeUpdate();

                                System.out.println("Transaction confirmed and completed successfully!");
                            }
                        } else {
                            System.out.println("Transaction canceled.");
                        }
                    }
                }
            }
        }
    }

    public static void viewTransactionHistory(Connection conn) throws SQLException {
        String query = "SELECT t.transaction_id, t.sender_id, r.name AS recipient_name, t.amount, t.transaction_date, t.status " +
                       "FROM transactions t " +
                       "JOIN customers r ON t.recipient_id = r.id";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nTransaction History:");
            System.out.println("+----------------+--------------+-----------------+--------+-------------------+-------------------+");
            System.out.println("| Transaction ID | Sender ID    | Recipient Name  | Amount | Transaction Date  | Status            |");
            System.out.println("+----------------+--------------+-----------------+--------+-------------------+-------------------+");

            while (rs.next()) {
                System.out.printf("| %-14d | %-12d | %-15s | %-6.2f | %-17s | %-17s |\n",
                        rs.getInt("transaction_id"),
                        rs.getInt("sender_id"),
                        rs.getString("recipient_name"),
                        rs.getDouble("amount"),
                        rs.getString("transaction_date"),
                        rs.getString("status"));
            }

            System.out.println("+----------------+--------------+-----------------+--------+-------------------+-------------------+");
        }
    }
}
