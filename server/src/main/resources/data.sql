INSERT INTO meeting (id,title,description,status,registration_start,registration_end,checkin_start,checkin_end,points_reward)
SELECT 1,'数智化技术交流会','用于本地演示报名、定位签到和积分闭环','OPEN','2020-01-01 00:00:00','2035-12-31 23:59:59','2020-01-01 00:00:00','2035-12-31 23:59:59',20
WHERE NOT EXISTS (SELECT 1 FROM meeting WHERE id=1);
INSERT INTO checkin_point (id,meeting_id,point_code,point_name,latitude,longitude,allowed_distance_meters,status)
SELECT 1,1,'SH-SJ-001','上海松江会场入口',31.0300000,121.2200000,500,'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM checkin_point WHERE id=1);
