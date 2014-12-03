# Table
DELETE FROM user;
DROP TABLE user IF EXISTS;
CREATE TABLE user (
    server_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    gcm_id VARCHAR(100) NOT NULL UNIQUE,
    latitude DOUBLE,
    longitude DOUBLE,
    last_use_time TIMESTAMP NOT NULL
);

# Rows
	# login user
INSERT INTO user (gcm_id, last_use_time) VALUES ('gcm_id', NOW());
	# get back server_id
SELECT LAST_INSERT_ID() FROM user;
	# update position
UPDATE user SET latitude=49.4184365, longitude=2.8212913 WHERE server_id=1;
	# verify then clean or reset time to live
DELETE FROM user WHERE server_id=1 HAVING TIMESTAMPADD(MINUTE, 15, last_use_time)<NOW();
SELECT server_id FROM user WHERE server_id=1;
UPDATE user SET last_use_time=NOW() WHERE server_id=1;
	# logout user
DELETE FROM user WHERE server_id=1;
	#retrieve users from a certain distance
CALL geodist(1, 0.5); # server_id, distance in miles

# Time to live clearer TO DEBUG !
DROP EVENT ClearUser;
CREATE EVENT ClearUser ON SCHEDULE EVERY 15 MINUTE
DO # 15 to 30 min time to live
DELETE FROM user WHERE TIMESTAMPADD(MINUTE, 15, last_use_time)<NOW();
SHOW EVENTS;