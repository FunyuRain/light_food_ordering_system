-- ============================================================================
-- 轻食点单平台（Light Food）阶段 1 数据库初始化脚本
-- 目标数据库：MySQL 8.0.16+
-- 说明：
--   1. 本脚本会删除并重建 light_food 库中的业务表，仅用于开发/测试环境；
--   2. 字符集统一为 utf8mb4，金额统一使用 DECIMAL，时间统一使用 DATETIME；
--   3. 订单包含模拟支付和状态流转，但不包含真实支付渠道、退款、配送费、优惠等字段；
--   4. 文末附带最小演示数据，默认口令仅用于本地联调，上线前必须修改。
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `light_food`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE `light_food`;

-- 按“子表 -> 父表”的顺序清理，便于重复执行
DROP TABLE IF EXISTS `order_detail`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `shopping_cart`;
DROP TABLE IF EXISTS `user_address`;
DROP TABLE IF EXISTS `dish`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `admin`;

-- ----------------------------------------------------------------------------
-- 1. 管理员表
-- ----------------------------------------------------------------------------
CREATE TABLE `admin` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`        VARCHAR(50)  NOT NULL                COMMENT '登录用户名',
  `password_hash`   VARCHAR(100) NOT NULL                COMMENT '登录密码哈希（BCrypt）',
  `real_name`       VARCHAR(50)  NOT NULL                COMMENT '管理员姓名',
  `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1启用，0禁用',
  `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '管理员表';

-- ----------------------------------------------------------------------------
-- 2. 用户表
--    本期支持“手机号/账号 + 密码”登录，不引入微信 openid；
--    不开放注册，联调账号见文末演示数据。
-- ----------------------------------------------------------------------------
CREATE TABLE `user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`        VARCHAR(50)  NOT NULL                COMMENT '登录账号',
  `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号（可用于登录或联系）',
  `password_hash`   VARCHAR(100) NOT NULL                COMMENT '登录密码哈希（BCrypt）',
  `nickname`        VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '用户昵称',
  `avatar_url`      VARCHAR(500) DEFAULT NULL            COMMENT '头像地址',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1正常，0禁用',
  `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户表';

-- ----------------------------------------------------------------------------
-- 3. 菜品分类表
--    本期只做分类增删改查，不做分类启用/禁用；
--    分类下存在菜品时由外键阻止删除。
-- ----------------------------------------------------------------------------
CREATE TABLE `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '分类名称',
  `description` VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '分类描述',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '菜品分类表';

-- ----------------------------------------------------------------------------
-- 4. 菜品表
--    管理端支持菜品新增、编辑、删除和上下架；
--    图片只保存模拟路径，不接图床、不提供上传接口。
-- ----------------------------------------------------------------------------
CREATE TABLE `dish` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` BIGINT        NOT NULL                COMMENT '所属分类ID',
  `name`        VARCHAR(100)  NOT NULL                COMMENT '菜品名称',
  `description` VARCHAR(500)  NOT NULL DEFAULT ''     COMMENT '菜品描述',
  `image_url`   VARCHAR(500)  NOT NULL DEFAULT '/mock/dish/default.svg' COMMENT '菜品图片模拟路径（不接图床）',
  `price`       DECIMAL(10,2) NOT NULL                COMMENT '当前售价（元）',
  `sort`        INT           NOT NULL DEFAULT 0      COMMENT '分类内排序值，越小越靠前',
  `status`      TINYINT       NOT NULL DEFAULT 0      COMMENT '售卖状态：1上架，0下架',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dish_category_status` (`category_id`, `status`),
  KEY `idx_dish_status` (`status`),
  CONSTRAINT `fk_dish_category`
    FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_dish_price` CHECK (`price` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '菜品表';

-- ----------------------------------------------------------------------------
-- 5. 用户收货地址表
--    is_default 的“每个用户最多一个默认地址”由业务层事务保证；
--    不使用 (user_id, is_default) 唯一键，否则会限制一个用户只能有两条地址。
-- ----------------------------------------------------------------------------
CREATE TABLE `user_address` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`        BIGINT       NOT NULL                COMMENT '所属用户ID',
  `receiver_name`  VARCHAR(50)  NOT NULL                COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20)  NOT NULL                COMMENT '收货人手机号',
  `province`       VARCHAR(50)  NOT NULL                COMMENT '省',
  `city`           VARCHAR(50)  NOT NULL                COMMENT '市',
  `district`       VARCHAR(50)  NOT NULL                COMMENT '区/县',
  `detail_address` VARCHAR(255) NOT NULL                COMMENT '详细地址（街道、门牌号等）',
  `label`          VARCHAR(20)  DEFAULT NULL            COMMENT '地址标签，如：家、公司',
  `is_default`     TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认地址：1是，0否',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_address_user_default` (`user_id`, `is_default`),
  CONSTRAINT `fk_user_address_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_user_address_default` CHECK (`is_default` IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户收货地址表';

-- ----------------------------------------------------------------------------
-- 6. 购物车表
--    同一用户同一菜品只保留一行，重复加购时累加 quantity；
--    购物车只存菜品引用和数量，不锁价，价格以菜品当前 price 为准。
-- ----------------------------------------------------------------------------
CREATE TABLE `shopping_cart` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
  `dish_id`     BIGINT   NOT NULL                COMMENT '菜品ID',
  `quantity`    INT      NOT NULL DEFAULT 1      COMMENT '购买数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_user_dish` (`user_id`, `dish_id`),
  CONSTRAINT `fk_cart_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_dish`
    FOREIGN KEY (`dish_id`) REFERENCES `dish` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_cart_quantity` CHECK (`quantity` > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '购物车表';

-- ----------------------------------------------------------------------------
-- 7. 订单主表
--    订单创建时保存收货信息快照；address_id 仅用于追溯来源地址，
--    地址删除后置 NULL，不影响历史订单展示。
--    状态流转：
--      0待付款 --用户模拟支付--> 1待接单 --管理员接单--> 2已接单 --用户确认完成--> 3已完成
--      0待付款 --用户取消--> 4已取消
--      1待接单/2已接单 --管理员取消--> 4已取消
--    支付仅为模拟，不对接真实支付渠道；已支付订单取消不涉及真实退款。
-- ----------------------------------------------------------------------------
CREATE TABLE `orders` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no`         VARCHAR(32)   NOT NULL                COMMENT '业务订单号',
  `user_id`          BIGINT        NOT NULL                COMMENT '下单用户ID',
  `address_id`       BIGINT        DEFAULT NULL            COMMENT '下单时选择的地址ID，地址删除后置NULL',
  `status`           TINYINT       NOT NULL DEFAULT 0      COMMENT '订单状态：0待付款，1待接单，2已接单，3已完成，4已取消',
  `total_amount`     DECIMAL(10,2) NOT NULL                COMMENT '订单总金额（元，等于明细金额之和）',
  `pay_method`       TINYINT       DEFAULT NULL            COMMENT '支付方式：1模拟支付；未支付为NULL',
  `pay_time`         DATETIME      DEFAULT NULL            COMMENT '支付时间；未支付为NULL',
  `accept_time`      DATETIME      DEFAULT NULL            COMMENT '管理员接单时间',
  `finish_time`      DATETIME      DEFAULT NULL            COMMENT '订单完成时间',
  `cancel_time`      DATETIME      DEFAULT NULL            COMMENT '订单取消时间',
  `cancel_by`        TINYINT       DEFAULT NULL            COMMENT '取消方：1用户，2管理员；未取消为NULL',
  `cancel_reason`    VARCHAR(200)  DEFAULT NULL            COMMENT '取消原因',
  `receiver_name`    VARCHAR(50)   NOT NULL                COMMENT '收货人姓名快照',
  `receiver_phone`   VARCHAR(20)   NOT NULL                COMMENT '收货人手机号快照',
  `delivery_address` VARCHAR(500)  NOT NULL                COMMENT '完整收货地址快照（省市区+详细地址）',
  `remark`           VARCHAR(200)  DEFAULT NULL            COMMENT '订单备注',
  `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  KEY `idx_orders_user_time` (`user_id`, `create_time`),
  KEY `idx_orders_status_time` (`status`, `create_time`),
  CONSTRAINT `fk_orders_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_address`
    FOREIGN KEY (`address_id`) REFERENCES `user_address` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_orders_total_amount` CHECK (`total_amount` >= 0),
  CONSTRAINT `chk_orders_status` CHECK (`status` BETWEEN 0 AND 4),
  CONSTRAINT `chk_orders_cancel_by` CHECK (`cancel_by` IS NULL OR `cancel_by` IN (1, 2))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单主表';

-- ----------------------------------------------------------------------------
-- 8. 订单明细表
--    必须单独建表，不能把多个菜品塞进订单主表；
--    菜品名称、图片、单价均保存下单时快照，菜品后续改价或删除不影响历史订单。
-- ----------------------------------------------------------------------------
CREATE TABLE `order_detail` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id`    BIGINT        NOT NULL                COMMENT '所属订单ID',
  `dish_id`     BIGINT        DEFAULT NULL            COMMENT '菜品ID，菜品删除后置NULL',
  `dish_name`   VARCHAR(100)  NOT NULL                COMMENT '菜品名称快照',
  `dish_image`  VARCHAR(500)  DEFAULT NULL            COMMENT '菜品图片快照',
  `unit_price`  DECIMAL(10,2) NOT NULL                COMMENT '下单时单价（元）',
  `quantity`    INT           NOT NULL                COMMENT '购买数量',
  `amount`      DECIMAL(10,2) NOT NULL                COMMENT '明细金额（unit_price × quantity，元）',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_detail_order` (`order_id`),
  KEY `idx_order_detail_dish` (`dish_id`),
  CONSTRAINT `fk_order_detail_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_detail_dish`
    FOREIGN KEY (`dish_id`) REFERENCES `dish` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_order_detail_unit_price` CHECK (`unit_price` >= 0),
  CONSTRAINT `chk_order_detail_quantity` CHECK (`quantity` > 0),
  CONSTRAINT `chk_order_detail_amount` CHECK (`amount` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单明细表';

-- ============================================================================
-- 最小演示数据
-- 演示账号：
--   管理员：admin / admin123
--   用户：  testuser / admin123
-- 口令均为 BCrypt 哈希，仅用于本地开发，部署前必须修改。
-- ============================================================================
START TRANSACTION;

INSERT INTO `admin`
  (`id`, `username`, `password_hash`, `real_name`, `phone`, `status`)
VALUES
  (1, 'admin',
   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
   '系统管理员', '13900000000', 1);

INSERT INTO `user`
  (`id`, `username`, `phone`, `password_hash`, `nickname`, `avatar_url`, `status`)
VALUES
  (1, 'testuser', '13800000000',
   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
   '测试用户', NULL, 1);

INSERT INTO `category`
  (`id`, `name`, `description`, `sort`)
VALUES
  (1, '轻食沙拉', '以蔬菜、鸡胸肉、牛油果等为主的低负担沙拉', 10),
  (2, '轻食主食', '糙米、藜麦、波奇饭等饱腹主食', 20),
  (3, '鲜榨饮品', '果汁、咖啡等佐餐饮品', 30);

INSERT INTO `dish`
  (`id`, `category_id`, `name`, `description`, `image_url`, `price`, `sort`, `status`)
VALUES
  (1, 1, '鸡胸肉藜麦沙拉', '低温慢煮鸡胸肉、藜麦、生菜、小番茄，搭配油醋汁', '/mock/dish/default.svg', 32.00, 10, 1),
  (2, 1, '牛油果虾仁沙拉', '牛油果、低脂虾仁、混合生菜，搭配柠檬酸奶酱', '/mock/dish/default.svg', 38.00, 20, 1),
  (3, 2, '香煎鸡胸糙米饭', '香煎鸡胸肉、糙米饭、时蔬，高蛋白轻负担', '/mock/dish/default.svg', 35.00, 10, 1),
  (4, 2, '三文鱼牛油果波奇饭', '三文鱼、牛油果、海苔、糙米饭，搭配低脂酱汁', '/mock/dish/default.svg', 45.00, 20, 1),
  (5, 3, '羽衣甘蓝苹果汁', '羽衣甘蓝、苹果、柠檬鲜榨，不额外加糖', '/mock/dish/default.svg', 22.00, 10, 1),
  (6, 3, '无糖冷萃美式', '深烘咖啡豆冷萃，无糖无奶', '/mock/dish/default.svg', 18.00, 20, 1);

INSERT INTO `user_address`
  (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `label`, `is_default`)
VALUES
  (1, 1, '测试用户', '13800000000', '上海市', '上海市', '徐汇区', '测试路 1 号 101 室', '家', 1);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 脚本结束
-- ============================================================================
