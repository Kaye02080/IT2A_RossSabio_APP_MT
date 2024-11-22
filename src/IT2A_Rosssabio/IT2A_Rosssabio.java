package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class IT2A_Rosssabio {
    public static void main(String[] args) {
        try (Connection conn = DatabaseManager.connect(); Scanner scanner = new Scanner(System.in)) {
            if (conn != null) {
                DatabaseManager.createTablesIfNotExists(conn);

                while (true) {
                    System.out.println("\n*** Account Management System ***");
                    System.out.println("1. Add Account");
                    System.out.println("2. Check Balance");
                    System.out.println("3. Update Balance");
                    System.out.println("4. Delete Account");
                    System.out.println("5. Send Money");
                    System.out.println("6. View Transaction History");
                    System.out.println("7. Exit");
                    System.out.print("Choose an option: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // consume newline character

                    switch (choice) {
                        case 1:
                            Account.addAccount(conn, scanner);
                            break;
                        case 2:
                            Account.checkBalance(conn, scanner);
                            break;
                        case 3:
                            Account.updateBalance(conn, scanner);
                            break;
                        case 4:
                            Account.deleteAccount(conn, scanner);
                            break;
                        case 5:
                            System.out.print("Enter sender account: ");
                            String sender = scanner.nextLine();
                            System.out.print("Enter recipient account: ");
                            String recipient = scanner.nextLine();
                            System.out.print("Enter amount to send: ");
                            double amount = scanner.nextDouble();
                            if (Transaction.processTransaction(conn, sender, recipient, amount)) {
                                System.out.println("Transaction completed successfully.");
                            } else {
                                System.out.println("Transaction failed.");
                            }
                            break;
                        case 6:
                            Transaction.viewTransactionHistory(conn);
                            break;
                        case 7:
                            System.out.println("Exiting...");
                            return;
                        default:
                            System.out.println("Invalid option. Try again.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
