package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;

public class IT2A_Rosssabio {
    private static final String DATABASE_URL = "jdbc:sqlite:DataAppv2.db";

    public static void main(String[] args) {
        try (Connection conn = connect();
             Scanner scanner = new Scanner(System.in)) {

            if (conn != null) {
                createTablesIfNotExists(conn);

                while (true) {
                    System.out.println("\n*** Money Remittance System ***");
                    System.out.println("1. Send Money");
                    System.out.println("2. View All Customers");
                    System.out.println("3. View Transaction History");
                    System.out.println("4. Individual Report");
                    System.out.println("5. Exit");
                    System.out.print("Choose an option: ");

                    int choice;
                    if (scanner.hasNextInt()) {
                        choice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                    } else {
                        System.out.println("Invalid input. Please enter a number between 1 and 5.");
                        scanner.nextLine(); // Clear invalid input
                        continue;
                    }

                    switch (choice) {
                        case 1:
                            sendMoney(conn, scanner);
                            break;
                        case 2:
                            viewAllCustomers(conn);
                            break;
                        case 3:
                            viewTransactionHistory(conn);
                            break;
                        case 4:
                            viewIndividualReport(conn, scanner);
                            break;
                        case 5:
                            System.out.println("Exiting...");
                            return;
                        default:
                            System.out.println("Invalid option. Please choose a number between 1 and 5.");
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

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    public static void createTablesIfNotExists(Connection conn) throws SQLException {
    String createCustomersTable = "CREATE TABLE IF NOT EXISTS customers (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "age INTEGER NOT NULL, " +
            "email TEXT NOT NULL UNIQUE, " +
            "address TEXT NOT NULL, " +
            "balance REAL NOT NULL DEFAULT 0.0)"; // Added balance column with a default value.

    String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
            "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "sender_id INTEGER NOT NULL, " +
            "recipient_id INTEGER NOT NULL, " +
            "amount REAL NOT NULL, " +
            "transaction_date TEXT NOT NULL, " +
            "status TEXT NOT NULL DEFAULT 'pending', " + // Added status column with a default value
            "FOREIGN KEY (sender_id) REFERENCES customers(id), " +
            "FOREIGN KEY (recipient_id) REFERENCES customers(id))";

    try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate(createCustomersTable);
        stmt.executeUpdate(createTransactionsTable);
    }
}

 public static void sendMoney(Connection conn, Scanner scanner) throws SQLException {
    System.out.print("Enter sender ID: ");
    int senderId;
    try {
        senderId = Integer.parseInt(scanner.nextLine());
    } catch (NumberFormatException e) {
        System.out.println("Invalid sender ID. Please enter a valid numeric sender ID.");
        return;
    }

    System.out.print("Enter recipient customer ID: ");
    int recipientId;
    try {
        recipientId = Integer.parseInt(scanner.nextLine());
    } catch (NumberFormatException e) {
        System.out.println("Invalid recipient ID. Please enter a valid numeric customer ID.");
        return;
    }

    System.out.print("Enter amount to send: ");
    double amount;
    try {
        amount = Double.parseDouble(scanner.nextLine());
        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }
    } catch (NumberFormatException e) {
        System.out.println("Invalid amount. Please enter a valid numeric value.");
        return;
    }

    // Query to check if recipient exists
    String customerQuery = "SELECT id FROM customers WHERE id = ?";
    String insertTransactionQuery = "INSERT INTO transactions (sender_id, recipient_id, amount, transaction_date, status) VALUES (?, ?, ?, datetime('now'), ?)";
    String updateTransactionStatusQuery = "UPDATE transactions SET status = ? WHERE transaction_id = ?";

    try (PreparedStatement customerStmt = conn.prepareStatement(customerQuery);
         PreparedStatement insertStmt = conn.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {

        // Check if recipient exists
        customerStmt.setInt(1, recipientId);
        try (ResultSet rs = customerStmt.executeQuery()) {
            if (!rs.next()) {
                System.out.println("Recipient customer ID not found.");
                return;
            }

            // Log the transaction with status 'pending'
            insertStmt.setInt(1, senderId);  // Sender ID
            insertStmt.setInt(2, recipientId); // Recipient ID
            insertStmt.setDouble(3, amount);   // Amount
            insertStmt.setString(4, "pending"); // Status: pending initially
            insertStmt.executeUpdate();

            // Get the transaction ID of the last inserted transaction
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int transactionId = generatedKeys.getInt(1);

                    // Prompt to confirm and finalize the transaction
                    System.out.println("Transaction created with status 'pending'. Do you want to confirm the transaction? (yes/no)");
                    String confirm = scanner.nextLine();

                    if ("yes".equalsIgnoreCase(confirm)) {
                        // Now update the transaction status to 'completed'
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateTransactionStatusQuery)) {
                            updateStmt.setString(1, "completed");  // Update status to 'completed'
                            updateStmt.setInt(2, transactionId);   // Transaction ID
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
    // View transaction history
    public static void viewTransactionHistory(Connection conn) throws SQLException {
    // SQL query to fetch transaction details, sender ID and recipient information
    String query = "SELECT t.transaction_id, t.sender_id, r.name AS recipient_name, t.amount, t.transaction_date, t.status " +
                   "FROM transactions t " +
                   "JOIN customers r ON t.recipient_id = r.id";

    try (PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        // Print the table header with proper formatting
        System.out.println("\nTransaction History:");
        System.out.println("+----------------+--------------+-----------------+--------+-------------------+-------------------+");
        System.out.println("| Transaction ID | Sender ID    | Recipient Name  | Amount | Transaction Date  | Status            |");
        System.out.println("+----------------+--------------+-----------------+--------+-------------------+-------------------+");

        // Loop through the result set and print each transaction
        while (rs.next()) {
            System.out.printf("| %-14d | %-12d | %-15s | %-6.2f | %-17s | %-17s |\n",
                    rs.getInt("transaction_id"),
                    rs.getInt("sender_id"),  // Only the sender ID is now displayed
                    rs.getString("recipient_name"),
                    rs.getDouble("amount"),
                    rs.getString("transaction_date"),
                    rs.getString("status"));
        }

        // Close the table footer
        System.out.println("+----------------+--------------+-----------------+--------+-------------------+-------------------+");
    }
}
    
   public static void viewAllCustomers(Connection conn) throws SQLException {
    // SQL query to select id, name, email, age, and address from customers table
    String query = "SELECT id, name, email, age, address FROM customers";

    try (PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        // Print the table header with appropriate column names
        System.out.println("\nCustomers:");
        System.out.println("+-----+---------------------+---------------------------+-----+-----------------------------+");
        System.out.println("| ID  | Name                | Email                     | Age | Address                     |");
        System.out.println("+-----+---------------------+---------------------------+-----+-----------------------------+");

        // Loop through the result set and print each customer's details
        while (rs.next()) {
            System.out.printf("| %-3d | %-19s | %-25s | %-3d | %-27s |\n",
                    rs.getInt("id"),                 // ID - 3 characters width
                    rs.getString("name"),            // Name - 19 characters width
                    rs.getString("email"),           // Email - 25 characters width
                    rs.getInt("age"),                // Age - 3 characters width
                    rs.getString("address"));        // Address - 27 characters width
        }

        // Print the table footer
        System.out.println("+-----+---------------------+---------------------------+-----+-----------------------------+");
    }
}
    // Individual report: Customer details and transaction history
public static void viewIndividualReport(Connection conn, Scanner scanner) throws SQLException {
    // Prompt for the customer ID
    System.out.print("Enter customer ID: ");
    int customerId;
    try {
        customerId = Integer.parseInt(scanner.nextLine());
    } catch (NumberFormatException e) {
        System.out.println("Invalid ID. Please enter a valid numeric customer ID.");
        return;
    }

    // Query to fetch customer details (name, age, email, address)
    String customerQuery = "SELECT name, age, email, address FROM customers WHERE id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(customerQuery)) {
        stmt.setInt(1, customerId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                System.out.println("Customer not found.");
                return;
            }

            // Print customer information
            System.out.println("\nCustomer Report:");
            System.out.println("Name: " + rs.getString("name"));
            System.out.println("Age: " + rs.getInt("age"));
            System.out.println("Email: " + rs.getString("email"));
            System.out.println("Address: " + rs.getString("address"));
        }
    }

    // Query to fetch transaction history where the customer is the sender
    String transactionQuery = "SELECT t.transaction_id, t.amount, t.transaction_date, c.name AS recipient_name " +
                              "FROM transactions t " +
                              "JOIN customers c ON t.recipient_id = c.id " +
                              "WHERE t.sender_id = ?";  // Ensure we only select transactions where the customer is the sender

    try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
        stmt.setInt(1, customerId);  // Set customerId as the sender ID
        try (ResultSet rs = stmt.executeQuery()) {
            System.out.println("\nTransaction History (Money Sent):");
            System.out.println("+----------------+---------+-------------------+------------------+");
            System.out.println("| Transaction ID | Amount  | Transaction Date  | Recipient Name   |");
            System.out.println("+----------------+---------+-------------------+------------------+");

            boolean hasTransactions = false;  // Flag to check if any transactions exist
            while (rs.next()) {
                hasTransactions = true;
                System.out.printf("| %-14d | %-7.2f | %-17s | %-16s |\n",
                        rs.getInt("transaction_id"),
                        rs.getDouble("amount"),
                        rs.getString("transaction_date"),
                        rs.getString("recipient_name"));
            }

            if (!hasTransactions) {
                System.out.println("No transactions found for this customer.");
            }

            System.out.println("+----------------+---------+-------------------+------------------+");
        }
    }
}





}
