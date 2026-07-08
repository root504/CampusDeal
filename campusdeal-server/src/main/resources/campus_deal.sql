-- =============================================
-- CampusDeal 校园二手交易平台数据库脚本 (v4.0 全面修复版)
-- 修复要点：
--   1) Product status: 0=待审核, 1=在售, 2=已下架/已拒绝
--   2) 16个商品均分给4个普通用户（排除管理员id=1）
--   3) 3个订单分配给3个不同普通用户
--   4) 新增 admins 表 + announcements 表
--   5) 消息补全 conversation_id，订单间增加完整对话记录
--   6) 补全 system_logs 表 + 10条测试日志
--   7) 补全 order_items 订单明细
-- =============================================

CREATE DATABASE IF NOT EXISTS campus_deal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_deal;

-- =============================================
-- 1. 用户表 (users)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    phone       VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号（登录账号）',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt）',
    student_id  VARCHAR(20)  DEFAULT NULL UNIQUE COMMENT '学号',
    wechat_id   VARCHAR(50)  DEFAULT NULL COMMENT '微信号',
    username    VARCHAR(50)  NOT NULL COMMENT '昵称',
    avatar_url  VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    school      VARCHAR(100) DEFAULT NULL COMMENT '学校',
    college     VARCHAR(100) DEFAULT NULL COMMENT '学院',
    dorm_area   VARCHAR(50)  DEFAULT NULL COMMENT '宿舍区/校区',
    grade       VARCHAR(20)  DEFAULT NULL COMMENT '年级',
    credit_score INT         DEFAULT 100 COMMENT '信用分',
    is_verified TINYINT      DEFAULT 0 COMMENT '是否实名认证 0=否 1=是',
    role        INT          DEFAULT 0 COMMENT '角色 0=普通用户 1=管理员',
    status      INT          DEFAULT 1 COMMENT '状态 0=封禁 1=正常',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- =============================================
-- 2. 管理员表 (admins)
-- =============================================
CREATE TABLE IF NOT EXISTS admins (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    username     VARCHAR(50)  NOT NULL UNIQUE COMMENT '管理员用户名',
    phone        VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号（登录账号）',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt）',
    name         VARCHAR(50)  DEFAULT NULL COMMENT '管理员姓名',
    role         VARCHAR(20)  DEFAULT 'admin' COMMENT '角色',
    status       INT          DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员表';

-- =============================================
-- 3. 分类表 (categories)
-- =============================================
CREATE TABLE IF NOT EXISTS categories (
    id          INT          AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    code        VARCHAR(30)  NOT NULL UNIQUE COMMENT '分类编码(英文路由标识)',
    icon_class  VARCHAR(50)  DEFAULT NULL COMMENT '图标CSS类名',
    sort_order  INT          DEFAULT 0 COMMENT '排序号',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- =============================================
-- 4. 商品表 (products)
-- =============================================
CREATE TABLE IF NOT EXISTS products (
    id              BIGINT         AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    seller_id       BIGINT         NOT NULL COMMENT '卖家用户ID',
    category_id     INT            NOT NULL COMMENT '分类ID',
    title           VARCHAR(200)   NOT NULL COMMENT '商品标题',
    description     TEXT           DEFAULT NULL COMMENT '商品描述',
    price           DECIMAL(10,2)  NOT NULL COMMENT '售价',
    original_price  DECIMAL(10,2)  DEFAULT NULL COMMENT '原价',
    condition_level VARCHAR(20)    NOT NULL COMMENT '成色（全新/几乎全新/轻微使用/明显使用）',
    campus          VARCHAR(50)    DEFAULT NULL COMMENT '所在校区',
    cover_image     VARCHAR(500)   DEFAULT NULL COMMENT '封面图URL',
    view_count      INT            DEFAULT 0 COMMENT '浏览量',
    favorite_count  INT            DEFAULT 0 COMMENT '收藏数',
    status          INT            DEFAULT 0 COMMENT '状态 0=待审核 1=在售 2=已下架/已拒绝',
    audit_remark    VARCHAR(255)   DEFAULT NULL COMMENT '审核备注',
    appeal_reason   TEXT           DEFAULT NULL COMMENT '申诉理由',
    appeal_status   TINYINT        DEFAULT 0 COMMENT '申诉状态 0=未申诉 1=申诉中 2=申诉通过 3=申诉驳回',
    appeal_remark   VARCHAR(500)   DEFAULT NULL COMMENT '申诉审核备注',
    appeal_time     DATETIME       DEFAULT NULL COMMENT '申诉时间',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (seller_id)   REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_category (category_id),
    INDEX idx_status   (status),
    INDEX idx_seller   (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- =============================================
-- 5. 商品图片表 (product_images)
-- =============================================
CREATE TABLE IF NOT EXISTS product_images (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    product_id  BIGINT       NOT NULL COMMENT '商品ID',
    url         VARCHAR(500) NOT NULL COMMENT '图片URL',
    sort_order  INT          DEFAULT 0 COMMENT '排序号',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品图片表';

-- =============================================
-- 6. 购物车表 (cart_items)
-- =============================================
CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    product_id  BIGINT   NOT NULL COMMENT '商品ID',
    quantity    INT      DEFAULT 1 COMMENT '数量',
    selected    TINYINT  DEFAULT 1 COMMENT '是否选中 0=否 1=是',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车表';

-- =============================================
-- 7. 订单表 (orders)
-- =============================================
CREATE TABLE IF NOT EXISTS orders (
    id             BIGINT         AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no       VARCHAR(32)    NOT NULL UNIQUE COMMENT '订单编号',
    buyer_id       BIGINT         NOT NULL COMMENT '买家用户ID',
    seller_id      BIGINT         NOT NULL COMMENT '卖家用户ID',
    total_amount   DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    status         VARCHAR(20)    NOT NULL DEFAULT 'pending' COMMENT '订单状态 pending=待付款 paid=已付款 shipped=已发货 completed=已完成 cancelled=已取消',
    payment_time   DATETIME       DEFAULT NULL COMMENT '付款时间',
    ship_time      DATETIME       DEFAULT NULL COMMENT '发货时间',
    receive_time   DATETIME       DEFAULT NULL COMMENT '收货时间',
    complete_time  DATETIME       DEFAULT NULL COMMENT '完成时间',
    cancel_time    DATETIME       DEFAULT NULL COMMENT '取消时间',
    cancel_reason  VARCHAR(255)   DEFAULT NULL COMMENT '取消原因',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (buyer_id)  REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    INDEX idx_buyer  (buyer_id),
    INDEX idx_seller (seller_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- =============================================
-- 8. 订单明细表 (order_items)
-- =============================================
CREATE TABLE IF NOT EXISTS order_items (
    id             BIGINT         AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    order_id       BIGINT         NOT NULL COMMENT '订单ID',
    product_id     BIGINT         NOT NULL COMMENT '商品ID',
    product_title  VARCHAR(200)   NOT NULL COMMENT '商品标题快照',
    product_image  VARCHAR(500)   DEFAULT NULL COMMENT '商品图片快照',
    price          DECIMAL(10,2)  NOT NULL COMMENT '下单时单价',
    quantity       INT            NOT NULL COMMENT '购买数量',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细表';

-- =============================================
-- 9. 消息表 (messages)
-- =============================================
CREATE TABLE IF NOT EXISTS messages (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conversation_id VARCHAR(50)  DEFAULT NULL COMMENT '会话ID（格式: minId_maxId）',
    from_user_id    BIGINT       DEFAULT NULL COMMENT '发送者用户ID（系统通知时为NULL）',
    to_user_id      BIGINT       NOT NULL COMMENT '接收者用户ID',
    product_id      BIGINT       DEFAULT NULL COMMENT '关联商品ID',
    content         TEXT         NOT NULL COMMENT '消息内容',
    message_type    VARCHAR(20)  DEFAULT 'text' COMMENT '消息类型 text=文字 image=图片',
    is_read         TINYINT      DEFAULT 0 COMMENT '是否已读 0=未读 1=已读',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    FOREIGN KEY (from_user_id) REFERENCES users(id),
    FOREIGN KEY (to_user_id)   REFERENCES users(id),
    FOREIGN KEY (product_id)   REFERENCES products(id),
    INDEX idx_conversation (conversation_id),
    INDEX idx_from_user    (from_user_id),
    INDEX idx_to_user      (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息表';

-- =============================================
-- 10. 收藏表 (favorites)
-- =============================================
CREATE TABLE IF NOT EXISTS favorites (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    product_id  BIGINT   NOT NULL COMMENT '商品ID',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏表';

-- =============================================
-- 11. 收货地址表 (addresses)
-- =============================================
CREATE TABLE IF NOT EXISTS addresses (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50)  NOT NULL COMMENT '收货人姓名',
    phone         VARCHAR(20)  NOT NULL COMMENT '联系电话',
    province      VARCHAR(50)  NOT NULL COMMENT '省份',
    city          VARCHAR(50)  NOT NULL COMMENT '城市',
    district      VARCHAR(50)  NOT NULL COMMENT '区/县',
    detail        VARCHAR(200) NOT NULL COMMENT '详细地址',
    is_default    TINYINT      DEFAULT 0 COMMENT '是否默认地址 0=否 1=是',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收货地址表';

-- =============================================
-- 12. 公告表 (announcements)
-- =============================================
CREATE TABLE IF NOT EXISTS announcements (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    admin_id    BIGINT       NOT NULL COMMENT '发布管理员ID',
    title       VARCHAR(200) NOT NULL COMMENT '公告标题',
    content     TEXT         NOT NULL COMMENT '公告内容',
    priority    INT          DEFAULT 0 COMMENT '优先级 0=普通 1=置顶',
    status      INT          DEFAULT 1 COMMENT '状态 0=下架 1=发布',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (admin_id) REFERENCES admins(id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- =============================================
-- 13. 轮播图表 (banners)
-- =============================================
CREATE TABLE IF NOT EXISTS banners (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '轮播图ID',
    image_url   VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url    VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
    sort_order  INT          DEFAULT 0 COMMENT '排序号',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0=下架 1=上架',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status     (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='轮播图表';

-- =============================================
-- 14. 系统日志表 (system_logs)
-- =============================================
CREATE TABLE IF NOT EXISTS system_logs (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    action      VARCHAR(100) NOT NULL COMMENT '操作类型',
    description VARCHAR(500) NOT NULL COMMENT '操作描述',
    operator    VARCHAR(50)  NOT NULL COMMENT '操作人',
    ip          VARCHAR(50)  DEFAULT NULL COMMENT '操作IP',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_action (action),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统日志表';

-- =============================================
-- 测试数据 (Test Data)
-- 密码均为 BCrypt 加密，明文 123456
-- =============================================

-- 管理员（admins 表，1个，密码: 123456）
INSERT INTO admins (id, username, phone, password_hash, name, role, status) VALUES
    (1, '13900000000', '13900000000', '$2a$10$TUG.xymA/HllSxVw.5YtLuzSYv42OgeCSqM4.Xuy/4heYHwOBySZG', '管理员', 'super_admin', 1)
ON DUPLICATE KEY UPDATE username=VALUES(username), phone=VALUES(phone);

-- 注意：users 表不再存储管理员账号，管理员仅存在于 admins 表。
-- users.role 保留字段定义，但不再有 role=1 的管理员记录。

-- 普通用户（4个，id=1,2,3,4）
INSERT INTO users (phone, password_hash, username, school, college, dorm_area, role, status) VALUES
    ('13800000001', '$2b$12$JgHiPoqlm6KSDDZjgZV1e..oXFsmo4zJ0oQ4Ut0z7EZ6Lo6CB1g42', '中南学长', '中南大学', '材料科学与工程', '铁道校区', 0, 1),
    ('13800000002', '$2b$12$JgHiPoqlm6KSDDZjgZV1e..oXFsmo4zJ0oQ4Ut0z7EZ6Lo6CB1g42', '湖大艺院', '湖南大学', '艺术学院',       '天马公寓', 0, 1),
    ('13800000003', '$2b$12$JgHiPoqlm6KSDDZjgZV1e..oXFsmo4zJ0oQ4Ut0z7EZ6Lo6CB1g42', '师大外院', '湖南师范大学', '外语学院',   '南院校区', 0, 1),
    ('13800000004', '$2b$12$JgHiPoqlm6KSDDZjgZV1e..oXFsmo4zJ0oQ4Ut0z7EZ6Lo6CB1g42', '理工小哥', '长沙理工', '计算机学院',     '云塘校区', 0, 1)
ON DUPLICATE KEY UPDATE phone=phone;

-- 商品分类（8个）
INSERT INTO categories (name, code, icon_class, sort_order) VALUES
    ('教材书籍', 'textbook',  'icon-book',   1),
    ('数码电子', 'digital',   'icon-laptop', 2),
    ('服饰鞋包', 'clothing',  'icon-clothes',3),
    ('生活日用', 'daily',     'icon-life',   4),
    ('运动户外', 'sports',    'icon-sport',  5),
    ('乐器文具', 'stationery','icon-music',  6),
    ('美妆护肤', 'beauty',    'icon-beauty', 7),
    ('其他好物', 'other',     'icon-other',  8)
ON DUPLICATE KEY UPDATE name=name;

-- =============================================
-- 测试商品（16个，均分给4个普通用户，每人4个，status=1 在售）
-- 用户1（原id=1）: 教材书籍(2) + 服饰鞋包(1) + 生活日用(1) = 4
-- 用户2（原id=2）: 教材书籍(0) + 服饰鞋包(1) + 乐器文具(1) + 美妆护肤(1) + 其他(1) = 4
-- 用户3（原id=3）: 数码电子(1) + 运动户外(1) + 乐器文具(1) + 其他(1) = 4
-- 用户4（原id=4）: 数码电子(1) + 生活日用(1) + 运动户外(1) + 美妆护肤(1) = 4
-- =============================================
INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, status) VALUES
-- 用户1（中南学长）：教材书籍×2 + 服饰鞋包×1 + 生活日用×1
    (1,1,'高等数学第七版上册','几乎全新，只用过前两章，无笔记',15.00,39.00,'几乎全新','铁道校区',1),
    (1,1,'土木工程材料 教材','大三专业课教材，有少量笔记，整体保存完好',25.00,48.00,'几乎全新','铁道校区',1),
    (1,3,'ZARA秋冬中长款毛呢大衣','去年买的L码，穿了两三次，风格不合适',199.00,599.00,'轻微使用','天马公寓',1),
    (1,4,'宿舍小型加湿器','用了两个月，静音款不打扰睡眠，换大的所以出',29.00,69.00,'轻微使用','铁道校区',1),
-- 用户2（湖大艺院）：服饰鞋包×1 + 运动户外×1 + 乐器文具×1 + 美妆护肤×1
    (2,3,'韩系简约帆布斜挎包','背了两三次，奶白色很百搭',35.00,89.00,'轻微使用','南院校区',1),
    (2,5,'宿舍瑜伽垫加厚款','只用了几次，10mm厚防滑款，送收纳绑带',30.00,65.00,'几乎全新','南院校区',1),
    (2,6,'雅马哈F310民谣吉他','买了半年没怎么弹，送调音器和谱架',499.00,899.00,'几乎全新','南院校区',1),
    (2,7,'完美日记眼影盘 赤狐','仅试色两次，配色不适合我',35.00,89.00,'几乎全新','南院校区',1),
-- 用户3（师大外院）：数码电子×1 + 运动户外×1 + 乐器文具×1 + 其他好物×1
    (3,2,'八成新 MacBook Air M1','2021款 8+256G，轻微使用痕迹，带原装充电器',4500.00,7999.00,'轻微使用','天马公寓',1),
    (3,5,'尤尼克斯羽毛球拍','正品，轻微磕碰不影响使用',120.00,380.00,'轻微使用','南院校区',1),
    (3,6,'樱花针管笔套装24色','全新未拆封，买多了一套',28.00,55.00,'几乎全新','南院校区',1),
    (3,8,'宿舍用小台灯','LED台灯三档调光，USB充电，可夹床头',25.00,59.00,'明显使用','南院校区',1),
-- 用户4（理工小哥）：数码电子×1 + 生活日用×1 + 美妆护肤×1 + 其他好物×1
    (4,2,'罗技K380蓝牙无线键盘 白色','买了机械键盘所以出，所有按键正常',85.00,199.00,'轻微使用','云塘校区',1),
    (4,4,'奥克斯2L电煮锅','用了半学期，煮面火锅都行，宿舍电压可用',39.00,99.00,'明显使用','云塘校区',1),
    (4,7,'悦诗风吟散粉 5g','免税店购入，只用过一次，色号不合适',22.00,50.00,'几乎全新','云塘校区',1),
    (4,8,'冬季加厚法兰绒床单三件套','只用了一冬天洗干净了，1.5米床适用',55.00,129.00,'明显使用','云塘校区',1);

-- =============================================
-- 商品图片（每商品1~2张）
-- =============================================
INSERT INTO product_images (product_id, url, sort_order) VALUES
    (1,'/images/math_book_1.jpg',1),
    (2,'/images/material_book_1.jpg',1),
    (3,'/images/coat_1.jpg',1),
    (4,'/images/humidifier_1.jpg',1),
    (5,'/images/bag_1.jpg',1),
    (6,'/images/yoga_mat_1.jpg',1),
    (7,'/images/guitar_1.jpg',1),
    (8,'/images/eyeshadow_1.jpg',1),
    (9,'/images/macbook_1.jpg',1),
    (10,'/images/badminton_1.jpg',1),
    (11,'/images/pens_1.jpg',1),
    (12,'/images/lamp_1.jpg',1),
    (13,'/images/keyboard_1.jpg',1),
    (14,'/images/cooker_1.jpg',1),
    (15,'/images/powder_1.jpg',1),
    (16,'/images/bedding_1.jpg',1);

-- =============================================
-- 消息对话（补全 conversation_id，订单间有完整对话）
-- conversation_id 规则: min(fromUserId, toUserId)_max(fromUserId, toUserId)
-- 会话1: 中南学长(1) <-> 湖大艺院(2)  conversation_id = "1_2"  - 湖大艺院咨询ZARA大衣 → 中南学长回复 → 湖大艺院下单确认
-- 会话2: 师大外院(3) <-> 中南学长(1)  conversation_id = "1_3"  - 师大外院咨询土木教材 → 中南学长回复 → 师大外院下单 → 中南学长发货通知
-- 会话3: 理工小哥(4) <-> 师大外院(3)  conversation_id = "3_4"  - 理工小哥咨询MacBook → 师大外院回复 → 理工小哥还在考虑
-- =============================================
-- 会话 "1_2": 湖大艺院(2)找中南学长(1)买ZARA大衣 (product_id=3)
INSERT INTO messages (conversation_id, from_user_id, to_user_id, product_id, content, message_type, is_read, created_at) VALUES
('1_2', 2, 1, 3, '你好，这件ZARA大衣还在吗？最低多少出？','text', 1, '2026-06-18 10:30:00'),
('1_2', 1, 2, 3, '在的，衣服保存得很好只穿过两三次。199已经最低了哦，原价599呢','text', 1, '2026-06-18 10:35:00'),
('1_2', 2, 1, 3, '能再便宜点吗？我诚心要，170行不行？','text', 1, '2026-06-18 10:40:00'),
('1_2', 1, 2, 3, '这样吧，180你拿走，再低我真不舍得出了','text', 1, '2026-06-18 10:42:00'),
('1_2', 2, 1, 3, '好的，180成交！我直接下单了','text', 1, '2026-06-18 10:45:00'),
('1_2', 1, 2, 3, 'OK，看到订单了，我明天给你发货','text', 0, '2026-06-18 10:48:00');
-- 会话 "1_3": 师大外院(3)找中南学长(1)买土木教材 (product_id=2)
INSERT INTO messages (conversation_id, from_user_id, to_user_id, product_id, content, message_type, is_read, created_at) VALUES
('1_3', 3, 1, 2, '你好，土木工程材料教材还有吗？我想要','text', 1, '2026-06-20 14:00:00'),
('1_3', 1, 3, 2, '还有的！书保存得很好，笔记也不多，25块很划算','text', 1, '2026-06-20 14:15:00'),
('1_3', 3, 1, 2, '好的我下单了，什么时候能拿到？','text', 1, '2026-06-20 14:20:00'),
('1_3', 1, 3, 2, '已发货，明天应该就能到，我在铁道校区，你在哪个校区？','text', 0, '2026-06-20 14:25:00');
-- 会话 "3_4": 理工小哥(4)找师大外院(3)买MacBook (product_id=9)
INSERT INTO messages (conversation_id, from_user_id, to_user_id, product_id, content, message_type, is_read, created_at) VALUES
('3_4', 4, 3, 9, 'MacBook电池循环多少次？能小刀吗？','text', 1, '2026-06-22 09:00:00'),
('3_4', 3, 4, 9, '电池循环大概120次，健康度还有92%。4500已经很便宜了，毕竟M1芯片8+256','text', 1, '2026-06-22 09:30:00'),
('3_4', 4, 3, 9, '能不能4300？我是学生预算有限','text', 0, '2026-06-22 10:00:00');

-- =============================================
-- 系统通知（from_user_id=NULL 表示系统发送）
-- conversation_id 格式: 'system_{userId}'
-- =============================================
INSERT INTO messages (conversation_id, from_user_id, to_user_id, product_id, content, message_type, is_read, created_at) VALUES
('system_2', NULL, 2, NULL, '欢迎使用校园二手交易平台！请完善个人信息，开始你的校园交易之旅吧~', 'text', 0, '2026-07-01 08:00:00'),
('system_2', NULL, 2, NULL, '系统维护通知：平台将于今晚22:00-24:00进行升级维护，期间暂停服务，请提前安排好交易。', 'text', 1, '2026-07-03 18:00:00'),
('system_3', NULL, 3, NULL, '欢迎使用校园二手交易平台！你发布的商品已通过审核，快去看看吧。', 'text', 0, '2026-07-02 09:30:00'),
('system_3', NULL, 3, NULL, '安全提醒：请勿在平台外进行私下交易，谨防诈骗。如遇可疑行为请及时举报。', 'text', 1, '2026-07-05 10:00:00'),
('system_4', NULL, 4, NULL, '欢迎使用校园二手交易平台！诚信交易，共建和谐校园二手市场。', 'text', 0, '2026-07-02 14:00:00'),
('system_4', NULL, 4, NULL, '系统通知：你的账号已通过实名认证，现在可以发布商品啦！', 'text', 1, '2026-07-04 16:30:00'),
('system_1', NULL, 1, NULL, '恭喜！你已成为平台活跃卖家，信用分+10。继续保持优质交易哦~', 'text', 0, '2026-07-06 12:00:00');

-- =============================================
-- 订单（3个，分配给3个不同普通用户）
-- ORD20260627001: 湖大艺院(2)买中南学长(1)的ZARA大衣 → paid  ORD20260627002: 师大外院(3)买中南学长(1)的土木教材 → shipped  ORD20260627003: 理工小哥(4)买师大外院(3)的MacBook → shipped
-- =============================================
INSERT INTO orders (order_no, buyer_id, seller_id, total_amount, status, payment_time, ship_time, created_at) VALUES
('ORD20260627001', 2, 1, 180.00,  'paid',      '2026-06-18 10:50:00', NULL,                 '2026-06-18 10:45:00'),
('ORD20260627002', 3, 1, 25.00,   'shipped',   '2026-06-20 14:22:00', '2026-06-20 14:25:00', '2026-06-20 14:20:00'),
('ORD20260627003', 4, 3, 4500.00, 'shipped',   '2026-07-07 12:00:00', '2026-07-07 12:00:00', '2026-06-22 10:05:00')
ON DUPLICATE KEY UPDATE
    buyer_id=VALUES(buyer_id), seller_id=VALUES(seller_id), total_amount=VALUES(total_amount),
    status=VALUES(status), payment_time=VALUES(payment_time), ship_time=VALUES(ship_time),
    created_at=VALUES(created_at);

-- =============================================
-- 订单明细（对应3个订单）
-- ORD20260627001: ZARA大衣(product_id=3), 180元   ORD20260627002: 土木工程材料教材(product_id=2), 25元  ORD20260627003: MacBook(product_id=9), 4500元
-- 先删后插保证幂等（order_items 表无 order_id+product_id 唯一约束，直接用 DELETE+INSERT 安全重导）
-- =============================================
DELETE FROM order_items WHERE order_id IN (1, 2, 3) AND product_id IN (2, 3, 9);
INSERT INTO order_items (order_id, product_id, product_title, product_image, price, quantity) VALUES
(1, 3, 'ZARA秋冬中长款毛呢大衣', '/images/demo/coat_1.jpg', 180.00, 1),
(2, 2, '土木工程材料 教材', '/images/demo/material_book_1.jpg', 25.00, 1),
(3, 9, '八成新 MacBook Air M1', '/images/demo/macbook_1.jpg', 4500.00, 1);

-- =============================================
-- 公告（4条测试公告）
-- =============================================
INSERT INTO announcements (admin_id, title, content, priority, status, created_at) VALUES
(1, '平台上线通知', '欢迎使用CampusDeal校园二手交易平台！请同学们文明交易，遵守平台规则。如有问题请联系管理员。', 1, 1, '2026-06-15 00:00:00'),
(1, '关于商品发布的注意事项', '发布商品时请上传清晰实拍图片，如实描述商品成色。虚假描述将被下架处理，严重者封禁账号。', 0, 1, '2026-06-20 00:00:00'),
(1, '欢迎来到校园二手交易平台', '欢迎新同学！请仔细阅读平台规则，诚信交易，共建和谐校园。如有问题请联系管理员。', 0, 1, '2026-07-01 00:00:00'),
(1, '平台交易安全提醒', '请勿在平台外进行交易，谨防诈骗。交易时请仔细核对商品信息，确认无误后再下单。', 0, 1, '2026-07-01 00:00:00');

-- =============================================
-- 轮播图种子数据（3条）
-- =============================================
INSERT INTO banners (image_url, link_url, sort_order, status) VALUES
    ('https://picsum.photos/seed/banner1/1200/400', '/products', 1, 1),
    ('https://picsum.photos/seed/banner2/1200/400', '/publish', 2, 1),
    ('https://picsum.photos/seed/banner3/1200/400', '/products?sort=hot', 3, 1)
ON DUPLICATE KEY UPDATE image_url=VALUES(image_url), link_url=VALUES(link_url);

-- =============================================
-- 系统日志（10条测试数据）
-- =============================================
INSERT INTO system_logs (action, description, operator, ip, created_at) VALUES
('用户登录', '管理员 13900000000 登录成功', '管理员', '127.0.0.1', '2026-06-25 09:15:00'),
('商品审核', '审核通过商品"高等数学第七版上册"(ID:1)', '管理员', '127.0.0.1', '2026-06-25 09:30:00'),
('商品审核', '审核通过商品"土木工程材料 教材"(ID:2)', '管理员', '127.0.0.1', '2026-06-25 09:31:00'),
('商品下架', '下架商品"八成新 MacBook Air M1"(ID:9)', '管理员', '127.0.0.1', '2026-06-25 10:00:00'),
('商品上架', '重新上架商品"八成新 MacBook Air M1"(ID:9)', '管理员', '127.0.0.1', '2026-06-25 10:05:00'),
('用户管理', '封禁用户"中南学长"(ID:1)', '管理员', '127.0.0.1', '2026-06-26 14:00:00'),
('用户管理', '解封用户"中南学长"(ID:1)', '管理员', '127.0.0.1', '2026-06-26 14:30:00'),
('用户登录', '用户 13800000002 登录成功', '湖大艺院', '192.168.1.100', '2026-06-27 08:20:00'),
('用户登录', '用户 13800000003 登录成功', '师大外院', '192.168.1.101', '2026-06-27 09:10:00'),
('商品审核', '驳回商品"测试商品示例"(ID:20)，原因：图片不清晰', '管理员', '127.0.0.1', '2026-06-28 11:00:00');

-- =============================================
-- 收货地址（4条测试数据）
-- =============================================
INSERT INTO addresses (id, user_id, receiver_name, phone, province, city, district, detail, is_default, created_at, updated_at) VALUES
-- 对应用户: 中南学长 (中南大学 铁道校区)
(1, 1, '中南学长', '13800000001', '湖南省', '长沙市', '岳麓区', '中南大学铁道校区', 1, NOW(), NOW()),
-- 对应用户: 湖大艺院 (湖南大学 天马公寓)
(2, 2, '湖大艺院', '13800000002', '湖南省', '长沙市', '岳麓区', '湖南大学天马公寓', 1, NOW(), NOW()),
-- 对应用户: 师大外院 (湖南师范大学 南院校区)
(3, 3, '师大外院', '13800000003', '湖南省', '长沙市', '岳麓区', '湖南师范大学南院校区', 1, NOW(), NOW()),
-- 对应用户: 理工小哥 (长沙理工 云塘校区)
(4, 4, '理工小哥', '13800000004', '湖南省', '长沙市', '天心区', '长沙理工大学云塘校区', 1, NOW(), NOW());