package LAB2;

import LAB1.ProcessedRequest;
import util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ProcessedRequestDaoImpl implements ProcessedRequestDao {

    private static final String INSERT_SQL = "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT (reference_id) DO NOTHING";
    private static final String FIND_BY_REFERENCE_SQL = "SELECT id, reference_id, processed_at FROM processed_requests WHERE reference_id = ?";
    private static final String FIND_ALL_SQL = "SELECT id, reference_id, processed_at FROM processed_requests ORDER BY processed_at DESC LIMIT ?";
    private static final String TOUCH_SQL = "UPDATE processed_requests SET processed_at = CURRENT_TIMESTAMP WHERE reference_id = ?";
    private static final String DELETE_SQL = "DELETE FROM processed_requests WHERE reference_id = ?";
    private static final String EXISTS_SQL = "SELECT 1 FROM processed_requests WHERE reference_id = ?";
    private static final String ALL_REFERENCES_SQL = "SELECT reference_id FROM processed_requests";
    private static final String DELETE_OLDER_THAN_SQL = "DELETE FROM processed_requests WHERE processed_at < CURRENT_TIMESTAMP - make_interval(days => ?)";

    @Override
    public boolean create(Connection connection, String referenceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, referenceId);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public Optional<ProcessedRequest> findByReferenceId(Connection connection, String referenceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_REFERENCE_SQL)) {
            statement.setString(1, referenceId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<ProcessedRequest> findAll(Connection connection, int limit) throws SQLException {
        List<ProcessedRequest> requests = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL)) {
            statement.setInt(1, Math.max(1, limit));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRow(rs));
                }
            }
        }
        return requests;
    }

    @Override
    public boolean touch(Connection connection, String referenceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(TOUCH_SQL)) {
            statement.setString(1, referenceId);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(Connection connection, String referenceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setString(1, referenceId);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean exists(Connection connection, String referenceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(EXISTS_SQL)) {
            statement.setString(1, referenceId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<String> findAllReferenceIds(Connection connection) throws SQLException {
        List<String> references = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(ALL_REFERENCES_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                references.add(rs.getString(1));
            }
        }
        return references;
    }

    @Override
    public int deleteOlderThan(Connection connection, int days) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_OLDER_THAN_SQL)) {
            statement.setInt(1, days);
            return statement.executeUpdate();
        }
    }

    private ProcessedRequest mapRow(ResultSet rs) throws SQLException {
        return new ProcessedRequest(rs.getLong("id"), rs.getString("reference_id"), JdbcUtil.toLocalDateTime(rs.getTimestamp("processed_at")));
    }
}
