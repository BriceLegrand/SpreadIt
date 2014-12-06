package spreadit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

public class SqlHandler {
	private static final DriverManagerDataSource dataSource;
	private static final JdbcTemplate jdbcTemplate;
	static {
		dataSource = new DriverManagerDataSource();
	    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
	    dataSource.setUrl("jdbc:mysql://localhost:3306/spreadit");
	    dataSource.setUsername("root");
	    dataSource.setPassword("root");
	    jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private SqlHandler() { }
	
	public static void create_tables() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS user");
		jdbcTemplate.execute("CREATE TABLE user ("
		    + "server_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,"
		    + "gcm_id VARCHAR(100) NOT NULL UNIQUE,"
		    + "latitude DOUBLE,"
		    + "longitude DOUBLE,"
		    + "last_use_time TIMESTAMP NOT NULL"
		+ ")");
	}
	
	@Transactional
	synchronized public static int login(final String gcm_id) {
		// if already logged in, clear data
		jdbcTemplate.update("DELETE FROM user WHERE gcm_id=?", gcm_id);

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
				new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps =
								connection.prepareStatement("INSERT INTO user (gcm_id, last_use_time) VALUES (?, NOW())", new String[] {"gcm_id"});
						ps.setString(1, gcm_id);
						return ps;
					}
				},
				keyHolder);

		return keyHolder.getKey().intValue();
	}

	public static void logout(final int server_id) {
		jdbcTemplate.update("DELETE FROM user WHERE server_id=?", server_id);
	}

	public static String get_gcm_id(final int server_id) throws TtlSqlException {
		reset_ttl_if_living(server_id);
		return jdbcTemplate.queryForObject("SELECT gcm_id FROM user WHERE server_id=?", new Object[]{server_id}, String.class);
	}

	public static List<User> retrieve_users(final int server_id, final double distance_km) throws TtlSqlException {
		reset_ttl_if_living(server_id);
		
		double dist_miles = 0.621371192 * distance_km;
		
		// Get the server_ids of users within distance
		List<User> users = jdbcTemplate.query(
		        "CALL geodist(?, ?)",
		        new RowMapper<User>() {
		            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
		                return new User(rs.getInt(1), rs.getString(2));
		            }
		        }
				, server_id, dist_miles);
		
		// Verify every user is still living (ttl)
		List<User> to_remove = new ArrayList<User>();
		for (User user : users) {
			try {
				verify_ttl(user.getServer_id());
			} catch (TtlSqlException e) {
				to_remove.add(user);
			}
		}
		users.removeAll(to_remove);
		
		return users;
	}
	
	public static void update_location(final int server_id, final double latitude, final double longitude) throws TtlSqlException {
		reset_ttl_if_living(server_id);

		jdbcTemplate.update("UPDATE user SET latitude=?, longitude=? WHERE server_id=?", latitude, longitude, server_id);
	}
	
	public static void reset_ttl_if_living(final int server_id) throws TtlSqlException {
		verify_ttl(server_id);
		// if verification did not throw, reset ttl
		jdbcTemplate.update("UPDATE user SET last_use_time=NOW() WHERE server_id=?", server_id);	
	}
	
	public static void verify_ttl(final int server_id) throws TtlSqlException {
		// delete user if ttl expired
		jdbcTemplate.update("DELETE FROM user WHERE server_id=? AND TIMESTAMPADD(MINUTE, ?, last_use_time)<NOW()", server_id, Application.time_to_live_min);

		// throw specific exception if no more exist
		try {
			jdbcTemplate.queryForObject("SELECT server_id FROM user WHERE server_id=?", new Object[]{server_id}, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			throw new TtlSqlException();
		}
	}
}
