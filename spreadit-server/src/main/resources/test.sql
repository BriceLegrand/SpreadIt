# TEST : please use a freshly created table with nothing inserted

# 5 first users within 1 mile in Compiegne
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

# 5 last users within 1 mile in Paris
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


# Run tests
CALL geodist(1, 1); # users within 1 mile of the user with server_id=1
CALL geodist(3, 1); # users within 1 mile of the user with server_id=3
CALL geodist(6, 1); # users within 1 mile of the user with server_id=6
CALL geodist(9, 1); # users within 1 mile of the user with server_id=6