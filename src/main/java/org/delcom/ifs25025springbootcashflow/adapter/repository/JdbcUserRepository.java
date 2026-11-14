package org.delcom.ifs25025springbootcashflow.adapter.repository;

import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Implementasi JDBC {@link IUserRepository} untuk runtime (PostgreSQL). */
public class JdbcUserRepository extends JdbcRepository<User, Integer> implements IUserRepository {

    private static final String INSERT_SQL =
            "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?";

    public JdbcUserRepository(DataSource dataSource) {
        super(dataSource, "users", "id");
    }

    @Override
    protected User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                rs.getString("photo_path"));
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        int id = insertReturningId(INSERT_SQL, username, passwordHash, fullName, role.name());
        return new User(id, username, passwordHash, fullName, role);
    }

    @Override
    public User save(User entity) {
        User saved = save(entity.getUsername(), entity.getPasswordHash(), entity.getFullName(),
                entity.getRole());
        if (entity.getPhotoPath() != null) {
            saved.changePhotoPath(entity.getPhotoPath());
            update(saved);
        }
        return saved;
    }

    @Override
    public void update(User entity) {
        executeUpdate(UPDATE_SQL, entity.getPasswordHash(), entity.getFullName(),
                entity.getPhotoPath(), entity.getId());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return query("SELECT * FROM users WHERE username = ?", username)
                .stream()
                .findFirst();
    }
}
