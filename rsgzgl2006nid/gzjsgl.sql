/*
 Navicat Premium Dump SQL

 Source Server         : MySQL8
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : gzjsgl

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 06/05/2026 15:14:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for aa
-- ----------------------------
DROP TABLE IF EXISTS `aa`;
CREATE TABLE `aa`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grbm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base
-- ----------------------------
DROP TABLE IF EXISTS `base`;
CREATE TABLE `base`  (
  `lbbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(34) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gzbzbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bdry
-- ----------------------------
DROP TABLE IF EXISTS `bdry`;
CREATE TABLE `bdry`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `csny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ryfl` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gwfl` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jrny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jrfs` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zdgznx` decimal(2, 0) NULL DEFAULT NULL,
  `gznx` decimal(2, 0) NULL DEFAULT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zdjhlnx` decimal(2, 0) NULL DEFAULT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bjglxlnx` decimal(2, 0) NULL DEFAULT NULL,
  `jx1` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `txsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xckhndzw` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xckhndjb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bgdwjc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xrzw` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgbl` decimal(2, 0) NULL DEFAULT NULL,
  `tgbl1` decimal(2, 0) NULL DEFAULT NULL,
  `jtbl` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jtbl1` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fddc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fddc1` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `khqk` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dynkh` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `denkh` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bbz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `hj1` decimal(4, 0) NULL DEFAULT NULL,
  `zwbm1` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw1` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzdc1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzse1` decimal(4, 0) NULL DEFAULT NULL,
  `jbgzjb1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `djc1` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbgzse1` decimal(4, 0) NULL DEFAULT NULL,
  `jcgz1` decimal(3, 0) NULL DEFAULT NULL,
  `glgz1` decimal(3, 0) NULL DEFAULT NULL,
  `jsdjgz1` decimal(3, 0) NULL DEFAULT NULL,
  `grjj1` decimal(3, 0) NULL DEFAULT NULL,
  `blfb1` decimal(3, 0) NULL DEFAULT NULL,
  `jsfszwtg1` decimal(3, 0) NULL DEFAULT NULL,
  `jt1` decimal(4, 0) NULL DEFAULT NULL,
  `fdgz1` decimal(3, 0) NULL DEFAULT NULL,
  `jjjy1` decimal(3, 0) NULL DEFAULT NULL,
  `dfbt1` decimal(4, 0) NULL DEFAULT NULL,
  `gwjt1` decimal(3, 0) NULL DEFAULT NULL,
  `hj2` decimal(4, 0) NULL DEFAULT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzse2` decimal(4, 0) NULL DEFAULT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbgzse2` decimal(4, 0) NULL DEFAULT NULL,
  `jcgz2` decimal(3, 0) NULL DEFAULT NULL,
  `glgz2` decimal(3, 0) NULL DEFAULT NULL,
  `jsdjgz2` decimal(3, 0) NULL DEFAULT NULL,
  `grjj2` decimal(3, 0) NULL DEFAULT NULL,
  `blfb2` decimal(3, 0) NULL DEFAULT NULL,
  `jsfszwtg2` decimal(3, 0) NULL DEFAULT NULL,
  `jt2` decimal(4, 0) NULL DEFAULT NULL,
  `fdgz2` decimal(3, 0) NULL DEFAULT NULL,
  `jjjy2` decimal(3, 0) NULL DEFAULT NULL,
  `dfbt2` decimal(4, 0) NULL DEFAULT NULL,
  `gwjt2` decimal(4, 0) NULL DEFAULT NULL,
  `bh` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jxgz` decimal(3, 0) NULL DEFAULT NULL,
  `zzbc` decimal(3, 0) NULL DEFAULT NULL,
  `zwjt` decimal(3, 0) NULL DEFAULT NULL,
  `zfbt` decimal(3, 0) NULL DEFAULT NULL,
  `dsznf` decimal(2, 0) NULL DEFAULT NULL,
  `nzgwsf` decimal(2, 0) NULL DEFAULT NULL,
  `jzmcbt` decimal(3, 0) NULL DEFAULT NULL,
  `sdbt1` decimal(3, 0) NULL DEFAULT NULL,
  `sdbt` decimal(3, 0) NULL DEFAULT NULL,
  `grsds` decimal(4, 1) NULL DEFAULT NULL,
  `zfgjj` decimal(4, 1) NULL DEFAULT NULL,
  `ylbxf` decimal(4, 1) NULL DEFAULT NULL,
  `ylf` decimal(4, 1) NULL DEFAULT NULL,
  `qtdk` decimal(4, 1) NULL DEFAULT NULL,
  `bfyqgz` decimal(5, 1) NULL DEFAULT NULL,
  `kjyqgz` decimal(5, 1) NULL DEFAULT NULL,
  `sfgz` decimal(5, 1) NULL DEFAULT NULL,
  `qtbt` decimal(3, 0) NULL DEFAULT NULL,
  `jxjt` decimal(3, 0) NULL DEFAULT NULL,
  `gryhzh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jxjt1` decimal(3, 0) NULL DEFAULT NULL,
  `jxgz1` decimal(3, 0) NULL DEFAULT NULL,
  `zwjt1` decimal(3, 0) NULL DEFAULT NULL,
  `dsznf1` decimal(3, 0) NULL DEFAULT NULL,
  `nzgwsf1` decimal(3, 0) NULL DEFAULT NULL,
  `jzmcbt1` decimal(3, 0) NULL DEFAULT NULL,
  `qtbt1` decimal(3, 0) NULL DEFAULT NULL,
  `tfnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tfyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `spdw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tbnd1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jxjtbz1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jxjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbtbz1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jhljt` decimal(3, 0) NULL DEFAULT NULL,
  `jhljt1` decimal(3, 0) NULL DEFAULT NULL,
  `zfbt1` decimal(3, 0) NULL DEFAULT NULL,
  `mz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `pgbc` decimal(5, 0) NULL DEFAULT NULL,
  `pgbc1` decimal(5, 0) NULL DEFAULT NULL,
  `sidbt` decimal(3, 0) NULL DEFAULT NULL,
  `zzmm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fdgd` decimal(1, 0) NULL DEFAULT NULL,
  `fdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ydwzw` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `yzwrzsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nrjxgzbf` decimal(3, 0) NULL DEFAULT NULL,
  `tgblbf` decimal(3, 0) NULL DEFAULT NULL,
  `nrjxgzbf1` decimal(3, 0) NULL DEFAULT NULL,
  `tgblbf1` decimal(3, 0) NULL DEFAULT NULL,
  `bz` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jcjtbz1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jcjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `spjtbz1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `spjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dah` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `njbt1` decimal(5, 1) NULL DEFAULT NULL,
  `njbt` decimal(5, 1) NULL DEFAULT NULL,
  `gwjtbz1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gwjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gwjtlb1` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gwjtlb` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_blfb
-- ----------------------------
DROP TABLE IF EXISTS `bz06_blfb`;
CREATE TABLE `bz06_blfb`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL,
  INDEX `zwbm`(`zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_djgz
-- ----------------------------
DROP TABLE IF EXISTS `bz06_djgz`;
CREATE TABLE `bz06_djgz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dc1` int NOT NULL,
  `dc2` int NOT NULL,
  `dc3` int NOT NULL,
  `dc4` int NOT NULL,
  `dc5` int NOT NULL,
  `dc6` int NOT NULL,
  `dc7` int NOT NULL,
  `dc8` int NOT NULL,
  `dc9` int NOT NULL,
  `dc10` int NOT NULL,
  `dc11` int NOT NULL,
  `dc12` int NOT NULL,
  `dc13` int NOT NULL,
  `dc14` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_fjtgb
-- ----------------------------
DROP TABLE IF EXISTS `bz06_fjtgb`;
CREATE TABLE `bz06_fjtgb`  (
  `jb8` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb9` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb10` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb11` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb12` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb13` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb14` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb15` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb16` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb17` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb18` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb19` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb20` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb21` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb22` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb23` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb24` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb25` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb26` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d035` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d036` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d037` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d038` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d039` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d03a` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d03b` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d03c` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `d03d` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_jbgz
-- ----------------------------
DROP TABLE IF EXISTS `bz06_jbgz`;
CREATE TABLE `bz06_jbgz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dc1` int NOT NULL,
  `dc2` int NOT NULL,
  `dc3` int NOT NULL,
  `dc4` int NOT NULL,
  `dc5` int NOT NULL,
  `dc6` int NOT NULL,
  `dc7` int NOT NULL,
  `dc8` int NOT NULL,
  `dc9` int NOT NULL,
  `dc10` int NOT NULL,
  `dc11` int NOT NULL,
  `dc12` int NOT NULL,
  `dc13` int NOT NULL,
  `dc14` int NOT NULL,
  `dc15` int NOT NULL,
  `dc16` int NOT NULL,
  `dc17` int NOT NULL,
  `dc18` int NOT NULL,
  `dc19` int NOT NULL,
  `dc20` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_jbt
-- ----------------------------
DROP TABLE IF EXISTS `bz06_jbt`;
CREATE TABLE `bz06_jbt`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `item` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `worklower` int NOT NULL,
  `workupper` int NOT NULL,
  `bz` int NOT NULL,
  `jxlb` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30186 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_jjjy
-- ----------------------------
DROP TABLE IF EXISTS `bz06_jjjy`;
CREATE TABLE `bz06_jjjy`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` int NOT NULL,
  `a2` int NOT NULL,
  `a3` int NOT NULL,
  `a4` int NOT NULL,
  `a5` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_jxgz
-- ----------------------------
DROP TABLE IF EXISTS `bz06_jxgz`;
CREATE TABLE `bz06_jxgz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_tgb
-- ----------------------------
DROP TABLE IF EXISTS `bz06_tgb`;
CREATE TABLE `bz06_tgb`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rzns` decimal(2, 0) NOT NULL,
  `rznz` decimal(2, 0) NOT NULL,
  `tgns` decimal(2, 0) NOT NULL,
  `tgnz` decimal(2, 0) NOT NULL,
  `jb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_xjgz
-- ----------------------------
DROP TABLE IF EXISTS `bz06_xjgz`;
CREATE TABLE `bz06_xjgz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwflbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xj` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NULL DEFAULT NULL,
  `jc` int NULL DEFAULT NULL,
  `jce` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_zw_gw
-- ----------------------------
DROP TABLE IF EXISTS `bz06_zw_gw`;
CREATE TABLE `bz06_zw_gw`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwmc` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tj1` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_zw_jb_xj
-- ----------------------------
DROP TABLE IF EXISTS `bz06_zw_jb_xj`;
CREATE TABLE `bz06_zw_jb_xj`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `max` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `min` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `id` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_zwgz
-- ----------------------------
DROP TABLE IF EXISTS `bz06_zwgz`;
CREATE TABLE `bz06_zwgz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_zwgz_fj
-- ----------------------------
DROP TABLE IF EXISTS `bz06_zwgz_fj`;
CREATE TABLE `bz06_zwgz_fj`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dc1` int NOT NULL,
  `dc2` int NOT NULL,
  `dc3` int NOT NULL,
  `dc4` int NOT NULL,
  `dc5` int NOT NULL,
  `dc6` int NOT NULL,
  `dc7` int NOT NULL,
  `dc8` int NOT NULL,
  `dc9` int NOT NULL,
  `dc10` int NOT NULL,
  `dc11` int NOT NULL,
  `dc12` int NOT NULL,
  `dc13` int NOT NULL,
  `dc14` int NOT NULL,
  `dc15` int NOT NULL,
  `dc16` int NOT NULL,
  `dc17` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_zwgz_gr
-- ----------------------------
DROP TABLE IF EXISTS `bz06_zwgz_gr`;
CREATE TABLE `bz06_zwgz_gr`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dc1` int NOT NULL,
  `dc2` int NOT NULL,
  `dc3` int NOT NULL,
  `dc4` int NOT NULL,
  `dc5` int NOT NULL,
  `dc6` int NOT NULL,
  `dc7` int NOT NULL,
  `dc8` int NOT NULL,
  `dc9` int NOT NULL,
  `dc10` int NOT NULL,
  `dc11` int NOT NULL,
  `dc12` int NOT NULL,
  `dc13` int NOT NULL,
  `dc14` int NOT NULL,
  `dc15` int NOT NULL,
  `dc16` int NOT NULL,
  `dc17` int NOT NULL,
  `dc18` int NOT NULL,
  `dc19` int NOT NULL,
  `dc20` int NOT NULL,
  `jsdjgz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz06_zzdz
-- ----------------------------
DROP TABLE IF EXISTS `bz06_zzdz`;
CREATE TABLE `bz06_zzdz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xlmc` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzzwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzzwmc` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gz1` int NOT NULL,
  `gz2` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_ggxbt
-- ----------------------------
DROP TABLE IF EXISTS `bz_ggxbt`;
CREATE TABLE `bz_ggxbt`  (
  `id` int NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwmc` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cb` int NULL DEFAULT NULL,
  `wybt` int NULL DEFAULT NULL,
  `txbt` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_gwjt
-- ----------------------------
DROP TABLE IF EXISTS `bz_gwjt`;
CREATE TABLE `bz_gwjt`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` decimal(5, 0) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_jcjxj
-- ----------------------------
DROP TABLE IF EXISTS `bz_jcjxj`;
CREATE TABLE `bz_jcjxj`  (
  `id` int NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwmc` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bz` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_pskhj
-- ----------------------------
DROP TABLE IF EXISTS `bz_pskhj`;
CREATE TABLE `bz_pskhj`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khjg` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL,
  `pjsp` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_txbt
-- ----------------------------
DROP TABLE IF EXISTS `bz_txbt`;
CREATE TABLE `bz_txbt`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_wmj
-- ----------------------------
DROP TABLE IF EXISTS `bz_wmj`;
CREATE TABLE `bz_wmj`  (
  `jb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL,
  `mul` decimal(4, 1) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_wybt
-- ----------------------------
DROP TABLE IF EXISTS `bz_wybt`;
CREATE TABLE `bz_wybt`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bz_yzf
-- ----------------------------
DROP TABLE IF EXISTS `bz_yzf`;
CREATE TABLE `bz_yzf`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cwjl
-- ----------------------------
DROP TABLE IF EXISTS `cwjl`;
CREATE TABLE `cwjl`  (
  `时间` datetime NOT NULL,
  `错误代码` decimal(4, 0) NOT NULL,
  `错误信息` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `程序代码` char(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `程序名` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cyxx
-- ----------------------------
DROP TABLE IF EXISTS `cyxx`;
CREATE TABLE `cyxx`  (
  `ID` int NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dwmc` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dwjc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zgry` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `szds` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zzrs` int NULL DEFAULT NULL,
  `skbz` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `shrq` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `yxhjsz` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bz` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwbh10` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zzdz10` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `qytrlxjs` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `qytrtg93` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `trdz10` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zyjrlxjs` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zydnjd` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgdcgljs` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xsdwywqx` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `blxs` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `swyz` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xsdwxg` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jzmcbtmc` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sdbtmc` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `spfs` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dwsplb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jqdm` decimal(12, 0) NULL DEFAULT NULL,
  `ltjddc` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jxgz` decimal(1, 0) NULL DEFAULT NULL,
  `jjjy` decimal(1, 0) NULL DEFAULT NULL,
  `fdgz` decimal(1, 0) NULL DEFAULT NULL,
  `pgbc` decimal(1, 0) NULL DEFAULT NULL,
  `softsn` char(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sp` tinyint(1) NULL DEFAULT NULL,
  `pict` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `path_bak` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwbhhjsdj` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cdchjsdj` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `autobak` decimal(1, 0) NULL DEFAULT NULL,
  `ask` decimal(1, 0) NULL DEFAULT NULL,
  `chkupdate` decimal(1, 0) NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for czrz
-- ----------------------------
DROP TABLE IF EXISTS `czrz`;
CREATE TABLE `czrz`  (
  `uid` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gn` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `wj` char(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sj` datetime NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for czyf
-- ----------------------------
DROP TABLE IF EXISTS `czyf`;
CREATE TABLE `czyf`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dfbt2` decimal(4, 0) NOT NULL,
  `jxlb` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for czyfbz
-- ----------------------------
DROP TABLE IF EXISTS `czyfbz`;
CREATE TABLE `czyfbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `js` int NOT NULL,
  `bz` int NOT NULL,
  `bm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dctgbz
-- ----------------------------
DROP TABLE IF EXISTS `dctgbz`;
CREATE TABLE `dctgbz`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdrznx` decimal(2, 0) NOT NULL,
  `zgrznx` decimal(2, 0) NOT NULL,
  `n1` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b1` decimal(4, 0) NOT NULL,
  `n2` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b2` decimal(4, 0) NOT NULL,
  `n3` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a3` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b3` decimal(4, 0) NOT NULL,
  `n4` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b4` decimal(4, 0) NOT NULL,
  `n5` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a5` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b5` decimal(4, 0) NOT NULL,
  `n6` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a6` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b6` decimal(4, 0) NOT NULL,
  `n7` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a7` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b7` decimal(4, 0) NOT NULL,
  `n8` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a8` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b8` decimal(4, 0) NOT NULL,
  `bz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for djxgz
-- ----------------------------
DROP TABLE IF EXISTS `djxgz`;
CREATE TABLE `djxgz`  (
  `id` int NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ny` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jcjx` decimal(6, 0) NOT NULL,
  `jljx` decimal(6, 0) NOT NULL,
  `yfje` decimal(6, 0) NOT NULL,
  `zcyf` decimal(6, 0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `grbm`(`grbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for djxgzb
-- ----------------------------
DROP TABLE IF EXISTS `djxgzb`;
CREATE TABLE `djxgzb`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ny` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jcjx` decimal(6, 0) NOT NULL,
  `jljx` decimal(6, 0) NOT NULL,
  `yfje` decimal(6, 0) NOT NULL,
  `zcyf` decimal(6, 0) NULL DEFAULT NULL,
  INDEX `grbm`(`grbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dmb
-- ----------------------------
DROP TABLE IF EXISTS `dmb`;
CREATE TABLE `dmb`  (
  `bm` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xt` tinyint NOT NULL,
  `sfsy` tinyint NOT NULL,
  INDEX `bm`(`bm` ASC) USING BTREE,
  INDEX `mc`(`mc` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dndkh
-- ----------------------------
DROP TABLE IF EXISTS `dndkh`;
CREATE TABLE `dndkh`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khnd` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khjg` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwbm`(`dwbm` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `khnd`(`khnd` ASC) USING BTREE,
  INDEX `ndbm`(`khnd` ASC, `dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 515940 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dndkhb
-- ----------------------------
DROP TABLE IF EXISTS `dndkhb`;
CREATE TABLE `dndkhb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khnd` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khjg` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwbm`(`dwbm` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `khnd`(`khnd` ASC) USING BTREE,
  INDEX `ndbm`(`khnd` ASC, `dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2125591 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dryjbxx
-- ----------------------------
DROP TABLE IF EXISTS `dryjbxx`;
CREATE TABLE `dryjbxx`  (
  `uid` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryfl` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwfl` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrfs` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdgznx` int NOT NULL,
  `gznx` int NOT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdjhlnx` int NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bjglxlnx` int NOT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `txsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bgdwjc` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgbl` int NOT NULL,
  `jtbl` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fddc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khqk` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dynkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `denkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bbz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bh` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gryhzh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spdw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzmm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdgd` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ydwzw` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `yzwrzsj` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dah` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfjzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `yctxsj` int NOT NULL,
  PRIMARY KEY (`uid`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `dwbm`(`dwbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27655 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dryjbxxb
-- ----------------------------
DROP TABLE IF EXISTS `dryjbxxb`;
CREATE TABLE `dryjbxxb`  (
  `uid` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryfl` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwfl` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrfs` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdgznx` int NOT NULL,
  `gznx` int NOT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdjhlnx` int NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bjglxlnx` int NOT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `txsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bgdwjc` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgbl` int NOT NULL,
  `jtbl` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fddc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khqk` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dynkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `denkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bbz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bh` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gryhzh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spdw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzmm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdgd` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ydwzw` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `yzwrzsj` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dah` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfjzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `yctxsj` int NOT NULL,
  `bz` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20475 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dryzwbh
-- ----------------------------
DROP TABLE IF EXISTS `dryzwbh`;
CREATE TABLE `dryzwbh`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xzzw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwlb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `kjnx` int NOT NULL,
  `xrzwbz` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsbz` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bmbz`(`dwbm` ASC, `grbm` ASC, `xzzw` ASC, `xrzwbz` ASC) USING BTREE,
  INDEX `bmlbbz`(`dwbm` ASC, `grbm` ASC, `zwlb` ASC, `xrzwbz` ASC) USING BTREE,
  INDEX `bmny`(`dwbm` ASC, `grbm` ASC, `zwbm` ASC, `srny` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `srny`(`srny` ASC) USING BTREE,
  INDEX `IX_dryzwbh_dwbm`(`dwbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 724777 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dryzwbhb
-- ----------------------------
DROP TABLE IF EXISTS `dryzwbhb`;
CREATE TABLE `dryzwbhb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xzzw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwlb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `kjnx` int NOT NULL,
  `xrzwbz` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsbz` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bmbz`(`dwbm` ASC, `grbm` ASC, `xzzw` ASC, `xrzwbz` ASC) USING BTREE,
  INDEX `bmlbbz`(`dwbm` ASC, `grbm` ASC, `zwlb` ASC, `xrzwbz` ASC) USING BTREE,
  INDEX `bmny`(`dwbm` ASC, `grbm` ASC, `zwbm` ASC, `srny` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `srny`(`srny` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 278734 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dtgxx
-- ----------------------------
DROP TABLE IF EXISTS `dtgxx`;
CREATE TABLE `dtgxx`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xlnx` decimal(2, 0) NOT NULL,
  `zdgznx` decimal(2, 0) NOT NULL,
  `kjnx` decimal(2, 0) NOT NULL,
  `tgnx` decimal(2, 0) NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rzsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rznx` decimal(2, 0) NOT NULL,
  `zwkjnx` decimal(2, 0) NOT NULL,
  `zwbm1` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc1` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rzsj1` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rznx1` decimal(2, 0) NOT NULL,
  `zwkjnx1` decimal(2, 0) NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgzwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgzw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gddc` decimal(1, 0) NOT NULL,
  `dddc` decimal(1, 0) NOT NULL,
  `gdjb` decimal(1, 0) NOT NULL,
  `ddjb` decimal(1, 0) NOT NULL,
  `remark` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 101271 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dtgxxb
-- ----------------------------
DROP TABLE IF EXISTS `dtgxxb`;
CREATE TABLE `dtgxxb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xlnx` decimal(2, 0) NOT NULL,
  `zdgznx` decimal(2, 0) NOT NULL,
  `kjnx` decimal(2, 0) NOT NULL,
  `tgnx` decimal(2, 0) NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rzsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rznx` decimal(2, 0) NOT NULL,
  `zwkjnx` decimal(2, 0) NOT NULL,
  `zwbm1` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc1` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rzsj1` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rznx1` decimal(2, 0) NOT NULL,
  `zwkjnx1` decimal(2, 0) NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgzwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgzw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gddc` decimal(1, 0) NOT NULL,
  `dddc` decimal(1, 0) NOT NULL,
  `gdjb` decimal(1, 0) NOT NULL,
  `ddjb` decimal(1, 0) NOT NULL,
  `remark` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 59467 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dwbm
-- ----------------------------
DROP TABLE IF EXISTS `dwbm`;
CREATE TABLE `dwbm`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwmc` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwmc1` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbz` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bzrs` int NOT NULL,
  `zbrs` int NOT NULL,
  `slrs` int NOT NULL,
  `dwjc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gzczbz` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jtbz` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gjzcrsxd` int NOT NULL,
  `zjzcrsxd` int NOT NULL,
  `cjzcrsxd` int NOT NULL,
  `bz` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `yhzh` char(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bin` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `csbz` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tfyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `kmbm` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbz` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `kzfgjj` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `kylbxf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ltrs` int NOT NULL,
  `jkjs` tinyint(1) NOT NULL,
  `jfly` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dfbt` decimal(1, 0) NOT NULL,
  `jb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xtlb` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jglb` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgbm` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tby` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tbd` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `frzsh` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxlb` decimal(1, 0) NOT NULL,
  `njbt` decimal(1, 0) NOT NULL,
  `dwcc` decimal(1, 0) NOT NULL,
  `sshy` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfqyhgl` decimal(1, 0) NOT NULL,
  `jxbl` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a0760` int NOT NULL,
  `a0770` int NOT NULL,
  `a0780` int NOT NULL,
  `a0790` int NOT NULL,
  `a07a0` int NOT NULL,
  `a07b0` int NOT NULL,
  `a07c0` int NOT NULL,
  `a1002` int NOT NULL,
  `a1003` int NOT NULL,
  `a1004` int NOT NULL,
  `a1005` int NOT NULL,
  `a1006` int NOT NULL,
  `a1007` int NOT NULL,
  `a1008` int NOT NULL,
  `a1009` int NOT NULL,
  `a1010` int NOT NULL,
  `a1011` int NOT NULL,
  `a1012` int NOT NULL,
  `a1013` int NOT NULL,
  `a0801` int NOT NULL,
  `a0802` int NOT NULL,
  `a0803` int NOT NULL,
  `a0804` int NOT NULL,
  `a0805` int NOT NULL,
  `nzj2010` decimal(1, 0) NOT NULL,
  `nzj2011` decimal(1, 0) NOT NULL,
  `nzj2012` decimal(1, 0) NOT NULL,
  `nzj2013` decimal(1, 0) NOT NULL,
  `gqbz` int NOT NULL,
  `wmdw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwbm`(`dwbm` ASC) USING BTREE,
  INDEX `yhzh`(`yhzh` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12952 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dxl
-- ----------------------------
DROP TABLE IF EXISTS `dxl`;
CREATE TABLE `dxl`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `byyx` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rxsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bysj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xz` int NOT NULL,
  `xllb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bmny`(`dwbm` ASC, `grbm` ASC, `bysj` ASC) USING BTREE,
  INDEX `bmxl`(`dwbm` ASC, `grbm` ASC, `xl` ASC) USING BTREE,
  INDEX `bmxlny`(`dwbm` ASC, `grbm` ASC, `xl` ASC, `bysj` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 193732 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dxlb
-- ----------------------------
DROP TABLE IF EXISTS `dxlb`;
CREATE TABLE `dxlb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `byyx` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rxsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bysj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xz` int NOT NULL,
  `xllb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bmny`(`dwbm` ASC, `grbm` ASC, `bysj` ASC) USING BTREE,
  INDEX `bmxl`(`dwbm` ASC, `grbm` ASC, `xl` ASC) USING BTREE,
  INDEX `bmxlny`(`dwbm` ASC, `grbm` ASC, `xl` ASC, `bysj` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 159574 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fjjxzl
-- ----------------------------
DROP TABLE IF EXISTS `fjjxzl`;
CREATE TABLE `fjjxzl`  (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nd` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cate` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgz` int NOT NULL,
  `jbt` int NOT NULL,
  `cb` int NOT NULL,
  `nzj` int NOT NULL,
  `rs` int NOT NULL,
  `jxzl` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fldgz
-- ----------------------------
DROP TABLE IF EXISTS `fldgz`;
CREATE TABLE `fldgz`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `sequence` decimal(2, 0) NOT NULL,
  `field_cate` tinyint NOT NULL,
  `tblname` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_name` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_len` decimal(3, 0) NOT NULL,
  `field_dec` decimal(1, 0) NOT NULL,
  `field_cap` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_caps` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_capj` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfsp` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfsy06` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfsy` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lrfs` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category6` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbt` tinyint NOT NULL,
  `gld` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxryff` decimal(1, 0) NOT NULL,
  `jbtbz` tinyint NOT NULL,
  `qsff` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gdz` decimal(4, 1) NOT NULL,
  `dmlb` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dmlb06` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `readonly` tinyint NOT NULL,
  `isgroup` decimal(1, 0) NOT NULL,
  `iscount` decimal(1, 0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fldname`(`field_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 127 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fldjbxx
-- ----------------------------
DROP TABLE IF EXISTS `fldjbxx`;
CREATE TABLE `fldjbxx`  (
  `sequence` int NOT NULL,
  `field_cate` tinyint NOT NULL,
  `must` tinyint NOT NULL,
  `category` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfsy` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xzry` tinyint NOT NULL,
  `isgz` tinyint NOT NULL,
  `property` tinyint NOT NULL,
  `readonly` tinyint NOT NULL,
  `tblname` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_name` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_len` decimal(3, 0) NOT NULL,
  `field_dec` decimal(1, 0) NOT NULL,
  `field_cap` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_capj` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dmlb` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_null` tinyint NOT NULL,
  `canbyhand` tinyint NOT NULL,
  `isgroup` decimal(1, 0) NOT NULL,
  `iscount` decimal(1, 0) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fldprop
-- ----------------------------
DROP TABLE IF EXISTS `fldprop`;
CREATE TABLE `fldprop`  (
  `field_name` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_cap` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_prop` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dmlb` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_len` decimal(3, 0) NOT NULL,
  `field_dec` decimal(3, 0) NOT NULL,
  `field_null` tinyint NOT NULL,
  `field_nocp` tinyint NOT NULL,
  `table_name` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_cate` tinyint NOT NULL,
  `sfsy` tinyint NOT NULL,
  `jbt` tinyint NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gcbz
-- ----------------------------
DROP TABLE IF EXISTS `gcbz`;
CREATE TABLE `gcbz`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for glyj
-- ----------------------------
DROP TABLE IF EXISTS `glyj`;
CREATE TABLE `glyj`  (
  `yjsj` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rs` decimal(6, 0) NOT NULL,
  `zze` decimal(9, 1) NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwmc` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gzbzdz
-- ----------------------------
DROP TABLE IF EXISTS `gzbzdz`;
CREATE TABLE `gzbzdz`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dzbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gzbzlb
-- ----------------------------
DROP TABLE IF EXISTS `gzbzlb`;
CREATE TABLE `gzbzlb`  (
  `bm` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gzbzbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xt` tinyint(1) NOT NULL,
  `sfsy` tinyint(1) NOT NULL,
  `jbtsz` tinyint(1) NOT NULL,
  `blfb2` decimal(4, 0) NOT NULL,
  `id` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hisbase
-- ----------------------------
DROP TABLE IF EXISTS `hisbase`;
CREATE TABLE `hisbase`  (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryfl` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwfl` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrfs` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdgznx` int NOT NULL,
  `gznx` int NOT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdjhlnx` int NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bjglxlnx` int NOT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndzw` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndjb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bgdwjc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgbl` int NOT NULL,
  `jtbl` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fddc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdgd` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khqk` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dynkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `denkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bbz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hj2` int NOT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `jcgz2` int NOT NULL,
  `glgz2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `grjj2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `jt2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `gwjt2` int NOT NULL,
  `bh` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxgz` int NOT NULL,
  `zzbc` int NOT NULL,
  `zwjt` int NOT NULL,
  `zfbt` int NOT NULL,
  `dsznf` int NOT NULL,
  `nzgwsf` int NOT NULL,
  `jzmcbt` int NOT NULL,
  `sdbt` int NOT NULL,
  `grsds` decimal(6, 1) NOT NULL,
  `zfgjj` decimal(6, 1) NOT NULL,
  `ylbxf` decimal(6, 1) NOT NULL,
  `ylf` decimal(6, 1) NOT NULL,
  `qtdk` decimal(6, 1) NOT NULL,
  `bfyqgz` decimal(6, 1) NOT NULL,
  `kjyqgz` decimal(6, 1) NOT NULL,
  `sfgz` decimal(6, 1) NOT NULL,
  `qtbt` int NOT NULL,
  `jxjt` int NOT NULL,
  `gryhzh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tfnf` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tfyf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spdw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jhljt` int NOT NULL,
  `pgbc` int NOT NULL,
  `sidbt` int NOT NULL,
  `jzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nrjxgzbf` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jcjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `njbt` decimal(6, 1) NOT NULL,
  `gwjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwjtlb` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfjzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sid` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `id`(`id` ASC) USING BTREE,
  INDEX `sid`(`sid` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `jslb`(`dwbm` ASC, `grbm` ASC, `jsnf` ASC, `jsyf` ASC, `jslb` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hisbaseb
-- ----------------------------
DROP TABLE IF EXISTS `hisbaseb`;
CREATE TABLE `hisbaseb`  (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryfl` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwfl` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jrfs` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdgznx` int NOT NULL,
  `gznx` int NOT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdjhlnx` int NOT NULL,
  `xlbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bjglxlnx` int NOT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndzw` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndjb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bgdwjc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgbl` int NOT NULL,
  `jtbl` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fddc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdgd` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsyf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khqk` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dynkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `denkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bbz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hj2` int NOT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `jcgz2` int NOT NULL,
  `glgz2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `grjj2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `jt2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `gwjt2` int NOT NULL,
  `bh` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxgz` int NOT NULL,
  `zzbc` int NOT NULL,
  `zwjt` int NOT NULL,
  `zfbt` int NOT NULL,
  `dsznf` int NOT NULL,
  `nzgwsf` int NOT NULL,
  `jzmcbt` int NOT NULL,
  `sdbt` int NOT NULL,
  `grsds` decimal(6, 1) NOT NULL,
  `zfgjj` decimal(6, 1) NOT NULL,
  `ylbxf` decimal(6, 1) NOT NULL,
  `ylf` decimal(6, 1) NOT NULL,
  `qtdk` decimal(6, 1) NOT NULL,
  `bfyqgz` decimal(6, 1) NOT NULL,
  `kjyqgz` decimal(6, 1) NOT NULL,
  `sfgz` decimal(6, 1) NOT NULL,
  `qtbt` int NOT NULL,
  `jxjt` int NOT NULL,
  `gryhzh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tfnf` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tfyf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spdw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jhljt` int NOT NULL,
  `pgbc` int NOT NULL,
  `sidbt` int NOT NULL,
  `jzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nrjxgzbf` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jcjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `njbt` decimal(6, 1) NOT NULL,
  `gwjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwjtlb` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfjzgb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sid` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  INDEX `xbm`(`dwbm` ASC, `grbm` ASC, `sid` ASC) USING BTREE,
  INDEX `sid`(`sid` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hjxx
-- ----------------------------
DROP TABLE IF EXISTS `hjxx`;
CREATE TABLE `hjxx`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hjmc` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sjdw` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jllx` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hjsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tqyjjssj` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `qtqk` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jldc` int NOT NULL,
  `jljb` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `bm`(`dwbm` ASC, `grbm` ASC, `tqyjjssj` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5036 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hjxxb
-- ----------------------------
DROP TABLE IF EXISTS `hjxxb`;
CREATE TABLE `hjxxb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hjmc` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sjdw` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jllx` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hjsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tqyjjssj` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `qtqk` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jldc` int NOT NULL,
  `jljb` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bm`(`dwbm` ASC, `grbm` ASC, `tqyjjssj` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2295 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jbbz
-- ----------------------------
DROP TABLE IF EXISTS `jbbz`;
CREATE TABLE `jbbz`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  INDEX `zwbm`(`zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jbgzbz
-- ----------------------------
DROP TABLE IF EXISTS `jbgzbz`;
CREATE TABLE `jbgzbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` decimal(4, 0) NOT NULL,
  `a2` decimal(4, 0) NOT NULL,
  `a3` decimal(4, 0) NOT NULL,
  `a4` decimal(4, 0) NOT NULL,
  `a5` decimal(4, 0) NOT NULL,
  `a6` decimal(4, 0) NOT NULL,
  `a7` decimal(4, 0) NOT NULL,
  `a8` decimal(4, 0) NOT NULL,
  `a9` decimal(4, 0) NOT NULL,
  `a10` decimal(4, 0) NOT NULL,
  `a11` decimal(4, 0) NOT NULL,
  `a12` decimal(4, 0) NOT NULL,
  `a13` decimal(4, 0) NOT NULL,
  `a14` decimal(4, 0) NOT NULL,
  `a15` decimal(4, 0) NOT NULL,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jbtbz
-- ----------------------------
DROP TABLE IF EXISTS `jbtbz`;
CREATE TABLE `jbtbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `blfb2` decimal(3, 0) NOT NULL,
  `zwjt` decimal(3, 0) NOT NULL,
  `dfbt2` decimal(4, 0) NOT NULL,
  `gwjt2` decimal(3, 0) NOT NULL,
  `zfbt` decimal(3, 0) NOT NULL,
  `jzmcbt` decimal(2, 1) NOT NULL,
  `sdbt` decimal(3, 0) NOT NULL,
  `sidbt` decimal(3, 0) NOT NULL,
  `qtbt` decimal(3, 0) NOT NULL,
  `nzgwsf` decimal(3, 0) NOT NULL,
  INDEX `zwbm`(`bm` ASC) USING BTREE,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE,
  INDEX `ndbm`(`tbnd` ASC, `bm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jbtbz06
-- ----------------------------
DROP TABLE IF EXISTS `jbtbz06`;
CREATE TABLE `jbtbz06`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(26) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `blfb2` decimal(3, 0) NULL DEFAULT NULL,
  `zwjt` decimal(3, 0) NULL DEFAULT NULL,
  `dfbt2` decimal(4, 0) NULL DEFAULT NULL,
  `gwjt2` decimal(3, 0) NULL DEFAULT NULL,
  `zfbt` decimal(3, 0) NULL DEFAULT NULL,
  `jzmcbt` decimal(2, 1) NULL DEFAULT NULL,
  `sdbt` decimal(3, 0) NULL DEFAULT NULL,
  `sidbt` decimal(3, 0) NULL DEFAULT NULL,
  `qtbt` decimal(3, 0) NULL DEFAULT NULL,
  `nzgwsf` decimal(3, 0) NULL DEFAULT NULL,
  `ltlb` decimal(1, 0) NULL DEFAULT NULL,
  `jxlb` decimal(1, 0) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jbtgbz
-- ----------------------------
DROP TABLE IF EXISTS `jbtgbz`;
CREATE TABLE `jbtgbz`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zgjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `n1` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b1` decimal(4, 0) NOT NULL,
  `n2` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b2` decimal(4, 0) NOT NULL,
  `n3` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a3` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b3` decimal(4, 0) NOT NULL,
  `n4` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b4` decimal(4, 0) NOT NULL,
  `n5` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a5` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b5` decimal(4, 0) NOT NULL,
  `n6` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a6` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b6` decimal(4, 0) NOT NULL,
  `n7` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a7` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b7` decimal(4, 0) NOT NULL,
  `n8` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a8` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b8` decimal(4, 0) NOT NULL,
  `n9` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a9` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b9` decimal(4, 0) NOT NULL,
  INDEX `zwbm`(`zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jcglgzbz
-- ----------------------------
DROP TABLE IF EXISTS `jcglgzbz`;
CREATE TABLE `jcglgzbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jcgz` decimal(4, 0) NOT NULL,
  `glgz` decimal(3, 0) NOT NULL,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jdgzbz
-- ----------------------------
DROP TABLE IF EXISTS `jdgzbz`;
CREATE TABLE `jdgzbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsjg` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zyjg` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fzywzgb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` decimal(4, 0) NOT NULL,
  `a2` decimal(4, 0) NOT NULL,
  `a3` decimal(4, 0) NOT NULL,
  `a4` decimal(4, 0) NOT NULL,
  `a5` decimal(4, 0) NOT NULL,
  `a6` decimal(4, 0) NOT NULL,
  `a7` decimal(4, 0) NOT NULL,
  `a8` decimal(4, 0) NOT NULL,
  `a9` decimal(4, 0) NOT NULL,
  `a10` decimal(4, 0) NOT NULL,
  `a11` decimal(4, 0) NOT NULL,
  `a12` decimal(4, 0) NOT NULL,
  `jx` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbbz` decimal(4, 0) NOT NULL,
  `a13` decimal(4, 0) NOT NULL,
  `a14` decimal(4, 0) NOT NULL,
  `a15` decimal(4, 0) NOT NULL,
  `a16` decimal(4, 0) NOT NULL,
  `a17` decimal(4, 0) NOT NULL,
  `a18` decimal(4, 0) NOT NULL,
  `a19` decimal(4, 0) NOT NULL,
  `a20` decimal(4, 0) NOT NULL,
  `nzze` decimal(4, 0) NOT NULL,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE,
  INDEX `ndbm`(`tbnd` ASC, `zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jdzw
-- ----------------------------
DROP TABLE IF EXISTS `jdzw`;
CREATE TABLE `jdzw`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rzsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3102 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jdzwb
-- ----------------------------
DROP TABLE IF EXISTS `jdzwb`;
CREATE TABLE `jdzwb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rzsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6244 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jfjs
-- ----------------------------
DROP TABLE IF EXISTS `jfjs`;
CREATE TABLE `jfjs`  (
  `id` int NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nd` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzse2` int NULL DEFAULT NULL,
  `jbgzse2` int NULL DEFAULT NULL,
  `jsdjgz2` int NULL DEFAULT NULL,
  `jsfszwtg2` decimal(18, 0) NULL DEFAULT NULL,
  `fdgz2` decimal(18, 0) NULL DEFAULT NULL,
  `dfbt2` int NULL DEFAULT NULL,
  `blfb2` int NULL DEFAULT NULL,
  `jjjy2` int NULL DEFAULT NULL,
  `jxjt` int NULL DEFAULT NULL,
  `jhljt` int NULL DEFAULT NULL,
  `tgblbf` int NULL DEFAULT NULL,
  `js` int NOT NULL,
  `bz` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jfjsb
-- ----------------------------
DROP TABLE IF EXISTS `jfjsb`;
CREATE TABLE `jfjsb`  (
  `id` int NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nd` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzse2` int NULL DEFAULT NULL,
  `jbgzse2` int NULL DEFAULT NULL,
  `jsdjgz2` int NULL DEFAULT NULL,
  `jsfszwtg2` decimal(18, 0) NULL DEFAULT NULL,
  `fdgz2` decimal(18, 0) NULL DEFAULT NULL,
  `dfbt2` int NULL DEFAULT NULL,
  `blfb2` int NULL DEFAULT NULL,
  `jjjy2` int NULL DEFAULT NULL,
  `jxjt` int NULL DEFAULT NULL,
  `jhljt` int NULL DEFAULT NULL,
  `tgblbf` int NULL DEFAULT NULL,
  `js` int NOT NULL,
  `bz` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jsdjgzbz
-- ----------------------------
DROP TABLE IF EXISTS `jsdjgzbz`;
CREATE TABLE `jsdjgzbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsdjgz` decimal(4, 0) NOT NULL,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE,
  INDEX `ndbm`(`tbnd` ASC, `zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jtgzzc
-- ----------------------------
DROP TABLE IF EXISTS `jtgzzc`;
CREATE TABLE `jtgzzc`  (
  `xmmc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sp` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bt` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `qsff` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gdz` decimal(4, 1) NOT NULL,
  `lrfs` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxryff` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `wbgbh` decimal(2, 0) NOT NULL,
  `fgl` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gldy` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsqzdm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jshzdm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jx
-- ----------------------------
DROP TABLE IF EXISTS `jx`;
CREATE TABLE `jx`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sysj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `syyy` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rmwh` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrjxbz` tinyint NOT NULL,
  `lb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `sysj`(`sysj` ASC) USING BTREE,
  INDEX `bmjx`(`dwbm` ASC, `grbm` ASC, `jx` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 79227 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jxb
-- ----------------------------
DROP TABLE IF EXISTS `jxb`;
CREATE TABLE `jxb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sysj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `syyy` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rmwh` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrjxbz` tinyint(1) NOT NULL,
  `lb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE,
  INDEX `sysj`(`sysj` ASC) USING BTREE,
  INDEX `bmjx`(`dwbm` ASC, `grbm` ASC, `jx` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10696 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jxgzbz
-- ----------------------------
DROP TABLE IF EXISTS `jxgzbz`;
CREATE TABLE `jxgzbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryflbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryfl` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xl` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b1` decimal(4, 0) NOT NULL,
  `b2` decimal(4, 0) NOT NULL,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE,
  INDEX `ndflbmxl`(`tbnd` ASC, `ryflbm` ASC, `xl` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jxjs
-- ----------------------------
DROP TABLE IF EXISTS `jxjs`;
CREATE TABLE `jxjs`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jxjtbz
-- ----------------------------
DROP TABLE IF EXISTS `jxjtbz`;
CREATE TABLE `jxjtbz`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxbm` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jtbz` int NULL DEFAULT NULL,
  `lb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ndjx`(`lb` ASC, `tbnd` ASC, `jx` ASC) USING BTREE,
  INDEX `tbnd`(`lb` ASC, `tbnd` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 632 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jxny
-- ----------------------------
DROP TABLE IF EXISTS `jxny`;
CREATE TABLE `jxny`  (
  `cstart` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cend` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jxzl
-- ----------------------------
DROP TABLE IF EXISTS `jxzl`;
CREATE TABLE `jxzl`  (
  `nd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `multiple` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `totalnum` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jcjx` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jljx` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  INDEX `bmnd`(`dwbm` ASC, `nd` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jxzls
-- ----------------------------
DROP TABLE IF EXISTS `jxzls`;
CREATE TABLE `jxzls`  (
  `nd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `rs` int NULL DEFAULT NULL,
  `jcjx` decimal(18, 2) NULL DEFAULT NULL,
  `jxgzbz` decimal(18, 2) NULL DEFAULT NULL,
  `spsj` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jytgyb
-- ----------------------------
DROP TABLE IF EXISTS `jytgyb`;
CREATE TABLE `jytgyb`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jytgybb
-- ----------------------------
DROP TABLE IF EXISTS `jytgybb`;
CREATE TABLE `jytgybb`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jytgzzbf
-- ----------------------------
DROP TABLE IF EXISTS `jytgzzbf`;
CREATE TABLE `jytgzzbf`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cjgzny` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgnx` int NULL DEFAULT NULL,
  `zwgw1` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `rznx` int NULL DEFAULT NULL,
  `jb1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dc1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgzw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwzz` int NULL DEFAULT NULL,
  `zwbfys` int NULL DEFAULT NULL,
  `jbzz1` int NULL DEFAULT NULL,
  `jbbfys1` int NULL DEFAULT NULL,
  `jbzz2` int NULL DEFAULT NULL,
  `jbbfys2` int NULL DEFAULT NULL,
  `jbzz3` int NULL DEFAULT NULL,
  `jbbfys3` int NULL DEFAULT NULL,
  `jbzz4` int NULL DEFAULT NULL,
  `jbbfys4` int NULL DEFAULT NULL,
  `jbtzz1` int NULL DEFAULT NULL,
  `jtys1` int NULL DEFAULT NULL,
  `jbtzz2` int NULL DEFAULT NULL,
  `jtys2` int NULL DEFAULT NULL,
  `jbtzz3` int NULL DEFAULT NULL,
  `jtys3` int NULL DEFAULT NULL,
  `bz` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwbh` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cstart` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cend` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbtzz4` int NULL DEFAULT NULL,
  `jtys4` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jytgzzbfb
-- ----------------------------
DROP TABLE IF EXISTS `jytgzzbfb`;
CREATE TABLE `jytgzzbfb`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cjgzny` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgnx` int NULL DEFAULT NULL,
  `zwgw1` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `rznx` int NULL DEFAULT NULL,
  `jb1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dc1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgzw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwzz` int NULL DEFAULT NULL,
  `zwbfys` int NULL DEFAULT NULL,
  `jbzz1` int NULL DEFAULT NULL,
  `jbbfys1` int NULL DEFAULT NULL,
  `jbzz2` int NULL DEFAULT NULL,
  `jbbfys2` int NULL DEFAULT NULL,
  `jbzz3` int NULL DEFAULT NULL,
  `jbbfys3` int NULL DEFAULT NULL,
  `jbzz4` int NULL DEFAULT NULL,
  `jbbfys4` int NULL DEFAULT NULL,
  `jbtzz1` int NULL DEFAULT NULL,
  `jtys1` int NULL DEFAULT NULL,
  `jbtzz2` int NULL DEFAULT NULL,
  `jtys2` int NULL DEFAULT NULL,
  `jbtzz3` int NULL DEFAULT NULL,
  `jtys3` int NULL DEFAULT NULL,
  `bz` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwbh` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cstart` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cend` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbtzz4` int NULL DEFAULT NULL,
  `jtys4` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jzdzdz
-- ----------------------------
DROP TABLE IF EXISTS `jzdzdz`;
CREATE TABLE `jzdzdz`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `wz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zywz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fzywz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dfzw` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  INDEX `zwbm`(`zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lczjltxf
-- ----------------------------
DROP TABLE IF EXISTS `lczjltxf`;
CREATE TABLE `lczjltxf`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjltxf` int NULL DEFAULT NULL,
  `bdsj` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bdyj` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jcgz` int NULL DEFAULT NULL,
  `jbgz` int NULL DEFAULT NULL,
  `zwgz` int NULL DEFAULT NULL,
  `jt` int NULL DEFAULT NULL,
  `zwtg` int NULL DEFAULT NULL,
  `txf` int NULL DEFAULT NULL,
  `tzf` int NULL DEFAULT NULL,
  `gzbdxh` int NULL DEFAULT NULL,
  `oldzwdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 306085 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ltgzbz
-- ----------------------------
DROP TABLE IF EXISTS `ltgzbz`;
CREATE TABLE `ltgzbz`  (
  `ltlb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bdyj` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bdsj` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zze` decimal(4, 0) NOT NULL,
  `wjyj` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dc` decimal(1, 0) NOT NULL,
  `jt` decimal(1, 0) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mbkhj
-- ----------------------------
DROP TABLE IF EXISTS `mbkhj`;
CREATE TABLE `mbkhj`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `zwgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `jhljt` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jxjt` int NOT NULL,
  `gwjt` int NOT NULL,
  `qtbt` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `mbkhj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mbkhjb
-- ----------------------------
DROP TABLE IF EXISTS `mbkhjb`;
CREATE TABLE `mbkhjb`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `zwgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `jhljt` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jxjt` int NOT NULL,
  `gwjt` int NOT NULL,
  `qtbt` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `mbkhj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for njbt
-- ----------------------------
DROP TABLE IF EXISTS `njbt`;
CREATE TABLE `njbt`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` decimal(5, 1) NOT NULL,
  `a2` decimal(5, 1) NOT NULL,
  `a3` decimal(5, 1) NOT NULL,
  `a4` decimal(5, 1) NOT NULL,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for nxj
-- ----------------------------
DROP TABLE IF EXISTS `nxj`;
CREATE TABLE `nxj`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NULL DEFAULT NULL,
  `jxjt` int NULL DEFAULT NULL,
  `gwjt2` int NOT NULL,
  `nzj` int NULL DEFAULT NULL,
  `wybt` int NOT NULL,
  `txbt` int NULL DEFAULT NULL,
  `pskhj` int NOT NULL,
  `nxj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for nxjb
-- ----------------------------
DROP TABLE IF EXISTS `nxjb`;
CREATE TABLE `nxjb`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `gwjt2` int NOT NULL,
  `nzj` int NULL DEFAULT NULL,
  `wybt` int NOT NULL,
  `txbt` int NULL DEFAULT NULL,
  `pskhj` int NOT NULL,
  `nxj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for nzj
-- ----------------------------
DROP TABLE IF EXISTS `nzj`;
CREATE TABLE `nzj`  (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` varchar(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khjg` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `nzj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for nzjb
-- ----------------------------
DROP TABLE IF EXISTS `nzjb`;
CREATE TABLE `nzjb`  (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` varchar(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `khjg` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `nzj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pajsj
-- ----------------------------
DROP TABLE IF EXISTS `pajsj`;
CREATE TABLE `pajsj`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `zwgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `jhljt` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jxjt` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `pajsj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pajsjb
-- ----------------------------
DROP TABLE IF EXISTS `pajsjb`;
CREATE TABLE `pajsjb`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `zwgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `jhljt` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jxjt` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `pajsj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pf
-- ----------------------------
DROP TABLE IF EXISTS `pf`;
CREATE TABLE `pf`  (
  `a1` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a3` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a5` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a6` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a7` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a8` decimal(4, 0) NOT NULL,
  `a9` decimal(6, 0) NOT NULL,
  `a10` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a11` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a12` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a13` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a14` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a15` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a16` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spwj` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qtqk
-- ----------------------------
DROP TABLE IF EXISTS `qtqk`;
CREATE TABLE `qtqk`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bmlb`(`dwbm` ASC, `grbm` ASC, `jslb` ASC, `jsnf` ASC, `jsyf` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qtqkb
-- ----------------------------
DROP TABLE IF EXISTS `qtqkb`;
CREATE TABLE `qtqkb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bmlb`(`dwbm` ASC, `grbm` ASC, `jslb` ASC, `jsnf` ASC, `jsyf` ASC) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for rptinfo
-- ----------------------------
DROP TABLE IF EXISTS `rptinfo`;
CREATE TABLE `rptinfo`  (
  `lbbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cname` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ctitle` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cfilename` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rpttype` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bblb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dyclb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dycfw` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mrhs` int NOT NULL,
  `copies` int NOT NULL,
  `ptop` decimal(2, 1) NOT NULL,
  `ptop2` decimal(2, 1) NOT NULL,
  `pleft` decimal(2, 1) NOT NULL,
  `pleft2` decimal(2, 1) NOT NULL,
  `ptoph` decimal(2, 1) NOT NULL,
  `plefth` decimal(2, 1) NOT NULL,
  `para1` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `para2` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `para3` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `para4` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reportname` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `haddb` tinyint(1) NOT NULL,
  `cdefault` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lbmc` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ryjbxxb
-- ----------------------------
DROP TABLE IF EXISTS `ryjbxxb`;
CREATE TABLE `ryjbxxb`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `csny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ryfl` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gwfl` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zdgznx` int NULL DEFAULT NULL,
  `gznx` int NULL DEFAULT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bjglxlnx` int NULL DEFAULT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `txsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xckhndzw` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xckhndjb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bgdwjc` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `khqk` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dynkh` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `denkh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bbz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tgbl` int NULL DEFAULT NULL,
  `hj1` int NULL DEFAULT NULL,
  `zwbm1` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw1` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzdc1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzse1` int NULL DEFAULT NULL,
  `jbgzjb1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `djc1` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbgzse1` int NULL DEFAULT NULL,
  `jcgz1` int NULL DEFAULT NULL,
  `glgz1` int NULL DEFAULT NULL,
  `jsdjgz1` int NULL DEFAULT NULL,
  `grjj1` int NULL DEFAULT NULL,
  `blfb1` int NULL DEFAULT NULL,
  `jsfszwtg1` int NULL DEFAULT NULL,
  `jt1` int NULL DEFAULT NULL,
  `fdgz1` int NULL DEFAULT NULL,
  `jjjy1` int NULL DEFAULT NULL,
  `dfbt1` int NULL DEFAULT NULL,
  `gwjt1` int NULL DEFAULT NULL,
  `hj2` int NULL DEFAULT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgzse2` int NULL DEFAULT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbgzse2` int NULL DEFAULT NULL,
  `jcgz2` int NULL DEFAULT NULL,
  `glgz2` int NULL DEFAULT NULL,
  `jsdjgz2` int NULL DEFAULT NULL,
  `grjj2` int NULL DEFAULT NULL,
  `blfb2` int NULL DEFAULT NULL,
  `jsfszwtg2` int NULL DEFAULT NULL,
  `jt2` int NULL DEFAULT NULL,
  `fdgz2` int NULL DEFAULT NULL,
  `jjjy2` int NULL DEFAULT NULL,
  `dfbt2` int NULL DEFAULT NULL,
  `gwjt2` int NULL DEFAULT NULL,
  `bh` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jxgz1` int NULL DEFAULT NULL,
  `jxgz` int NULL DEFAULT NULL,
  `zzbc` int NULL DEFAULT NULL,
  `zwjt1` int NULL DEFAULT NULL,
  `zwjt` int NULL DEFAULT NULL,
  `zfbt` int NULL DEFAULT NULL,
  `dsznf1` int NULL DEFAULT NULL,
  `dsznf` int NULL DEFAULT NULL,
  `nzgwsf1` int NULL DEFAULT NULL,
  `nzgwsf` int NULL DEFAULT NULL,
  `jzmcbt1` int NULL DEFAULT NULL,
  `jzmcbt` int NULL DEFAULT NULL,
  `sdbt1` int NULL DEFAULT NULL,
  `sdbt` int NULL DEFAULT NULL,
  `grsds` decimal(6, 1) NULL DEFAULT NULL,
  `zfgjj` decimal(6, 1) NULL DEFAULT NULL,
  `ylbxf` decimal(6, 1) NULL DEFAULT NULL,
  `ylf` decimal(6, 1) NULL DEFAULT NULL,
  `qtdk` decimal(6, 1) NULL DEFAULT NULL,
  `bfyqgz` decimal(8, 1) NULL DEFAULT NULL,
  `kjyqgz` decimal(8, 1) NULL DEFAULT NULL,
  `sfgz` decimal(8, 1) NULL DEFAULT NULL,
  `qtbt1` int NULL DEFAULT NULL,
  `qtbt` int NULL DEFAULT NULL,
  `jxjt` int NULL DEFAULT NULL,
  `byyx` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bysj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gryhzh` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jxjt1` int NULL DEFAULT NULL,
  `tfnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbldxf` int NULL DEFAULT NULL,
  `lczjldxf` int NULL DEFAULT NULL,
  `tfyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jssj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gzbdxh` int NULL DEFAULT NULL,
  `zjblyy` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zjbl` int NULL DEFAULT NULL,
  `spdw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jshtbny` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jsqtbny` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zgdwsp` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ltyy` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xzzw` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xzzwdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `srsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jszwdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `qdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jkdwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jkgrbm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jbtbz1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jhljt` int NULL DEFAULT NULL,
  `jhljt1` int NULL DEFAULT NULL,
  `pzwh` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zdjhlnx` int NULL DEFAULT NULL,
  `jhl` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60754 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sc
-- ----------------------------
DROP TABLE IF EXISTS `sc`;
CREATE TABLE `sc`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `scyy` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sczlqk` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `scjl` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sjwj
-- ----------------------------
DROP TABLE IF EXISTS `sjwj`;
CREATE TABLE `sjwj`  (
  `fname` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `wjmc` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sym1` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `syb1` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sym2` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `syb2` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sym3` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `syb3` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sym4` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `syb4` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sym5` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `syb5` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `wjlj` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for srckhjg
-- ----------------------------
DROP TABLE IF EXISTS `srckhjg`;
CREATE TABLE `srckhjg`  (
  `bm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xz` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sy` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t1
-- ----------------------------
DROP TABLE IF EXISTS `t1`;
CREATE TABLE `t1`  (
  `id` int NOT NULL,
  `a1` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t2
-- ----------------------------
DROP TABLE IF EXISTS `t2`;
CREATE TABLE `t2`  (
  `id` int NOT NULL,
  `b1` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_systemlog
-- ----------------------------
DROP TABLE IF EXISTS `t_systemlog`;
CREATE TABLE `t_systemlog`  (
  `TableName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `KeyValue` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `FieldName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `OldValue` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `NewValue` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `Modifier` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ModifyDate` datetime NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_no
-- ----------------------------
DROP TABLE IF EXISTS `tb_no`;
CREATE TABLE `tb_no`  (
  `dwbm` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `currentNo` int NOT NULL,
  `BHlen` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_nob
-- ----------------------------
DROP TABLE IF EXISTS `tb_nob`;
CREATE TABLE `tb_nob`  (
  `dwbm` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `currentNo` int NOT NULL,
  `BHlen` int NOT NULL,
  PRIMARY KEY (`dwbm`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tfyj
-- ----------------------------
DROP TABLE IF EXISTS `tfyj`;
CREATE TABLE `tfyj`  (
  `zztxbz` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `yf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zrs` decimal(5, 0) NOT NULL,
  `xzrs` decimal(5, 0) NOT NULL,
  `czrs` decimal(4, 0) NOT NULL,
  `qgrs` decimal(5, 0) NOT NULL,
  `cgrs` decimal(5, 0) NOT NULL,
  `zzrs` decimal(6, 0) NOT NULL,
  `sfzje` decimal(9, 1) NOT NULL,
  `yfzje` decimal(9, 1) NOT NULL,
  `grsdsje` decimal(7, 1) NOT NULL,
  `zfgjjje` decimal(7, 1) NOT NULL,
  `ylbxje` decimal(7, 1) NOT NULL,
  `ylfje` decimal(7, 1) NOT NULL,
  `qtdkje` decimal(7, 1) NOT NULL,
  `bfje` decimal(9, 1) NOT NULL,
  `kjje` decimal(9, 1) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tgb
-- ----------------------------
DROP TABLE IF EXISTS `tgb`;
CREATE TABLE `tgb`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdrznx` decimal(2, 0) NOT NULL,
  `zgrznx` decimal(2, 0) NOT NULL,
  `a0103` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a0405` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a0607` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a0809` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1012` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1314` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1517` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1819` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2022` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2324` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2527` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2829` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a3032` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a3334` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a3537` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a3839` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4042` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4344` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4547` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4899` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tgbl
-- ----------------------------
DROP TABLE IF EXISTS `tgbl`;
CREATE TABLE `tgbl`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgbl` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lb` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hjxx` char(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jl` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xcjl` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tggzbz
-- ----------------------------
DROP TABLE IF EXISTS `tggzbz`;
CREATE TABLE `tggzbz`  (
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tggzbzbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zw` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdrznx` decimal(2, 0) NOT NULL,
  `zgrznx` decimal(2, 0) NOT NULL,
  `a1` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b1` decimal(4, 0) NOT NULL,
  `a2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b2` decimal(4, 0) NOT NULL,
  `a3` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b3` decimal(4, 0) NOT NULL,
  `a4` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b4` decimal(4, 0) NOT NULL,
  `a5` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b5` decimal(4, 0) NOT NULL,
  `a6` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b6` decimal(4, 0) NOT NULL,
  `a7` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b7` decimal(4, 0) NOT NULL,
  `a8` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `b8` decimal(4, 0) NOT NULL,
  `jsdjgz` decimal(4, 0) NOT NULL,
  `bz` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  INDEX `tggzbzbm`(`tggzbzbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tgjjjy
-- ----------------------------
DROP TABLE IF EXISTS `tgjjjy`;
CREATE TABLE `tgjjjy`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` decimal(4, 0) NOT NULL,
  `a2` decimal(4, 0) NOT NULL,
  `a3` decimal(4, 0) NOT NULL,
  `a4` decimal(4, 0) NOT NULL,
  `a5` decimal(4, 0) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tgqgz2006
-- ----------------------------
DROP TABLE IF EXISTS `tgqgz2006`;
CREATE TABLE `tgqgz2006`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryfl` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwfl` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdgznx` int NOT NULL,
  `gznx` int NOT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xllb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bjglxlnx` int NOT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndzw` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndjb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bgdwjc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgbl` int NOT NULL,
  `jtbl` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fddc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hj2` int NOT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `jcgz2` int NOT NULL,
  `glgz2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `grjj2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `jt2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `gwjt2` int NOT NULL,
  `jxgz` int NOT NULL,
  `zzbc` int NOT NULL,
  `zwjt` int NOT NULL,
  `zfbt` int NOT NULL,
  `dsznf` int NOT NULL,
  `nzgwsf` int NOT NULL,
  `jzmcbt` int NOT NULL,
  `sdbt` int NOT NULL,
  `qtbt` int NOT NULL,
  `jxjt` int NOT NULL,
  `jhljt` int NOT NULL,
  `pgbc` int NOT NULL,
  `fdgd` int NOT NULL,
  `fdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jcjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dah` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dwgrbm`(`dwbm` ASC, `grbm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14903 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tgqgz2006b
-- ----------------------------
DROP TABLE IF EXISTS `tgqgz2006b`;
CREATE TABLE `tgqgz2006b`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfzh` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ryfl` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwsx` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gwfl` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjgzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zzny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zdgznx` int NOT NULL,
  `gznx` int NOT NULL,
  `zgxl` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xllb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bjglxlnx` int NOT NULL,
  `tc` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndzw` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xckhndjb` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bgdwjc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwjb` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zjbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xrzw` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jx` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgbl` int NOT NULL,
  `jtbl` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fddc` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsnf` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jsyf` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jslb` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hj2` decimal(8, 1) NOT NULL,
  `zwbm2` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzse2` int NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `jcgz2` int NOT NULL,
  `glgz2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `grjj2` decimal(8, 1) NOT NULL,
  `blfb2` int NOT NULL,
  `jsfszwtg2` decimal(8, 1) NOT NULL,
  `jt2` decimal(8, 1) NOT NULL,
  `fdgz2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `gwjt2` int NOT NULL,
  `jxgz` int NOT NULL,
  `zzbc` int NOT NULL,
  `zwjt` int NOT NULL,
  `zfbt` int NOT NULL,
  `dsznf` int NOT NULL,
  `nzgwsf` int NOT NULL,
  `jzmcbt` int NOT NULL,
  `sdbt` int NOT NULL,
  `qtbt` int NOT NULL,
  `jxjt` int NOT NULL,
  `jhljt` int NOT NULL,
  `pgbc` int NOT NULL,
  `fdgd` int NOT NULL,
  `fdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jxjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jhlqsny` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jcjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spjtbz` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dah` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 59531 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wmcsj
-- ----------------------------
DROP TABLE IF EXISTS `wmcsj`;
CREATE TABLE `wmcsj`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `zwgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `jhljt` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jxjt` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `wmcsj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wmcsjb
-- ----------------------------
DROP TABLE IF EXISTS `wmcsjb`;
CREATE TABLE `wmcsjb`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzjb2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgzdc2` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `djc2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NOT NULL,
  `zwgzse2` int NOT NULL,
  `jsdjgz2` int NOT NULL,
  `jsfszwtg2` int NOT NULL,
  `fdgz2` int NOT NULL,
  `dfbt2` int NOT NULL,
  `blfb2` int NOT NULL,
  `jjjy2` int NOT NULL,
  `jhljt` int NOT NULL,
  `tgblbf` int NOT NULL,
  `jxjt` int NOT NULL,
  `wybt` int NOT NULL,
  `txbt` int NOT NULL,
  `wmj` int NOT NULL,
  `pskhj` int NOT NULL,
  `wmcsj` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wmj
-- ----------------------------
DROP TABLE IF EXISTS `wmj`;
CREATE TABLE `wmj`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NULL DEFAULT NULL,
  `zwgzse2` int NULL DEFAULT NULL,
  `jsdjgz2` int NULL DEFAULT NULL,
  `fdgz2` int NULL DEFAULT NULL,
  `dfbt2` int NULL DEFAULT NULL,
  `blfb2` int NULL DEFAULT NULL,
  `jjjy2` int NULL DEFAULT NULL,
  `jxjt` int NULL DEFAULT NULL,
  `jsfszwtg2` int NULL DEFAULT NULL,
  `jhljt` int NULL DEFAULT NULL,
  `tgblbf` int NULL DEFAULT NULL,
  `gwjt2` int NULL DEFAULT NULL,
  `nzj` int NULL DEFAULT NULL,
  `wybt` int NULL DEFAULT NULL,
  `txbt` int NULL DEFAULT NULL,
  `pskhj` int NULL DEFAULT NULL,
  `mbkhj` int NULL DEFAULT NULL,
  `pajsj` int NULL DEFAULT NULL,
  `wmcsj` int NULL DEFAULT NULL,
  `wmj` int NULL DEFAULT NULL,
  `wmjbz` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wmjb
-- ----------------------------
DROP TABLE IF EXISTS `wmjb`;
CREATE TABLE `wmjb`  (
  `id` int NOT NULL,
  `nd` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jbgzse2` int NULL DEFAULT NULL,
  `zwgzse2` int NULL DEFAULT NULL,
  `jsdjgz2` int NULL DEFAULT NULL,
  `fdgz2` int NULL DEFAULT NULL,
  `dfbt2` int NULL DEFAULT NULL,
  `blfb2` int NULL DEFAULT NULL,
  `jjjy2` int NULL DEFAULT NULL,
  `jxjt` int NULL DEFAULT NULL,
  `jsfszwtg2` int NULL DEFAULT NULL,
  `jhljt` int NULL DEFAULT NULL,
  `tgblbf` int NULL DEFAULT NULL,
  `gwjt2` int NULL DEFAULT NULL,
  `nzj` int NULL DEFAULT NULL,
  `wybt` int NULL DEFAULT NULL,
  `txbt` int NULL DEFAULT NULL,
  `pskhj` int NULL DEFAULT NULL,
  `mbkhj` int NULL DEFAULT NULL,
  `pajsj` int NULL DEFAULT NULL,
  `wmcsj` int NULL DEFAULT NULL,
  `wmj` int NULL DEFAULT NULL,
  `wmjbz` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for xldzbz
-- ----------------------------
DROP TABLE IF EXISTS `xldzbz`;
CREATE TABLE `xldzbz`  (
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xl` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gzdc` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gzjb` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  INDEX `bmxl`(`zwbm` ASC, `xl` ASC) USING BTREE,
  INDEX `zwbm`(`zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for xtcs
-- ----------------------------
DROP TABLE IF EXISTS `xtcs`;
CREATE TABLE `xtcs`  (
  `qydrstg` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgdcxlgl` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xsws` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jwbz` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgjjjy` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fdgz` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for yfje
-- ----------------------------
DROP TABLE IF EXISTS `yfje`;
CREATE TABLE `yfje`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zwgw` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `yfje` decimal(10, 0) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for yhgl
-- ----------------------------
DROP TABLE IF EXISTS `yhgl`;
CREATE TABLE `yhgl`  (
  `gzzt` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `yhdm` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ID` int NOT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for yjtj
-- ----------------------------
DROP TABLE IF EXISTS `yjtj`;
CREATE TABLE `yjtj`  (
  `yjsj` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `职务编码` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `职务岗位` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `人数` decimal(6, 0) NOT NULL,
  `工资总额` decimal(9, 1) NOT NULL,
  `人均工资` decimal(7, 1) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ys
-- ----------------------------
DROP TABLE IF EXISTS `ys`;
CREATE TABLE `ys`  (
  `dwbm` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grbm` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xb` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `jg` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rdsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwgw2` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cjgzny` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `txsj` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `swyy` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `swsj` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `swdd` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `azdd` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sfls` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rel1` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm1` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny1` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwzw1` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gz1` decimal(6, 0) NOT NULL,
  `hk1` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz1` decimal(4, 0) NOT NULL,
  `bz11` decimal(4, 0) NOT NULL,
  `rel2` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm2` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny2` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwzw2` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gz2` decimal(6, 0) NOT NULL,
  `hk2` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz2` decimal(4, 0) NOT NULL,
  `bz21` decimal(4, 0) NOT NULL,
  `rel3` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm3` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny3` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwzw3` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gz3` decimal(6, 0) NOT NULL,
  `hk3` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz3` decimal(4, 0) NOT NULL,
  `bz31` decimal(4, 0) NOT NULL,
  `rel4` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm4` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny4` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwzw4` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gz4` decimal(6, 0) NOT NULL,
  `hk4` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz4` decimal(4, 0) NOT NULL,
  `bz41` decimal(4, 0) NOT NULL,
  `rel5` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `xm5` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `csny5` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dwzw5` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gz5` decimal(6, 0) NOT NULL,
  `hk5` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bz5` decimal(4, 0) NOT NULL,
  `bz51` decimal(4, 0) NOT NULL,
  `gdsr` decimal(6, 0) NOT NULL,
  `zz` char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bh` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for zsbl
-- ----------------------------
DROP TABLE IF EXISTS `zsbl`;
CREATE TABLE `zsbl`  (
  `lb` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2` decimal(2, 0) NOT NULL,
  `a3` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4` decimal(2, 0) NOT NULL,
  `a5` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a6` decimal(2, 0) NOT NULL,
  `a7` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a8` decimal(2, 0) NOT NULL,
  `a9` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a10` decimal(2, 0) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for zsbl06
-- ----------------------------
DROP TABLE IF EXISTS `zsbl06`;
CREATE TABLE `zsbl06`  (
  `lb` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a2` decimal(2, 0) NOT NULL,
  `a3` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a4` decimal(2, 0) NOT NULL,
  `a5` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a6` decimal(2, 0) NOT NULL,
  `a7` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a8` decimal(2, 0) NOT NULL,
  `a9` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a10` decimal(2, 0) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for zwbm
-- ----------------------------
DROP TABLE IF EXISTS `zwbm`;
CREATE TABLE `zwbm`  (
  `mc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for zwgzbz
-- ----------------------------
DROP TABLE IF EXISTS `zwgzbz`;
CREATE TABLE `zwgzbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwbm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zwmc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `a1` decimal(4, 0) NOT NULL,
  `a2` decimal(4, 0) NOT NULL,
  `a3` decimal(4, 0) NOT NULL,
  `a4` decimal(4, 0) NOT NULL,
  `a5` decimal(4, 0) NOT NULL,
  `a6` decimal(4, 0) NOT NULL,
  `a7` decimal(4, 0) NOT NULL,
  `a8` decimal(4, 0) NOT NULL,
  `a9` decimal(4, 0) NOT NULL,
  `a10` decimal(4, 0) NOT NULL,
  `a11` decimal(4, 0) NOT NULL,
  `a12` decimal(4, 0) NOT NULL,
  `a13` decimal(4, 0) NOT NULL,
  `a14` decimal(4, 0) NOT NULL,
  `a15` decimal(4, 0) NOT NULL,
  `a16` decimal(4, 0) NOT NULL,
  `a17` decimal(4, 0) NOT NULL,
  `a18` decimal(4, 0) NOT NULL,
  INDEX `tbnd`(`tbnd` ASC) USING BTREE,
  INDEX `ndbm`(`tbnd` ASC, `zwbm` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for zzjbtbz
-- ----------------------------
DROP TABLE IF EXISTS `zzjbtbz`;
CREATE TABLE `zzjbtbz`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `blfb2` decimal(3, 0) NOT NULL,
  `zwjt` decimal(3, 0) NOT NULL,
  `dfbt2` decimal(4, 0) NOT NULL,
  `gwjt2` decimal(3, 0) NOT NULL,
  `zfbt` decimal(3, 0) NOT NULL,
  `jzmcbt` decimal(2, 1) NOT NULL,
  `sdbt` decimal(3, 0) NOT NULL,
  `sidbt` decimal(3, 0) NOT NULL,
  `qtbt` decimal(3, 0) NOT NULL,
  `nzgwsf` decimal(3, 0) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for zzjbtbz06
-- ----------------------------
DROP TABLE IF EXISTS `zzjbtbz06`;
CREATE TABLE `zzjbtbz06`  (
  `tbnd` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bm` char(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mc` char(26) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `czbm` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `blfb2` int NOT NULL,
  `zwjt` int NOT NULL,
  `dfbt2` int NOT NULL,
  `gwjt2` int NOT NULL,
  `zfbt` int NOT NULL,
  `jzmcbt` int NOT NULL,
  `sdbt` int NOT NULL,
  `sidbt` int NOT NULL,
  `qtbt` int NOT NULL,
  `nzgwsf` int NOT NULL,
  `jxlb` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
