package exception;

/**
 * The application cannot connect to PostgreSQL (server down, wrong URL, wrong password, missing driver).
 */
public class DatabaseConnectionException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
