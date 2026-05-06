PROCEDURE INDATA

capath=GETDIR()
IF EMPTY(capath)
    RETURN
ENDIF

OPEN DATABASE gzjsgl

IF USED("dndkh")
    USE IN ndkh
ENDIF

USE h+"\data\dndkh.dbf" IN 0 EXCLUSIVE
SELECT Dndkh
DELETE ALL
APPEND FROM capath+"\data\dndkh"
PACK
USE IN dndkh

IF USED("dryjbxx")
    USE IN dryjbxx
ENDIF
USE h+"\data\dryjbxx.dbf" IN 0 EXCLUSIVE
SELECT Dryjbxx
DELETE ALL
PACK
APPEND FROM capath+"\data\dryjbxx"
REPLACE ALL tbnd1 WITH "200307"
REPLACE ALL tbnd WITH "200307"
REPLACE ALL jtbl WITH ""
REPLACE ALL dwsx WITH "07" FOR gwfl="事业管理人员"
REPLACE ALL dwsx WITH "08" FOR gwfl="事业技术工人"
REPLACE ALL dwsx WITH "09" FOR gwfl="事业普通工人"
REPLACE ALL dwsx WITH "05" FOR gwfl="机关技术工人"
REPLACE ALL dwsx WITH "06" FOR gwfl="机关普通工人"
REPLACE ALL zjbm WITH chgzwbm(zjbm)&&行政编码、事业编码、试用见习
REPLACE ALL spdw WITH "人事局" FOR spdw="人事"
REPLACE ALL gwfl WITH "专业技术岗位" FOR dwsx>="10"
REPLACE ALL gwfl WITH "管理岗位" FOR dwsx="07"
REPLACE ALL gwfl WITH "技术工岗位" FOR dwsx="08"
REPLACE ALL gwfl WITH "普通工岗位" FOR dwsx="09"
*!*	REPLACE ALL dwsx WITH "10" FOR dwsx>="10"

REPLACE ALL zgxl WITH SUBSTR(chgxl(zgxl),3)
REPLACE ALL xlbm WITH LEFT(chgxl(zgxl),2)

IF USED("hisbase")
    USE IN hisbase
ENDIF
USE h+"\data\hisbase.dbf" IN 0 EXCLUSIVE
SELECT hisbase
DELETE ALL
PACK
USE IN hisbase

IF USED("dryzwbh")
    USE IN dryzwbh
ENDIF
USE h+"\data\dryzwbh.dbf" IN 0 EXCLUSIVE
SELECT Dryzwbh
DELETE ALL
APPEND FROM capath+"\data\dryzwbh"

REPLACE dryzwbh.zwbm WITH zwbm(dryzwbh.xzzw) FOR EMPTY(zwbm) IN dryzwbh
REPLACE zjbm WITH LEFT(zwbm,2)+SUBSTR(zjbm,3,2) IN dryzwbh

SELECT dryzwbh
REPLACE ALL zwbm WITH chgzwbm(zwbm) IN dryzwbh
REPLACE ALL zjbm WITH chgzwbm(zjbm) IN dryzwbh

REPLACE ALL zwjb WITH "一级职员正部" FOR LEFT(zjbm,2)="07" AND zwjb="正部级"
REPLACE ALL zwjb WITH "一级职员副部" FOR LEFT(zjbm,2)="07" AND zwjb="副部级"
REPLACE ALL zwjb WITH "二级职员正厅" FOR LEFT(zjbm,2)="07" AND zwjb="正厅级"
REPLACE ALL zwjb WITH "二级职员副厅" FOR LEFT(zjbm,2)="07" AND zwjb="副厅级"
REPLACE ALL zwjb WITH "三级职员正处" FOR LEFT(zjbm,2)="07" AND zwjb="正处级"
REPLACE ALL zwjb WITH "三级职员副处" FOR LEFT(zjbm,2)="07" AND zwjb="副处级"
REPLACE ALL zwjb WITH "四级职员正科" FOR LEFT(zjbm,2)="07" AND zwjb="正科级"
REPLACE ALL zwjb WITH "四级职员副科" FOR LEFT(zjbm,2)="07" AND zwjb="副科级"
REPLACE ALL zwjb WITH "五级职员科员" FOR LEFT(zjbm,2)="07" AND zwjb="科员"
REPLACE ALL zwjb WITH "六级职员办事员" FOR LEFT(zjbm,2)="07" AND zwjb="办事员"

REPLACE ALL zjbm WITH "0150" FOR zwjb="正厅局级领导职务"
REPLACE ALL zjbm WITH "0151" FOR zwjb="正厅局级非领导职务"
REPLACE ALL zjbm WITH "0160" FOR zwjb="副厅局级领导职务"
REPLACE ALL zjbm WITH "0161" FOR zwjb="副厅局级非领导职务"
REPLACE ALL zjbm WITH "0170" FOR zwjb="正县处级领导职务"
REPLACE ALL zjbm WITH "0171" FOR zwjb="正县处级非领导职务"
REPLACE ALL zjbm WITH "0180" FOR zwjb="副县处级领导职务"
REPLACE ALL zjbm WITH "0181" FOR zwjb="副县处级非领导职务"
REPLACE ALL zjbm WITH "0190" FOR zwjb="正科级领导职务"
REPLACE ALL zjbm WITH "0191" FOR zwjb="正科级非领导职务"
REPLACE ALL zjbm WITH "01A0" FOR zwjb="副科级领导职务"
REPLACE ALL zjbm WITH "01A1" FOR zwjb="副科级非领导职务"
REPLACE ALL zjbm WITH "01B0" FOR zwjb="科员"
REPLACE ALL zjbm WITH "01C0" FOR zwjb="办事员"

REPLACE ALL zwbm WITH "0150" FOR xzzw="正厅局级"
REPLACE ALL zwbm WITH "0160" FOR xzzw="副厅局级"
REPLACE ALL zwbm WITH "0170" FOR xzzw="正县处级"
REPLACE ALL zwbm WITH "0180" FOR xzzw="副县处级"
REPLACE ALL zwbm WITH "0190" FOR xzzw="正科级"
REPLACE ALL zwbm WITH "01A0" FOR xzzw="副科级"
REPLACE ALL zwbm WITH "01B0" FOR xzzw="科员"
REPLACE ALL zwbm WITH "01C0" FOR xzzw="办事员"
DELETE ALL FOR EMPTY(zjbm) AND EMPTY(zwbm) AND EMPTY(zwjb) AND EMPTY(xzzw)

REPLACE ALL zwjb WITH "机关高级技师" FOR zwjb="高级技师" AND LEFT(zjbm,2)="05"
REPLACE ALL zwjb WITH "机关技师" FOR zwjb="技师" AND LEFT(zjbm,2)="05"
REPLACE ALL zwjb WITH "机关高级工" FOR zwjb="高级工" AND LEFT(zjbm,2)="05"
REPLACE ALL zwjb WITH "机关中级工" FOR zwjb="中级工" AND LEFT(zjbm,2)="05"
REPLACE ALL zwjb WITH "机关初级工" FOR zwjb="初级工" AND LEFT(zjbm,2)="05"
REPLACE ALL zwjb WITH "机关普工" FOR zwjb="普工" AND LEFT(zjbm,2)="06"

REPLACE ALL zwjb WITH "事业高级技师" FOR zwjb="高级技师" AND LEFT(zjbm,2)="08"
REPLACE ALL zwjb WITH "事业技师" FOR zwjb="技师" AND LEFT(zjbm,2)="08"
REPLACE ALL zwjb WITH "事业高级工" FOR zwjb="高级工" AND LEFT(zjbm,2)="08"
REPLACE ALL zwjb WITH "事业中级工" FOR zwjb="中级工" AND LEFT(zjbm,2)="08"
REPLACE ALL zwjb WITH "事业初级工" FOR zwjb="初级工" AND LEFT(zjbm,2)="08"
REPLACE ALL zwjb WITH "事业普工" FOR zwjb="普工" AND LEFT(zjbm,2)="09"

PACK

USE IN dryjbxx
USE IN dryzwbh

IF USED("jdzw")
    USE IN jdzw
ENDIF
USE h+"\data\jdzw.dbf" IN 0 EXCLUSIVE
SELECT jdzw
DELETE ALL
APPEND FROM capath+"\data\jdzw"

REPLACE ALL zjbm WITH "0130" FOR zwjb="按正军职" OR zwjb="四" OR zwjb="正军职"
REPLACE ALL zjbm WITH "0140" FOR zwjb="按副军职" OR zwjb="五" OR zwjb="副军职"
REPLACE ALL zjbm WITH "0150" FOR zwjb="正局" OR zwjb="六" OR zwjb="正师职"
REPLACE ALL zjbm WITH "0160" FOR zwjb="副局" OR zwjb="七" OR zwjb="副师职"
REPLACE ALL zjbm WITH "0170" FOR zwjb="正处" OR zwjb="八" OR zwjb="正团职"
REPLACE ALL zjbm WITH "0180" FOR zwjb="副处" OR zwjb="九" OR zwjb="副团职"
REPLACE ALL zjbm WITH "0190" FOR zwjb="正科" OR zwjb="十" OR zwjb="正营职"
REPLACE ALL zjbm WITH "01A0" FOR zwjb="副科" OR zwjb="十一" OR zwjb="副营职"
REPLACE ALL zjbm WITH "01B0" FOR zwjb="一级科员" OR zwjb="二级科员" OR zwjb="十三" OR zwjb="副连职" OR zwjb="十二" OR zwjb="正连职"
REPLACE ALL zjbm WITH "01C0" FOR zwjb="办事员" OR zwjb="十四" OR zwjb="排职"

DELETE ALL FOR EMPTY(zjbm) AND EMPTY(zwjb)

PACK

USE IN jdzw

IF USED("dwbm")
    USE IN dwbm
ENDIF
USE h+"\data\dwbm.dbf" IN 0 EXCLUSIVE
SELECT Dwbm
DELETE ALL
PACK
APPEND FROM capath+"\data\dwbm"
USE IN dwbm

IF USED("dxl")
    USE IN dxl
ENDIF
USE h+"\data\dxl.dbf" IN 0 EXCLUSIVE
SELECT Dxl
DELETE ALL
PACK
APPEND FROM capath+"\data\dxl"
REPLACE ALL xl WITH SUBSTR(chgxl(xl),3) IN dxl
REPLACE ALL xlbm WITH LEFT(chgxl(xl),2) IN dxl
USE IN dxl

IF USED("hjxx")
    USE IN hjxx
ENDIF
USE h+"\data\hjxx.dbf" IN 0 EXCLUSIVE
SELECT Hjxx
DELETE ALL
PACK
APPEND FROM capath+"\data\hjxx"
USE IN hjxx

IF USED("jx")
    USE IN jx
ENDIF
USE h+"\data\jx.dbf" IN 0 EXCLUSIVE
SELECT Jx
APPEND FROM capath+"\data\jx"
USE IN jx

IF USED("tgqgz2006")
    USE IN tgqgz2006
ENDIF
USE h+"\data\tgqgz2006.dbf" IN 0 EXCLUSIVE
SELECT Tgqgz2006
DELETE ALL
PACK
APPEND FROM capath+"\data\dryjbxx"
REPLACE ALL tbnd WITH "200307"
REPLACE ALL jt2 WITH 0 FOR dwsx="01"

USE IN tgqgz2006

CLOSE DATABASES
