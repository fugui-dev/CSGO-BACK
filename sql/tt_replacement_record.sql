/*
 Navicat Premium Data Transfer

 Source Server         : 1.92.85.51_3306_fire
 Source Server Type    : MySQL
 Source Server Version : 50740
 Source Host           : 1.92.85.51:3306
 Source Schema         : fire

 Target Server Type    : MySQL
 Target Server Version : 50740
 File Encoding         : 65001

 Date: 15/06/2024 10:51:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tt_replacement_record
-- ----------------------------
DROP TABLE IF EXISTS `tt_replacement_record`;
CREATE TABLE `tt_replacement_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uid` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `uname` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名称',
  `oids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '饰品ids',
  `award_oid` int(11) NULL DEFAULT NULL COMMENT '合成饰品的id',
  `award_oname` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合成饰品的名称',
  `award_oprice` decimal(10, 2) NULL DEFAULT NULL COMMENT '合成饰品的价格',
  `time` datetime(6) NULL DEFAULT NULL COMMENT '合成日期',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `award_oimg` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '饰品图片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 376 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
