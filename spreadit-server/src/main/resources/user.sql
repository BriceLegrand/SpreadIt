# Table
DELETE FROM user;
DROP TABLE user;
CREATE TABLE user (
    server_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    gcm_id VARCHAR(100) NOT NULL,
    phone_id VARCHAR(100) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    last_use_time TIMESTAMP NOT NULL
);

# Rows
	# login user
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('gcm_id', 'mac_adress', NOW());
	# update position
UPDATE user SET last_use_time=NOW(), latitude=49.4184365, longitude=2.8212913 WHERE server_id=1;
	# reset time to live -> to be called foreach request received with a specific
UPDATE user SET last_use_time=NOW() WHERE server_id=1;
	# retrieve (we don't use location nor time to live)
SELECT server_id, gcm_id, phone_id FROM user WHERE server_id=1;
	# logout user
DELETE FROM user WHERE server_id=1;

# Time to live clearer
DROP EVENT ClearUser;
CREATE EVENT ClearUser ON SCHEDULE EVERY 15 MINUTE
DO # 15 to 30 min time to live
DELETE FROM user WHERE TIMESTAMPADD(MINUTE, 15, last_use_time)<NOW();
SHOW EVENTS;

SET GLOBAL general_log = 'ON';
