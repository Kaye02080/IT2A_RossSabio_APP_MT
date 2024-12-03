 package IT2A_Rosssabio;

import java.sql.Connection;
import java.util.Scanner;

public class IT2A_Rosssabio {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.connect();
             Scanner scanner = new Scanner(System.in)) {

            if (conn != null) {
                TransactionOperations.createTablesIfNotExists(conn);

                while (true) {
                    System.out.println("\n*** Money Remittance System ***");
                    System.out.println("1. Send Money");
                    System.out.println("2. View All Customers");
                    System.out.println("3. View Transaction History");
                    System.out.println("4. Individual Report");
                    System.out.println("5. Exit");

                    int choice = -1; // Default invalid value
                    while (choice == -1) {
                        System.out.print("Enter your choice: ");
                        String input = scanner.nextLine();
                        
                        // Validate input
                        if (input.matches("\\d+")) { // Checks if input contains only digits
                            choice = Integer.parseInt(input);
                        } else {
                            System.out.println("Invalid input. Please enter a number.");
                        }
                    }

                    switch (choice) {
                        case 1:
                            TransactionOperations.sendMoney(conn, scanner);
                            break;
                        case 2:
                            CustomerOperations.viewAllCustomers(conn);
                            break;
                        case 3:
                            TransactionOperations.viewTransactionHistory(conn);
                            break;
                        case 4:
                            CustomerOperations.viewIndividualReport(conn, scanner);
                            break;
                        case 5:
                            System.out.println("Exiting system...");
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
