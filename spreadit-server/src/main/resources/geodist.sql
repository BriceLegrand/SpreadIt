DROP PROCEDURE IF EXISTS geodist;
DELIMITER $$

-- This procedure select all users within dist miles of the location of the user identified by userid (its server_id)
CREATE PROCEDURE geodist (IN userid INT, IN dist DOUBLE)
BEGIN
	DECLARE mylon DOUBLE;
	DECLARE mylat DOUBLE;
	DECLARE lon1 DOUBLE;
	DECLARE lon2 DOUBLE;
	DECLARE lat1 DOUBLE;
	DECLARE lat2 DOUBLE;

	-- get the original lon and lat for the userid 
	SELECT longitude, latitude INTO mylon, mylat FROM user WHERE server_id=userid;
    
	-- calculate lon and lat for the rectangle:
	SET lon1 = mylon-dist/abs(cos(radians(mylat))*69);
	SET lon2 = mylon+dist/abs(cos(radians(mylat))*69);
	SET lat1 = mylat-(dist/69);
	SET lat2 = mylat+(dist/69);

	-- run the query:
	SELECT server_id, gcm_id,
	3956 * 2 * ASIN(SQRT( POWER(SIN((mylat - latitude) * pi()/180 / 2), 2) +COS(mylat * pi()/180) * COS(latitude * pi()/180) *POWER(SIN((mylon - longitude) * pi()/180 / 2), 2) )) AS distance
	FROM user
	WHERE server_id<>userid
		AND longitude BETWEEN lon1 AND lon2
		AND latitude BETWEEN lat1 AND lat2 
	HAVING distance < dist;
END$$

DELIMITER ;