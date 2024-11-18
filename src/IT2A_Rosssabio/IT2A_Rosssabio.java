package IT2A_Rosssabio;

import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    // Collect additional account details
    System.out.print("Enter address: ");
    String address = scanner.nextLine().trim();

    int age = 0;
    while (true) {
        System.out.print("Enter age: ");
        try {
            age = Integer.parseInt(scanner.nextLine());
            if (age > 0) {
                break;
            } else {
                System.out.println("Age must be greater than zero. Try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid age.");
        }
    }

    System.out.print("Enter contact number: ");
    String contactNumber = scanner.nextLine().trim();

    // Insert new account into the database with provided details
    String insertQuery = "INSERT INTO accounts (name, address, age, contact_number, balance) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
        stmt.setString(1, name);
        stmt.setString(2, address);
        stmt.setInt(3, age);
        stmt.setString(4, contactNumber);
        stmt.setDouble(5, 0.0);  // New accounts start with a balance of 0.0
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
    String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + "name TEXT NOT NULL, "
                                + "address TEXT, "
                                + "age INTEGER, "
                                + "contact_number TEXT, "
                                + "balance REAL DEFAULT 0.0)";


   
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

            conn.setAutoCommit(false); // Begin transaction
            try {
                if (processTransaction(conn, sender, recipient, amount)) {
                    conn.commit();
                    System.out.println("Transaction successful. " + amount + " sent from " + sender + " to " + recipient);
                } else {
                    conn.rollback();
                    System.out.println("Transaction failed. Rolled back.");
                }

                String response = "";
                while (true) {
                    System.out.print("Would you like to perform another transaction? (yes/no): ");
                    response = scanner.nextLine().trim().toLowerCase();
                    if (response.equals("yes") || response.equals("no")) {
                        break;
                    } else {
                        System.out.println("Invalid response. Please enter 'yes' or 'no'.");
                    }
                }

                if (response.equals("no")) {
                    System.out.println("Exiting transaction process.");
                    break; // Exit the loop and method
                }
            } catch (SQLException e) {
                conn.rollback();  // Rollback the transaction in case of any error
                System.out.println("Transaction failed. Rolled back.");
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);  // Reset auto-commit mode
            }
        }
    }
   private static boolean processTransaction(Connection conn, String sender, String recipient, double amount) throws SQLException {
        double senderBalance = getBalance(conn, sender);
        double recipientBalance = getBalance(conn, recipient);

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
   

private static void viewAllAccounts(Connection conn) throws SQLException {
    String query = "SELECT id, name, address, age, contact_number, balance FROM accounts";
    try (PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        System.out.println("\nAccounts and Details:");
        System.out.println("+-----+----------------+----------------+-----+-------------------+---------+");
        System.out.println("| ID  | Name           | Address        | Age | Contact Number    | Balance |");
        System.out.println("+-----+----------------+----------------+-----+-------------------+---------+");

        // Check if the result set is empty
        if (!rs.isBeforeFirst()) {
            System.out.println("|                      No accounts found                          |");
        } else {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                int age = rs.getInt("age");
                String contactNumber = rs.getString("contact_number");
                double balance = rs.getDouble("balance");

                // Display formatted account details
                System.out.printf("| %-3d | %-14s | %-14s | %-3d | %-17s | %-7.2f |\n",
                        id, name, address, age, contactNumber, balance);
            }
        }
        System.out.println("+-----+----------------+----------------+-----+-------------------+---------+");
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

        System.out.println("\nTransaction History:");
        System.out.println("+----------------+-------------+----------------+--------+------------------+-------------------+");
        System.out.println("| Transaction ID | Sender Name | Recipient Name | Amount | Transaction Date | Time              |");
        System.out.println("+----------------+-------------+----------------+--------+------------------+-------------------+");
        
        if (!rs.next()) {
            System.out.println("No transactions found.");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // For date format
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); // For time format

            do {
                int transactionId = rs.getInt("transaction_id");
                String senderName = rs.getString("sender_name");
                String recipientName = rs.getString("recipient_name");
                double amount = rs.getDouble("amount");
                String transactionDate = rs.getString("transaction_date");

                // Parse the full date-time string from the database
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transactionDate);
                    String dateFormatted = dateFormat.format(date);  // Get the date part (yyyy-MM-dd)
                    String timeFormatted = timeFormat.format(date);  // Get the time part (HH:mm:ss)

                    // Printing the transaction details in a formatted way, splitting date and time
                    System.out.printf("| %-15d | %-12s | %-14s | %-6.2f | %-16s | %-15s |\n", 
                                      transactionId, senderName, recipientName, amount, dateFormatted, timeFormatted);
                } catch (Exception e) {
                    // Handle parsing errors, if any (e.g., invalid date format)
                    e.printStackTrace();
                }
            } while (rs.next());
        }
        System.out.println("+----------------+-------------+----------------+--------+------------------+-------------------+");
    }
}



}