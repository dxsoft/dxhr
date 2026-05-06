FUNCTION zwzj

PARAMETERS zwbm

LOCAL retv

retv=SPACE(30)

DO case
CASE zwbm="01C0"
    retv = "二级科员及相当职级层次"
CASE zwbm="01B0"
    retv = "一级科员及相当职级层次"
CASE zwbm="01A1"
    retv = "四级主任科员及相当层次职级"
CASE zwbm="01A0"
    retv = "乡科级副职"
CASE zwbm="0191"
    retv = "二级主任科员及相当层次职级"
CASE zwbm="0190"
    retv = "乡科级正职"
CASE zwbm="0181"
    retv = "四级调研员及相当层次职级"
CASE zwbm="0180"
    retv = "县处级副职"
CASE zwbm="0171"
    retv = "二级调研员及相当层次职级"
CASE zwbm="0170"
    retv = "县处级正职"
CASE zwbm="0161"
    retv = "二级巡视员及相当层次职级"
CASE zwbm="0160"
    retv = "厅局级副职"
CASE zwbm="0151"
    retv = "一级巡视员及相当层次职级"
CASE zwbm="0150"
    retv = "厅局级正职"
CASE zwbm="0207"
    retv = "二级科员及相当职级层次"
CASE zwbm="0206"
    retv = "一级科员及相当职级层次"
CASE zwbm="0205"
    retv = "四级主任科员及相当层次职级"
CASE zwbm="0204"&&四级警长
    retv = "二级主任科员及相当层次职级"
CASE zwbm="0203"&&三级警长
    retv = "二级主任科员及相当层次职级"
CASE zwbm="0202"
    retv = "四级调研员及相当层次职级"
CASE zwbm="0201"
    retv = "二级调研员及相当层次职级"
OTHERWISE
    retv = "                          "
ENDCASE

RETURN retv