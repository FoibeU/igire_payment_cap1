package exception;

import java.sql.SQLException;

/**
 * A SQL error while reading or writing data. Wraps SQLException so upper layers do not depend on JDBC.
 */
public class DataAccessException extends IgirePayException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /** PostgreSQL SQLSTATE of the underlying error (e.g. 23505 = unique violation), or null. */
    public String getSqlState() {
        return getCause() instanceof SQLException sqlException ? sqlException.getSQLState() : null;
    }
}
