package spreadit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
	
	
	synchronized public static int login(String gcm_id) {
		try {
			jdbcTemplate.update("INSERT INTO user (gcm_id, last_use_time) VALUES (?, NOW())", gcm_id);
		} catch (DataIntegrityViolationException e) {
			// gcm_id already present in DB (unique sql constraint violated)
			jdbcTemplate.update("DELETE FROM user WHERE gcm_id=?)", gcm_id);
			jdbcTemplate.update("INSERT INTO user (gcm_id, last_use_time) VALUES (?, NOW())", gcm_id);
		}
		
		int server_id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID() FROM user", Integer.class);
		return server_id;
	}

	public static void logout(int server_id) {
		jdbcTemplate.update("DELETE FROM user WHERE server_id=?", server_id);
	}

	public static String get_gcm_id(int server_id) throws TtlSqlException {
		reset_ttl_if_living(server_id);
		return jdbcTemplate.queryForObject("SELECT gcm_id FROM user WHERE server_id=?", new Object[]{server_id}, String.class);
	}

	public static List<User> retrieve_users(int server_id, double distance_km) throws TtlSqlException {
		reset_ttl_if_living(server_id);
		
		double dist_miles = 0.621371192 * distance_km;
		
		// Get the server_ids of users within distance
		List<User> users = jdbcTemplate.query(
		        "CALL geodist(?, ?)", new Object[]{server_id, dist_miles},
		        new RowMapper<User>() {
		            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
		                return new User(rs.getInt(0), rs.getString(1));
		            }
		        });
		
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
	
	public static void update_location(int server_id, double latitude, double longitude) throws TtlSqlException {
		reset_ttl_if_living(server_id);

		jdbcTemplate.update("UPDATE user SET latitude=?, longitude=? WHERE server_id=?", latitude, longitude, server_id);
	}
	
	public static void reset_ttl_if_living(int server_id) throws TtlSqlException {
		verify_ttl(server_id);
		// if verification did not throw, reset ttl
		jdbcTemplate.update("UPDATE user SET last_use_time=NOW() WHERE server_id=?", server_id);	
	}
	
	public static void verify_ttl(int server_id) throws TtlSqlException {
		// delete user if ttl expired
		jdbcTemplate.update("DELETE FROM user WHERE server_id=? AND TIMESTAMPADD(MINUTE, ?, last_use_time)<NOW()", server_id, Application.time_to_live_min);

		// throw specific exception if no more exist
		try {
			jdbcTemplate.queryForObject("SELECT server_id FROM user WHERE server_id=?", new Object[]{server_id}, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			throw new TtlSqlException("Time to live expired or user not logged in");
		}
	}
}
