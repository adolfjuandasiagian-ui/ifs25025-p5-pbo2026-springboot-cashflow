package org.delcom.ifs25025springbootcashflow.adapter.repository;

import org.delcom.ifs25025springbootcashflow.domain.repository.IRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Base generic repository berbasis JDBC murni (DataSource, PreparedStatement, ResultSet).
 * Operasi umum (findAll, findById, deleteById, findBy) ditulis sekali di sini; subclass hanya
 * menyediakan nama tabel, pemetaan baris ke entity, serta {@code save} dan {@code update}.
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe data identifier entitas
 */
public abstract class JdbcRepository<T, ID> implements IRepository<T, ID> {

    /** Pekerjaan JDBC yang memakai satu koneksi. */
    @FunctionalInterface
    protected interface SqlWork<R> {
        R apply(Connection connection) throws SQLException;
    }

    private final DataSource dataSource;
    private final String table;
    private final String idColumn;

    protected JdbcRepository(DataSource dataSource, String table, String idColumn) {
        this.dataSource = dataSource;
        this.table = table;
        this.idColumn = idColumn;
    }

    /** Memetakan baris ResultSet saat ini menjadi entity. */
    protected abstract T map(ResultSet resultSet) throws SQLException;

    @Override
    public List<T> findAll() {
        return query("SELECT * FROM " + table + " ORDER BY " + idColumn);
    }

    @Override
    public Optional<T> findById(ID id) {
        return query("SELECT * FROM " + table + " WHERE " + idColumn + " = ?", id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean deleteById(ID id) {
        return executeUpdate("DELETE FROM " + table + " WHERE " + idColumn + " = ?", id) > 0;
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return findAll().stream()
                .filter(predicate)
                .toList();
    }

    /** Menjalankan SELECT dan memetakan seluruh barisnya. */
    protected List<T> query(String sql, Object... params) {
        return execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<T> rows = new ArrayList<>();
                    while (resultSet.next()) {
                        rows.add(map(resultSet));
                    }
                    return rows;
                }
            }
        });
    }

    /** Menjalankan INSERT/UPDATE/DELETE dan mengembalikan jumlah baris terdampak. */
    protected int executeUpdate(String sql, Object... params) {
        return execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);
                return statement.executeUpdate();
            }
        });
    }

    /** Menjalankan INSERT dan mengembalikan nilai kolom {@code id} yang dibuat database. */
    protected int insertReturningId(String sql, Object... params) {
        return execute(connection -> {
            try (PreparedStatement statement =
                         connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, params);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return keys.getInt("id");
                }
            }
        });
    }

    /** Membuka koneksi, menjalankan pekerjaan, lalu menutup koneksi; SQLException dibungkus. */
    protected <R> R execute(SqlWork<R> work) {
        try (Connection connection = dataSource.getConnection()) {
            return work.apply(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal mengakses database: " + e.getMessage(), e);
        }
    }

    private static void bind(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
