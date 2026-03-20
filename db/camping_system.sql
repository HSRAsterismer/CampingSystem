CREATE DATABASE IF NOT EXISTS camping_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE camping_system;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,
  display_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled BIT NOT NULL,
  last_login_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS camping_site (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  province VARCHAR(50) NOT NULL,
  city VARCHAR(50) NOT NULL,
  address VARCHAR(200) NOT NULL,
  latitude DECIMAL(10, 6) NOT NULL,
  longitude DECIMAL(10, 6) NOT NULL,
  capacity INT NOT NULL,
  base_price DECIMAL(10, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  scenic_level INT NOT NULL,
  eco_index INT NOT NULL,
  facilities TEXT NULL,
  tags TEXT NULL,
  description TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reservation_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  reservation_no VARCHAR(32) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  site_id BIGINT NOT NULL,
  contact_name VARCHAR(50) NOT NULL,
  contact_phone VARCHAR(20) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  guest_count INT NOT NULL,
  tent_count INT NOT NULL,
  total_amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  risk_level VARCHAR(20) NOT NULL,
  risk_tags VARCHAR(255) NULL,
  source_ip VARCHAR(100) NULL,
  remark VARCHAR(255) NULL,
  paid_at DATETIME NULL,
  cancelled_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  order_no VARCHAR(32) NOT NULL UNIQUE,
  reservation_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  channel VARCHAR(20) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  transaction_no VARCHAR(64) NULL,
  paid_at DATETIME NULL,
  settled_at DATETIME NULL,
  operator_name VARCHAR(50) NULL,
  settlement_note VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS observation_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  site_id BIGINT NOT NULL,
  observer_id BIGINT NOT NULL,
  species_name VARCHAR(100) NOT NULL,
  category VARCHAR(50) NOT NULL,
  quantity INT NOT NULL,
  weather VARCHAR(50) NULL,
  observation_time DATETIME NOT NULL,
  photo_url VARCHAR(255) NULL,
  coordinates VARCHAR(100) NULL,
  habitat VARCHAR(100) NULL,
  rarity_level VARCHAR(30) NULL,
  environmental_score INT NULL,
  notes TEXT NULL,
  status VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fraud_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  user_id BIGINT NULL,
  reservation_id BIGINT NULL,
  event_type VARCHAR(50) NOT NULL,
  risk_level VARCHAR(20) NOT NULL,
  action_taken VARCHAR(20) NOT NULL,
  source_ip VARCHAR(100) NULL,
  detail TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_user (id, created_at, updated_at, username, password_hash, display_name, phone, role, enabled, last_login_at)
VALUES
  (1, '2026-03-10 09:00:00', '2026-03-10 09:00:00', 'admin', '713ba056104ed00e063473dbfd17091ced4241078b574993ad3686b48b1c9997', '系统管理员', '13800138000', 'ADMIN', b'1', NULL),
  (2, '2026-03-10 09:05:00', '2026-03-10 09:05:00', 'camper', '499adfe40f0fd5ed6ca5e10e580437976ad935292ee545ed0a06abc16862c7f9', '星野营地主理人', '13800138001', 'USER', b'1', NULL),
  (3, '2026-03-10 09:10:00', '2026-03-10 09:10:00', 'eco', 'f9ad37a8e3779c5f506f7163ec060ba1c39702eb9a6ae1d38dc55f5da0253210', '生态观察员', '13800138002', 'OBSERVER', b'1', NULL)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO camping_site (id, created_at, updated_at, code, name, province, city, address, latitude, longitude, capacity, base_price, status, scenic_level, eco_index, facilities, tags, description)
VALUES
  (1, '2026-03-10 10:00:00', '2026-03-10 10:00:00', 'CAMP-001', '祁连星河营地', '甘肃省', '张掖市', '丹霞地貌观景带北侧', 38.972510, 100.448120, 18, 268.00, 'OPEN', 5, 92, '观星,徒步,供电,应急医疗', '高海拔,星空,摄影', '以星空观测和低干扰营地管理著称的旗舰露营点'),
  (2, '2026-03-10 10:05:00', '2026-03-10 10:05:00', 'CAMP-002', '雨林溪谷营地', '云南省', '西双版纳州', '勐腊县热带雨林生态廊道', 21.932410, 101.263560, 14, 328.00, 'OPEN', 5, 95, '科普讲解,昆虫灯诱,生态巡护', '雨林,观鸟,水系', '侧重生物多样性观察与微栖息地记录'),
  (3, '2026-03-10 10:10:00', '2026-03-10 10:10:00', 'CAMP-003', '海岸风语营地', '福建省', '宁德市', '霞浦东冲半岛生态海岸', 26.914200, 120.022400, 12, 298.00, 'OPEN', 4, 88, '冲洗区,露台,电源补给', '海岸,日出,海鸟', '适合海岸带生态观察与轻量化预订体验'),
  (4, '2026-03-10 10:15:00', '2026-03-10 10:15:00', 'CAMP-004', '雪峰松林营地', '四川省', '阿坝州', '四姑娘山双桥沟保护缓冲区', 31.130500, 102.886400, 10, 368.00, 'MAINTENANCE', 5, 90, '防寒补给,巡护联动,高山告警', '雪山,松林,高海拔', '用于高山生态体验与科考配套，当前维护中'),
  (5, '2026-03-10 10:20:00', '2026-03-10 10:20:00', 'CAMP-005', '湖湾晨雾营地', '浙江省', '湖州市', '安吉天荒坪湖湾步道', 30.638200, 119.675900, 16, 238.00, 'OPEN', 4, 86, '亲子观察,夜巡,基础餐饮', '湖湾,湿地,亲子', '兼顾家庭露营与湿地鸟类晨间观察')
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO reservation_order (id, created_at, updated_at, reservation_no, user_id, site_id, contact_name, contact_phone, start_date, end_date, guest_count, tent_count, total_amount, status, risk_level, risk_tags, source_ip, remark, paid_at, cancelled_at)
VALUES
  (1, '2026-03-15 10:00:00', '2026-03-15 12:00:00', 'RSV20260311001', 2, 1, '星野营地主理人', '13800138001', '2026-03-19', '2026-03-21', 4, 2, 1072.00, 'CONFIRMED', 'LOW', '', '127.0.0.1', '观星摄影预订', '2026-03-15 11:00:00', NULL),
  (2, '2026-03-16 09:00:00', '2026-03-16 10:00:00', 'RSV20260312002', 2, 2, '星野营地主理人', '13800138001', '2026-03-22', '2026-03-24', 6, 3, 1968.00, 'PENDING_PAYMENT', 'MEDIUM', 'weekend_peak,multi_tent', '127.0.0.1', '团队生态观察活动', NULL, NULL),
  (3, '2026-03-14 09:20:00', '2026-03-14 11:20:00', 'RSV20260314003', 3, 3, '生态观察员', '13800138002', '2026-03-18', '2026-03-19', 2, 1, 298.00, 'CONFIRMED', 'LOW', '', '127.0.0.8', '海鸟观察单晚预订', '2026-03-14 10:20:00', NULL)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO payment_order (id, created_at, updated_at, order_no, reservation_id, user_id, channel, amount, status, transaction_no, paid_at, settled_at, operator_name, settlement_note)
VALUES
  (1, '2026-03-15 10:50:00', '2026-03-16 10:00:00', 'PAY20260311001', 1, 2, 'WECHAT', 1072.00, 'SETTLED', 'TXN20260311001', '2026-03-15 11:00:00', '2026-03-16 10:00:00', '系统管理员', '批量自动结算'),
  (2, '2026-03-14 10:10:00', '2026-03-14 10:20:00', 'PAY20260314003', 3, 3, 'ALIPAY', 298.00, 'PAID', 'TXN20260314003', '2026-03-14 10:20:00', NULL, '生态观察员', NULL)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO observation_record (id, created_at, updated_at, site_id, observer_id, species_name, category, quantity, weather, observation_time, photo_url, coordinates, habitat, rarity_level, environmental_score, notes, status)
VALUES
  (1, '2026-03-16 06:15:00', '2026-03-16 06:20:00', 1, 3, '黑鹳', '鸟类', 2, '晴', '2026-03-16 06:00:00', NULL, '38.972510,100.448120', '栖息于岩壁热流区域', '稀有', 93, '迁徙路径稳定，建议保持低噪音巡护', 'VERIFIED'),
  (2, '2026-03-15 08:15:00', '2026-03-15 08:20:00', 2, 3, '中华鬣羚', '兽类', 1, '多云', '2026-03-15 08:00:00', NULL, '21.932410,101.263560', '林下灌丛', '珍稀', 96, '发现新鲜蹄印，需继续监测活动轨迹', 'VERIFIED'),
  (3, '2026-03-17 07:15:00', '2026-03-17 07:20:00', 5, 2, '白鹭', '鸟类', 6, '晨雾', '2026-03-17 07:00:00', NULL, '30.638200,119.675900', '湖湾浅滩', '常见', 82, '清晨集群觅食，湿地水位正常', 'SUBMITTED'),
  (4, '2026-03-13 05:15:00', '2026-03-13 05:20:00', 3, 3, '滨鹬', '鸟类', 12, '晴', '2026-03-13 05:00:00', NULL, '26.914200,120.022400', '潮间带', '常见', 85, '数量波动与潮汐相关', 'VERIFIED')
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

INSERT INTO fraud_event (id, created_at, updated_at, user_id, reservation_id, event_type, risk_level, action_taken, source_ip, detail)
VALUES
  (1, '2026-03-16 18:00:00', '2026-03-16 18:05:00', 2, 2, 'RESERVATION_REVIEW', 'MEDIUM', 'REVIEW', '127.0.0.1', '周末高峰+多帐篷组合，系统已标记重点监测'),
  (2, '2026-03-17 00:00:00', '2026-03-17 00:05:00', 2, NULL, 'RESERVATION_BLOCK', 'HIGH', 'BLOCK', '127.0.0.1', '短时间内重复提交相似预订请求，已被拦截'),
  (3, '2026-03-17 04:00:00', '2026-03-17 04:05:00', 3, NULL, 'PAYMENT_RECHECK', 'MEDIUM', 'REVIEW', '127.0.0.8', '支付设备切换频繁，建议人工复核')
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);
