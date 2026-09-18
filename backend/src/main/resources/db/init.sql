-- =====================================================
-- 融小通平台数据库初始化脚本（SQLite）
-- 说明：后端每次启动都会执行本脚本，脚本是"幂等"的，
--       即重复执行不会产生重复数据（INSERT OR IGNORE 依赖主键去重）
-- =====================================================

-- ==================== 一、建表 ====================

CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT NOT NULL UNIQUE,   -- 登录账号（唯一）
    password    TEXT NOT NULL,          -- BCrypt 加密后的密码
    real_name   TEXT,
    phone       TEXT,
    avatar      TEXT,
    role        TEXT NOT NULL,          -- FARMER 农户 / BANK 银行 / BUYER 买家 / EXPERT 专家 / ADMIN 管理员
    status      INTEGER DEFAULT 1,      -- 1 正常 / 0 禁用
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS farmer_profile (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER,
    id_card      TEXT,
    address      TEXT,                  -- 所在地区
    credit_score INTEGER DEFAULT 80,    -- 信用分 0-100
    crop         TEXT,                  -- 主营作物
    land_area    REAL                   -- 种植面积（亩）
);

CREATE TABLE IF NOT EXISTS expert_profile (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER,
    title         TEXT,                 -- 职称
    specialty     TEXT,                 -- 擅长领域
    intro         TEXT,                 -- 专家介绍
    years         INTEGER,              -- 从业年限
    rating        REAL DEFAULT 5.0,     -- 评分
    service_count INTEGER DEFAULT 0,    -- 服务次数
    status        INTEGER DEFAULT 1     -- 1 正常 / 0 停用
);

CREATE TABLE IF NOT EXISTS address (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER,
    receiver   TEXT,
    phone      TEXT,
    region     TEXT,
    detail     TEXT,
    is_default INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS news (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT,
    cover       TEXT,
    summary     TEXT,
    content     TEXT,
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS loan_product (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id     INTEGER,
    name        TEXT,
    rate        REAL,                  -- 年利率 %
    min_amount  REAL,                  -- 最小贷款金额
    max_amount  REAL,                  -- 最大贷款金额
    term_months INTEGER,               -- 贷款期限（月）
    description TEXT,
    status      INTEGER DEFAULT 1      -- 1 上架 / 0 下架
);

CREATE TABLE IF NOT EXISTS loan_application (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    farmer_id    INTEGER,
    product_id   INTEGER,
    amount       REAL,
    term         INTEGER,
    purpose      TEXT,
    status       TEXT DEFAULT 'PENDING',  -- PENDING 待审批 / APPROVED 已通过 / REJECTED 已拒绝
    apply_time   TEXT,
    approve_time TEXT,
    bank_id      INTEGER
);

CREATE TABLE IF NOT EXISTS loan_partner (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id    INTEGER,
    partner_farmer_id INTEGER,
    score             REAL,
    status            INTEGER DEFAULT 0   -- 0 未确认 / 1 已确认
);

CREATE TABLE IF NOT EXISTS match_record (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id     INTEGER,
    farmer_id   INTEGER,
    product_id  INTEGER,
    score       REAL,
    status      INTEGER DEFAULT 0,        -- 0 已推荐 / 1 已联系
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS article (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    expert_id   INTEGER,
    title       TEXT,
    category    TEXT,                     -- 种植技术 / 养殖技术 / 病虫害防治 / 政策法规
    cover       TEXT,
    content     TEXT,
    views       INTEGER DEFAULT 0,
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS question (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    farmer_id   INTEGER,
    title       TEXT,
    content     TEXT,
    expert_id   INTEGER,                  -- 指定专家（可空）
    status      INTEGER DEFAULT 0,        -- 0 待回答 / 1 已回答
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS answer (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER,
    expert_id   INTEGER,
    content     TEXT,
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS appointment (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    farmer_id    INTEGER,
    expert_id    INTEGER,
    topic        TEXT,
    description  TEXT,
    appoint_time TEXT,
    phone        TEXT,
    status       TEXT DEFAULT 'PENDING',  -- PENDING 待确认 / CONFIRMED 已确认 / DONE 已完成 / CANCELLED 已取消
    create_time  TEXT
);

CREATE TABLE IF NOT EXISTS consult_message (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    sender_id   INTEGER,
    receiver_id INTEGER,
    content     TEXT,
    is_read     INTEGER DEFAULT 0,        -- 0 未读 / 1 已读
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS product (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    farmer_id   INTEGER,
    name        TEXT,
    category    TEXT,                     -- 水果 / 蔬菜 / 粮食 / 禽畜 / 水产 / 其他
    price       REAL,
    stock       INTEGER,
    unit        TEXT,
    cover       TEXT,
    description TEXT,
    status      INTEGER DEFAULT 1,        -- 1 上架 / 0 下架
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS demand (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    buyer_id    INTEGER,
    title       TEXT,
    category    TEXT,
    quantity    REAL,
    unit        TEXT,
    budget      REAL,
    deadline    TEXT,
    description TEXT,
    status      INTEGER DEFAULT 1,        -- 1 有效 / 0 已结束
    create_time TEXT
);

CREATE TABLE IF NOT EXISTS cart (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER,
    product_id INTEGER,
    quantity   INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS orders (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no     TEXT,
    buyer_id     INTEGER,
    farmer_id    INTEGER,
    address_id   INTEGER,
    total_amount REAL,
    status       TEXT DEFAULT 'UNPAID',   -- UNPAID 待付款 / SHIPPED 待发货 / COMPLETED 已完成 / CANCELLED 已取消
    create_time  TEXT
);

CREATE TABLE IF NOT EXISTS order_item (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id   INTEGER,
    product_id INTEGER,
    price      REAL,
    quantity   INTEGER
);

-- ==================== 二、演示数据 ====================
-- 所有演示账号密码均为 123456（存储的是 BCrypt 加密后的值）

INSERT OR IGNORE INTO users (id, username, password, real_name, phone, avatar, role, status, create_time) VALUES
(1, 'farmer01', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '王志强', '13800000001', '/api/images/avatar-farmer.svg', 'FARMER', 1, '2026-08-01 10:00:00'),
(2, 'farmer02', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '李建国', '13800000002', '/api/images/avatar-farmer.svg', 'FARMER', 1, '2026-08-02 10:00:00'),
(3, 'farmer03', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '张秀兰', '13800000003', '/api/images/avatar-farmer.svg', 'FARMER', 1, '2026-08-03 10:00:00'),
(4, 'farmer04', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '赵德贵', '13800000004', '/api/images/avatar-farmer.svg', 'FARMER', 1, '2026-08-04 10:00:00'),
(5, 'buyer01', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '陈明', '13800000005', '/api/images/avatar-buyer.svg', 'BUYER', 1, '2026-08-05 10:00:00'),
(6, 'expert01', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '刘建华', '13800000006', '/api/images/avatar-expert.svg', 'EXPERT', 1, '2026-08-06 10:00:00'),
(7, 'expert02', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '王丽华', '13800000007', '/api/images/avatar-expert.svg', 'EXPERT', 1, '2026-08-07 10:00:00'),
(8, 'bank01', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '中国农业银行周口支行', '0371-88888888', '/api/images/avatar-bank.svg', 'BANK', 1, '2026-08-08 10:00:00'),
(9, 'admin', '$2a$10$zgDuagWvZkPypUH7cILDsORTaKVKoGDlC/2EoJzkPQc1gt04FupOC', '系统管理员', '13800000009', '/api/images/avatar-admin.svg', 'ADMIN', 1, '2026-08-09 10:00:00');

INSERT OR IGNORE INTO farmer_profile (id, user_id, id_card, address, credit_score, crop, land_area) VALUES
(1, 1, '412701198001010011', '河南省周口市沈丘县', 88, '小麦', 120),
(2, 2, '412701198202020022', '河南省周口市商水县', 92, '玉米', 85),
(3, 3, '412728197903030033', '河南省驻马店市平舆县', 76, '花生', 60),
(4, 4, '371728197704040044', '山东省菏泽市曹县', 81, '大蒜', 45);

INSERT OR IGNORE INTO expert_profile (id, user_id, title, specialty, intro, years, rating, service_count, status) VALUES
(1, 6, '高级农艺师', '小麦种植、病虫害防治', '从事农业技术推广工作 20 余年，长期深入田间地头指导农户科学种田，累计服务农户上千家。', 20, 4.9, 156, 1),
(2, 7, '畜牧师', '禽畜养殖、疫病防控', '专注蛋鸡与生猪养殖技术研究，擅长养殖场规划与常见疫病防控。', 15, 4.7, 98, 1);

INSERT OR IGNORE INTO news (id, title, cover, summary, content, create_time) VALUES
(1, '融小通平台正式上线，助力乡村振兴', '/api/images/news1.svg', '平台整合银行融资、农产品销售、农技服务三大板块，为农户提供一站式服务。', '融小通农业综合服务平台今日正式上线。平台以农户为中心，整合银行融资授信、农产品线上购销、农业技术咨询指导三大业务，通过智能匹配算法实现融资、销售、农技服务一体化，助力乡村振兴战略落地。', '2026-09-01 09:00:00'),
(2, '秋季小麦播种指南发布，专家支招科学种田', '/api/images/news2.svg', '秋播在即，平台特邀农业专家发布小麦播种技术要点。', '秋分将至，小麦秋播进入关键时期。专家建议：一是选用优良品种；二是适时适量播种；三是做好土壤处理，预防地下害虫。', '2026-09-10 09:00:00'),
(3, '普惠金融进乡村活动圆满举行', '/api/images/news3.svg', '多家合作银行深入乡村，现场为农户讲解惠农贷款政策。', '本次活动共有 200 余名农户参与，现场解答融资疑问 50 余条，帮助 30 余户农户完成贷款意向登记。', '2026-09-15 09:00:00');

INSERT OR IGNORE INTO loan_product (id, bank_id, name, rate, min_amount, max_amount, term_months, description, status) VALUES
(1, 8, '惠农贷', 3.85, 10000, 100000, 12, '面向从事种植、养殖的农户发放的普惠型贷款，利率低、审批快，无需抵押。', 1),
(2, 8, '农机贷', 4.35, 5000, 50000, 24, '支持农户购置农机具的专项贷款，最长可分 24 期偿还。', 1),
(3, 8, '乡村振兴创业贷', 4.10, 20000, 300000, 36, '支持返乡创业、发展特色农业的大额贷款，额度高、期限灵活。', 1);

INSERT OR IGNORE INTO loan_application (id, farmer_id, product_id, amount, term, purpose, status, apply_time, approve_time, bank_id) VALUES
(1, 1, 1, 50000, 12, '购置农机具，扩大种植规模', 'PENDING', '2026-09-12 14:30:00', NULL, 8);

INSERT OR IGNORE INTO article (id, expert_id, title, category, cover, content, views, create_time) VALUES
(1, 6, '小麦越冬期管理要点', '种植技术', '/api/images/article1.svg', '小麦越冬期管理直接影响来年产量，需重点做好三件事：一、适时冬灌，保证土壤墒情；二、镇压防冻，防止麦苗受冻；三、追施冬肥，促进根系生长。', 326, '2026-09-05 10:00:00'),
(2, 6, '玉米病虫害绿色防控技术', '病虫害防治', '/api/images/article2.svg', '玉米螟和玉米大斑病是黄淮海地区玉米的主要病虫害。绿色防控措施包括：选用抗病品种、合理轮作、生物防治（释放赤眼蜂）、科学用药。', 258, '2026-09-08 10:00:00'),
(3, 6, '秋季蔬菜大棚温湿度管理', '种植技术', '/api/images/article3.svg', '秋季昼夜温差大，大棚管理要掌握"白天通风降湿、夜间保温防寒"的原则，番茄、黄瓜等作物白天保持 25-28℃，夜间不低于 12℃。', 189, '2026-09-11 10:00:00'),
(4, 7, '蛋鸡养殖常见问题与对策', '养殖技术', '/api/images/article4.svg', '蛋鸡产蛋率下降的常见原因：光照不足、饲料营养不均衡、应激反应。对策：保持每天 16 小时光照，补充维生素和微量元素，减少噪音干扰。', 412, '2026-09-13 10:00:00');

INSERT OR IGNORE INTO question (id, farmer_id, title, content, expert_id, status, create_time) VALUES
(1, 1, '小麦叶片发黄是什么原因？', '我家小麦最近叶片发黄，是从叶尖开始的，请问是什么原因？该怎么处理？', 6, 1, '2026-09-14 09:00:00'),
(2, 3, '大棚西红柿如何预防白粉病？', '今年大棚西红柿长势不错，担心得白粉病，想请教预防措施。', 6, 1, '2026-09-15 09:00:00');

INSERT OR IGNORE INTO answer (id, question_id, expert_id, content, create_time) VALUES
(1, 1, 6, '叶尖发黄一般有三种可能：一是缺水干旱；二是缺氮；三是小麦纹枯病。建议先观察土壤墒情，若偏干可适量浇水，同时追施尿素 10 公斤/亩。若叶片有褐色病斑，则可能是纹枯病，需及时喷施杀菌剂。', '2026-09-14 15:20:00'),
(2, 2, 6, '白粉病预防要抓好三点：一、控制棚内湿度，加强通风；二、发病前喷施保护性杀菌剂（如百菌清）；三、合理密植，及时摘除老叶病叶。', '2026-09-15 16:40:00');

INSERT OR IGNORE INTO product (id, farmer_id, name, category, price, stock, unit, cover, description, status, create_time) VALUES
(1, 1, '优质冬小麦', '粮食', 2.8, 5000, '斤', '/api/images/p-wheat.svg', '自家种植的优质冬小麦，颗粒饱满，水分低，适合加工面粉。', 1, '2026-09-01 10:00:00'),
(2, 1, '石磨面粉 5kg', '粮食', 25.0, 200, '袋', '/api/images/p-flour.svg', '传统石磨工艺加工，保留小麦原香，无任何添加剂。', 1, '2026-09-02 10:00:00'),
(3, 2, '黄玉米（饲料级）', '粮食', 2.2, 8000, '斤', '/api/images/p-corn.svg', '新季黄玉米，籽粒饱满，适合饲料加工与酿造。', 1, '2026-09-03 10:00:00'),
(4, 2, '甜糯玉米 10 根装', '粮食', 19.9, 150, '箱', '/api/images/p-sweetcorn.svg', '当日现摘甜糯玉米，香甜软糯，真空锁鲜包装。', 1, '2026-09-04 10:00:00'),
(5, 3, '鲜花生（湿花生）', '其他', 4.5, 600, '斤', '/api/images/p-peanut.svg', '沙土地种植的鲜花生，果实饱满，适合煮食和榨油。', 1, '2026-09-05 10:00:00'),
(6, 3, '散养土鸡蛋 30 枚', '禽畜', 35.0, 100, '箱', '/api/images/p-egg.svg', '林下散养土鸡所产，蛋黄大、口感香，营养丰富。', 1, '2026-09-06 10:00:00'),
(7, 4, '新鲜紫皮大蒜', '蔬菜', 6.8, 800, '斤', '/api/images/p-garlic.svg', '紫皮大蒜，蒜瓣饱满，辣味浓郁，耐储存。', 1, '2026-09-07 10:00:00'),
(8, 4, '农家土蜂蜜 500g', '其他', 58.0, 60, '瓶', '/api/images/p-honey.svg', '槐花蜜，农家自产，纯天然无添加。', 1, '2026-09-08 10:00:00');

INSERT OR IGNORE INTO demand (id, buyer_id, title, category, quantity, unit, budget, deadline, description, status, create_time) VALUES
(1, 5, '求购优质小麦 5000 斤', '粮食', 5000, '斤', 15000, '2026-10-01', '面粉厂原料采购，要求水分 ≤ 13%，杂质少，可长期合作。', 1, '2026-09-10 10:00:00'),
(2, 5, '求购散养土鸡蛋 2000 枚', '禽畜', 2000, '枚', 1800, '2026-09-25', '社区团购渠道供货，要求新鲜、破损率低。', 1, '2026-09-12 10:00:00');

INSERT OR IGNORE INTO address (id, user_id, receiver, phone, region, detail, is_default) VALUES
(1, 5, '陈明', '13800000005', '河南省郑州市金水区', '农业路 12 号农产品批发市场 B 区 3 号', 1);

INSERT OR IGNORE INTO appointment (id, farmer_id, expert_id, topic, description, appoint_time, phone, status, create_time) VALUES
(1, 1, 6, '小麦病虫害防治现场指导', '今年小麦播种后想请专家到地里看看土壤情况，指导播种和后期病虫害防治。', '2026-09-20 09:00', '13800000001', 'PENDING', '2026-09-16 10:00:00');

INSERT OR IGNORE INTO orders (id, order_no, buyer_id, farmer_id, address_id, total_amount, status, create_time) VALUES
(1, 'RXT202609120001', 5, 1, 1, 56.0, 'COMPLETED', '2026-09-12 11:00:00');

INSERT OR IGNORE INTO order_item (id, order_id, product_id, price, quantity) VALUES
(1, 1, 2, 25.0, 2);
