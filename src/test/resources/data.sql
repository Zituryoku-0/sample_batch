MERGE INTO userInfo (userId, userName, userPassword, latest_access_time) KEY(userId) VALUES('sampleUserId1', 'sample UserName1', 'abcdefgh', TIMESTAMP '2000-01-01 00:00:00');
MERGE INTO userInfo (userId, userName, userPassword, latest_access_time) KEY(userId) VALUES('sampleUserId2', 'sample UserName2', 'abcdefgh', CURRENT_TIMESTAMP);
