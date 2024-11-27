package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class CustomerOperations {

    public static void viewAllCustomers(Connection conn) throws SQLException {
        String query = "SELECT id, name, email, age, address FROM customers";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nCustomers:");
            System.out.println("+-----+---------------------+---------------------------+-----+-----------------------------+");
            System.out.println("| ID  | Name                | Email                     | Age | Address                     |");
            System.out.println("+-----+---------------------+---------------------------+-----+-----------------------------+");

            while (rs.next()) {
                System.out.printf("| %-3d | %-19s | %-25s | %-3d | %-27s |\n",
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getInt("age"), rs.getString("address"));
            }
            System.out.println("+-----+---------------------+---------------------------+-----+-----------------------------+");
        }
    }

    public static void viewIndividualReport(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine());

        String customerQuery = "SELECT name, age, email, address FROM customers WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(customerQuery)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Customer not found.");
                    return;
                }

                System.out.println("\nCustomer Report:");
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Age: " + rs.getInt("age"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Address: " + rs.getString("address"));
            }
        }

        String transactionQuery = "SELECT t.transaction_id, t.amount, t.transaction_date " +
                                  "FROM transactions t " +
                                  "WHERE t.recipient_id = ? " +
                                  "ORDER BY t.transaction_date";

        try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nTransaction History (Money Received):");
                System.out.println("+----------------+---------+-------------------+------------------+");
                System.out.println("| Transaction ID | Amount  | Transaction Date  | Sender ID        |");
                System.out.println("+----------------+---------+-------------------+------------------+");

                int senderCounter = 1;
                while (rs.next()) {
                    System.out.printf("| %-14d | %-7.2f | %-17s | %-16s |\n",
                            rs.getInt("transaction_id"), rs.getDouble("amount"),
                            rs.getString("transaction_date"), "Sender " + senderCounter++);
                }

                System.out.println("+----------------+---------+-------------------+------------------+");
            }
        }
    }
}
