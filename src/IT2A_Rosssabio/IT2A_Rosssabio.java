package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class IT2A_Rosssabio {
    public static void main(String[] args) {
        try (Connection conn = DatabaseManager.connect();
             Scanner scanner = new Scanner(System.in)) {

            if (conn != null) {
                DatabaseManager.createTablesIfNotExists(conn);

                while (true) {
                    System.out.println("\n*** Money Remittance System ***");
                    System.out.println("1. Add Account");
                    System.out.println("2. Check Balance");
                    System.out.println("3. Update Balance");
                    System.out.println("4. Delete Account");
                    System.out.println("5. Send Money");
                    System.out.println("6. View Transaction History");
                    System.out.println("7. View All Accounts");
                    System.out.println("8. Exit");
                    System.out.print("Choose an option: ");

                    int choice;
                    // Validate user input
                    if (scanner.hasNextInt()) {
                        choice = scanner.nextInt();
                        scanner.nextLine(); // consume newline character
                    } else {
                        System.out.println("Invalid input. Please enter a number between 1 and 8.");
                        scanner.nextLine(); // clear invalid input
                        continue;
                    }

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
                            Transaction.sendMoney(conn, scanner);
                            break;
                        case 6:
                            Transaction.viewTransactionHistory(conn);
                            break;
                        case 7:
                            Account.viewAllAccounts(conn);
                            break;
                        case 8:
                            System.out.println("Exiting...");
                            return;
                        default:
                            System.out.println("Invalid option. Please choose a number between 1 and 8.");
                    }
                }
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
