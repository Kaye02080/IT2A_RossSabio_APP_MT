package IT2A_Rosssabio;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    public static boolean processTransaction(Connection conn, String sender, String recipient, double amount) throws SQLException {
        double senderBalance = DatabaseManager.getBalance(conn, sender);
        double recipientBalance = DatabaseManager.getBalance(conn, recipient);

        if (senderBalance == -1 || recipientBalance == -1) {
            System.out.println("Sender or recipient account not found.");
            return false;
        }

        if (senderBalance < amount) {
            System.out.println("Insufficient funds.");
            return false;
        }

        // Update balances
        String updateSender = "UPDATE accounts SET balance = ? WHERE LOWER(name) = LOWER(?)";
        String updateRecipient = "UPDATE accounts SET balance = ? WHERE LOWER(name) = LOWER(?)";

        try (PreparedStatement stmt = conn.prepareStatement(updateSender)) {
            stmt.setDouble(1, senderBalance - amount);
            stmt.setString(2, sender);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = conn.prepareStatement(updateRecipient)) {
            stmt.setDouble(1, recipientBalance + amount);
            stmt.setString(2, recipient);
            stmt.executeUpdate();
        }

        // Insert transaction record
        String insertTransaction = "INSERT INTO transactions (sender_id, recipient_id, amount, transaction_date) "
                + "SELECT (SELECT id FROM accounts WHERE LOWER(name) = LOWER(?)), "
                + "(SELECT id FROM accounts WHERE LOWER(name) = LOWER(?)), ?, CURRENT_TIMESTAMP";
        try (PreparedStatement stmt = conn.prepareStatement(insertTransaction)) {
            stmt.setString(1, sender);
            stmt.setString(2, recipient);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        }

        return true;
    }

    public static void viewTransactionHistory(Connection conn) throws SQLException {
        String query = "SELECT t.transaction_id, t.sender_id, t.recipient_id, t.amount, t.transaction_date, "
                     + "s.name AS sender_name, r.name AS recipient_name "
                     + "FROM transactions t "
                     + "JOIN accounts s ON t.sender_id = s.id "
                     + "JOIN accounts r ON t.recipient_id = r.id";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nTransaction History:");
            System.out.println("+----------------+-------------+----------------+--------+------------------+-------------------+");
            System.out.println("| Transaction ID | Sender Name | Recipient Name | Amount | Transaction Date | Time              |");
            System.out.println("+----------------+-------------+----------------+--------+------------------+-------------------+");

            while (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                String senderName = rs.getString("sender_name");
                String recipientName = rs.getString("recipient_name");
                double amount = rs.getDouble("amount");
                String transactionDate = rs.getString("transaction_date");

                System.out.println("| " + transactionId + "           | " + senderName + "         | " + recipientName + "      | " + amount + "    | " + transactionDate + " |");
            }

            System.out.println("+----------------+-------------+----------------+--------+------------------+-------------------+");
        }
    }
}
