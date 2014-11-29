# TEST : please use a freshly created table with nothing inserted

# 1-5 users within very close in Compiegne
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('compi', 'compi', NOW());
UPDATE user SET last_use_time=NOW(), latitude=49.4184365, longitude=2.8212913 WHERE server_id=1;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('compi', 'compi', NOW());
UPDATE user SET last_use_time=NOW(), latitude=49.419080, longitude=2.823950 WHERE server_id=2;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('compi', 'compi', NOW());
UPDATE user SET last_use_time=NOW(), latitude=49.417995, longitude=2.823950 WHERE server_id=3;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('compi', 'compi', NOW());
UPDATE user SET last_use_time=NOW(), latitude=49.419080, longitude=2.820452 WHERE server_id=4;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('compi', 'compi', NOW());
UPDATE user SET last_use_time=NOW(), latitude=49.417995, longitude=2.820452 WHERE server_id=5;

# 6-10 users within very close in Paris
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('paris', 'paris', NOW());
UPDATE user SET last_use_time=NOW(), latitude=48.865338, longitude=2.320967 WHERE server_id=6;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('paris', 'paris', NOW());
UPDATE user SET last_use_time=NOW(), latitude=48.864523, longitude=2.324132 WHERE server_id=7;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('paris', 'paris', NOW());
UPDATE user SET last_use_time=NOW(), latitude=48.863538, longitude=2.324132 WHERE server_id=8;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('paris', 'paris', NOW());
UPDATE user SET last_use_time=NOW(), latitude=48.864523, longitude=2.319712 WHERE server_id=9;
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('paris', 'paris', NOW());
UPDATE user SET last_use_time=NOW(), latitude=48.863538, longitude=2.319712 WHERE server_id=10;

# 11-13 users with null location
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('null', 'null', NOW());
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('null', 'null', NOW());
INSERT INTO user (gcm_id, phone_id, last_use_time) VALUES ('null', 'null', NOW());


# Run tests
CALL geodist(1, 0.5); # users within 0.5 mile of the user with server_id=1
CALL geodist(3, 0.5);
CALL geodist(6, 0.5);
CALL geodist(9, 0.5);