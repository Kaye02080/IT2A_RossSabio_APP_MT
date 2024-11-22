package IT2A_Rosssabio;

import java.sql.*;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:DataAppv2.db";

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
            stmt.executeUpdate(createAccountsTable);
            stmt.executeUpdate(createTransactionsTable);
        }
    }

    public static double getBalance(Connection conn, String accountName) throws SQLException {
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
}
