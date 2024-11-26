package IT2A_Rosssabio;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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
        System.out.println("+----------------+----------------+-----------------+--------+-------------------+-------------------+");
        System.out.println("| Transaction ID | Sender Name    | Recipient Name  | Amount | Transaction Date  | Time              |");
        System.out.println("+----------------+----------------+-----------------+--------+-------------------+-------------------+");

        while (rs.next()) {
            int transactionId = rs.getInt("transaction_id");
            String senderName = rs.getString("sender_name");
            String recipientName = rs.getString("recipient_name");
            double amount = rs.getDouble("amount");
            String transactionDateTime = rs.getString("transaction_date");

            // Split the transaction date into date and time
            String[] dateTimeParts = transactionDateTime.split(" ");
            String transactionDate = dateTimeParts[0]; // Date part (yyyy-MM-dd)
            String transactionTime = dateTimeParts.length > 1 ? dateTimeParts[1] : ""; // Time part (HH:mm:ss)

            // Print the transaction details with proper alignment
            System.out.printf("| %-14d | %-14s | %-15s | %-6.2f | %-17s | %-17s |\n",
                              transactionId, senderName, recipientName, amount, transactionDate, transactionTime);
        }

        System.out.println("+----------------+----------------+-----------------+--------+-------------------+-------------------+");
    }
}

    
public static void sendMoney(Connection conn, Scanner scanner) throws SQLException {
    System.out.print("Enter sender's account name: ");
    String senderName = scanner.nextLine().trim();

    System.out.print("Enter recipient's account name: ");
    String recipientName = scanner.nextLine().trim();

    double amount;
    while (true) {
        System.out.print("Enter amount to send: ");
        try {
            amount = Double.parseDouble(scanner.nextLine());
            if (amount > 0) {
                break;
            } else {
                System.out.println("Amount must be greater than zero. Try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid amount.");
        }
    }

    // Process transaction
    if (processTransaction(conn, senderName, recipientName, amount)) {
        System.out.println("Transfer successful! " + amount + " sent from " + senderName + " to " + recipientName);
    } else {
        System.out.println("Transaction failed. Please check the details and try again.");
    }
}

  
}
