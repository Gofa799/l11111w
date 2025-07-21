import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {
    private static final String DB_URL = System.getenv("DB_URL");

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}