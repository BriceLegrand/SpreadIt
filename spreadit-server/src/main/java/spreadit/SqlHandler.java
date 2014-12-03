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
	    dataSource.setUrl("jdbc:mysql://89.88.245.68:8080/spreadit");
	    dataSource.setUsername("root");
	    dataSource.setPassword("root");
	    jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private SqlHandler() { }
	
	public static void create_tables() {
		jdbcTemplate.execute("DROP TABLE user IF EXISTS");
		jdbcTemplate.execute("CREATE TABLE user ("
		    + "server_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,"
		    + "gcm_id VARCHAR(100) NOT NULL UNIQUE,"
		    + "latitude DOUBLE,"
		    + "longitude DOUBLE,"
		    + "last_use_time TIMESTAMP NOT NULL"
		+ ")");
		
		jdbcTemplate.execute("DROP PROCEDURE IF EXISTS geodist");
		
		//TODO verify this works
		jdbcTemplate.execute("DELIMITER $$\n" + 
			"CREATE PROCEDURE geodist (IN userid INT, IN dist DOUBLE)\n" + 
			"BEGIN\n" + 
			"	DECLARE mylon DOUBLE;\n" + 
			"	DECLARE mylat DOUBLE;\n" + 
			"	DECLARE lon1 DOUBLE;\n" + 
			"	DECLARE lon2 DOUBLE;\n" + 
			"	DECLARE lat1 DOUBLE;\n" + 
			"	DECLARE lat2 DOUBLE;\n" + 
			"	SELECT longitude, latitude INTO mylon, mylat FROM user WHERE server_id=userid;\n" + 
			"	SET lon1 = mylon-dist/abs(cos(radians(mylat))*69);\n" + 
			"	SET lon2 = mylon+dist/abs(cos(radians(mylat))*69);\n" + 
			"	SET lat1 = mylat-(dist/69);\n" + 
			"	SET lat2 = mylat+(dist/69);\n" + 
			"	SELECT server_id, gcm_id,\n" + 
			"	3956 * 2 * ASIN(SQRT( POWER(SIN((mylat - latitude) * pi()/180 / 2), 2) +COS(mylat * pi()/180) * COS(latitude * pi()/180) *POWER(SIN((mylon - longitude) * pi()/180 / 2), 2) )) AS distance\n" + 
			"	FROM user\n" + 
			"	WHERE server_id<>userid\n" + 
			"		AND longitude BETWEEN lon1 AND lon2\n" + 
			"		AND latitude BETWEEN lat1 AND lat2 \n" + 
			"	HAVING distance < dist;\n" + 
			"END$$\n" + 
		"DELIMITER ;");
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

	public static List<Integer> retrieve_users(int server_id, double distance_km) throws TtlSqlException {
		reset_ttl_if_living(server_id);
		
		double dist_miles = 0.621371192 * distance_km;
		
		// Get the server_ids of users within distance
		List<Integer> server_ids = jdbcTemplate.query(
		        "CALL geodist(?, ?)", // server_id, distance in miles
		        new RowMapper<Integer>() {
		            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
		                return rs.getInt(0);
		            }
		        },
		        server_id, dist_miles);
		
		// Verify every user is still living (ttl)
		List<Integer> to_remove = new ArrayList<Integer>();
		for (Integer id : server_ids) {
			try {
				verify_ttl(id);
			} catch (TtlSqlException e) {
				to_remove.add(id);
			}
		}
		server_ids.removeAll(to_remove);
		
		return server_ids;
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
		jdbcTemplate.update("DELETE FROM user WHERE server_id=? HAVING TIMESTAMPADD(MINUTE, 15, last_use_time)<NOW()", server_id);

		// throw specific exception if no more exist
		try {
			jdbcTemplate.queryForObject("SELECT server_id FROM user WHERE server_id=?", new Object[]{server_id}, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			throw new TtlSqlException("Time to live expired for item server_id");
		}
	}
}
