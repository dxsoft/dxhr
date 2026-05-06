FUNCTION jbmc
PARAMETERS tcjb

DO CASE
CASE VAL(tcjb)=1
    RETURN "公务员一级    "
CASE VAL(tcjb)=2
    RETURN "公务员二级    "
CASE VAL(tcjb)=3
    RETURN "公务员三级    "
CASE VAL(tcjb)=4
    RETURN "公务员四级    "
CASE VAL(tcjb)=5
    RETURN "公务员五级    "
CASE VAL(tcjb)=6
    RETURN "公务员六级    "
CASE VAL(tcjb)=7
    RETURN "公务员七级    "
CASE VAL(tcjb)=8
    RETURN "公务员八级    "
CASE VAL(tcjb)=9
    RETURN "公务员九级    "
CASE VAL(tcjb)=10
    RETURN "公务员十级    "
CASE VAL(tcjb)=11
    RETURN "公务员十一级  "
CASE VAL(tcjb)=12
    RETURN "公务员十二级  "
CASE VAL(tcjb)=13
    RETURN "公务员十三级  "
CASE VAL(tcjb)=14
    RETURN "公务员十四级  "
CASE VAL(tcjb)=15
    RETURN "公务员十五级  "
CASE VAL(tcjb)=16
    RETURN "公务员十六级  "
CASE VAL(tcjb)=17
    RETURN "公务员十七级  "
CASE VAL(tcjb)=18
    RETURN "公务员十八级  "
CASE VAL(tcjb)=19
    RETURN "公务员十九级  "
CASE VAL(tcjb)=20
    RETURN "公务员二十级  "
CASE VAL(tcjb)=21
    RETURN "公务员二十一级"
CASE VAL(tcjb)=22
    RETURN "公务员二十二级"
CASE VAL(tcjb)=23
    RETURN "公务员二十三级"
CASE VAL(tcjb)=24
    RETURN "公务员二十四级"
CASE VAL(tcjb)=25
    RETURN "公务员二十五级"
CASE VAL(tcjb)=26
    RETURN "公务员二十六级"
CASE VAL(tcjb)=27
    RETURN "公务员二十七级"
OTHERWISE 
    RETURN "              "
ENDCASE