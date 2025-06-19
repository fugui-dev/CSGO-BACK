/*
 Navicat Premium Data Transfer

 Source Server         : 网域未来
 Source Server Type    : MySQL
 Source Server Version : 80024
 Source Host           : 152.136.63.254:3306
 Source Schema         : db_fire

 Target Server Type    : MySQL
 Target Server Version : 80024
 File Encoding         : 65001

 Date: 16/06/2024 12:54:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tt_upgrade_fail_ornaments
-- ----------------------------
DROP TABLE IF EXISTS `tt_upgrade_fail_ornaments`;
CREATE TABLE `tt_upgrade_fail_ornaments`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `upgrade_id` int(0) NULL DEFAULT NULL COMMENT 'tt_upgrade_ornaments表id',
  `ornament_id` int(0) NULL DEFAULT NULL COMMENT '失败奖励饰品id',
  `ornament_level_id` int(0) NULL DEFAULT 1,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `ornament_price` decimal(10, 2) NULL DEFAULT NULL,
  `ornament_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `ornament_number` int(0) NULL DEFAULT NULL COMMENT '数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tt_upgrade_fail_ornaments
-- ----------------------------
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (9, 11, 2073872884, 1, '2024-04-08 18:20:16', '2024-04-08 18:20:16', 1.78, '2020 RMR 竞争组战队胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (12, 15, 1600898503, 1, '2024-04-14 11:31:06', '2024-04-14 11:31:06', 1.29, '2020 RMR 传奇组战队胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (13, 19, 1600898503, 1, '2024-04-14 11:31:23', '2024-04-14 11:31:23', 1.29, '2020 RMR 传奇组战队胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (14, 18, 1600898503, 1, '2024-04-14 11:31:41', '2024-04-14 11:31:41', 1.29, '2020 RMR 传奇组战队胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (15, 20, 1874438817, 1, '2024-04-14 11:32:13', '2024-04-14 11:32:13', 0.05, 'AUG | 扫频仪 (久经沙场)', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (16, 17, 1600898503, 1, '2024-04-14 11:32:28', '2024-04-14 11:32:28', 1.29, '2020 RMR 传奇组战队胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (17, 16, 2100491617, 1, '2024-04-14 11:32:48', '2024-04-14 11:32:48', 0.05, 'MP5-SD | 越野 (久经沙场)', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (18, 14, 1470154038, 1, '2024-04-14 11:33:20', '2024-04-14 11:33:20', 0.10, '封装的涂鸦 | 背刺 (豆青)', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (19, 13, 1600898503, 1, '2024-04-14 11:33:34', '2024-04-14 11:33:34', 1.29, '2020 RMR 传奇组战队胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (20, 12, 1874438817, 1, '2024-04-14 11:33:55', '2024-04-14 11:33:55', 0.05, 'AUG | 扫频仪 (久经沙场)', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (21, 10, 1655024877, 1, '2024-04-14 11:35:15', '2024-04-14 11:35:15', 5.15, 'CS:GO 10周年印花胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (22, 22, 736260141, 1, '2024-04-15 15:10:32', '2024-04-15 15:10:32', 1.33, '2020 RMR 挑战组战队胶囊', 1);
INSERT INTO `tt_upgrade_fail_ornaments` VALUES (23, 21, 1600898503, 1, '2024-04-15 15:10:43', '2024-04-15 15:10:43', 1.29, '2020 RMR 传奇组战队胶囊', 1);

SET FOREIGN_KEY_CHECKS = 1;
