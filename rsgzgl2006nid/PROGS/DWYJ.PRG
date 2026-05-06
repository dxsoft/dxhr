FUNCTION dwyj

PARAMETERS P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15&&xm,jslb,jsnf,jsyf,yzw,ydc,xzw,xdc,xzwgz,yjb,ydjc,xjb,xdjc,xjbgz,khjg

tcWjyj=""

DO case
CASE P2="正常职务"
    tcWjyj="建议 "+P1+"同志的职务工资从 "+P3+" 年 "+p4+" 月起，由  "+ALLTRIM(P5)+SPACE(1)+ALLTRIM(p6)+"  档晋升为  "+ALLTRIM(p7)+SPACE(1)+ALLTRIM(p8)+"  档，工资额 "+ALLTRIM(STR(P9))+" 元。"
    
CASE P2="提前越级"
    tcWjyj="建议 "+P1+"同志的职务工资从 "+P3+" 年 "+p4+" 月起，由  "+ALLTRIM(P5)+SPACE(1)+ALLTRIM(p6)+"  档晋升为  "+ALLTRIM(p7)+SPACE(1)+ALLTRIM(p8)+"  档，工资额 "+ALLTRIM(STR(P9))+" 元。"

CASE P2="职务变化"
    tcWjyj="建议 "+P1+"同志的职务工资由 "+ALLTRIM(P5)+SPACE(1)+ALLTRIM(P6)+" 档变动为 "+ALLTRIM(p7)+SPACE(1)+ALLTRIM(p8)+" 档，工资额 "+ALLTRIM(STR(P9))+" 元，"+;
    IIF(ISNULL(P12) or EMPTY(P12),"","级别工资确定为"+dtc(P12)+"级"+iif(P13>"0","倒"+dtc(alltrim(P13)),"")+"，工资额 "+ALLTRIM(STR(P14))+" 元。")+"从"+p3+"年"+p4+"月起执行。"

CASE P2="调入定资"
    tcWjyj="建议 "+P1+"同志的职务工资由 "+ALLTRIM(P5)+SPACE(1)+ALLTRIM(P6)+" 档变动为 "+ALLTRIM(p7)+SPACE(1)+ALLTRIM(p8)+" 档，工资额 "+ALLTRIM(STR(P9))+" 元，"+;
    IIF(ISNULL(P12) or EMPTY(P12),"","级别工资确定为"+dtc(P12)+"级"+iif(P13>"0","倒"+dtc(alltrim(P13)),"")+"，工资额 "+ALLTRIM(STR(P14))+" 元。")+"从"+p3+"年"+p4+"月起执行。"

CASE P2="转业定资"
    tcWjyj="建议 "+P1+"同志的职务工资确定为 "+ALLTRIM(p7)+SPACE(1)+ALLTRIM(p8)+" 档，工资额 "+ALLTRIM(STR(P9))+" 元，"+;
    IIF(ISNULL(P12) or EMPTY(P12),"","级别工资确定为"+dtc(P12)+"级"+iif(P13>"0","倒"+dtc(alltrim(P13)),"")+"，工资额 "+ALLTRIM(STR(P14))+" 元。")+"从"+p3+"年"+p4+"月起执行。"
    
CASE (p15='上年合格' OR p15='上年称职') AND P2="正常级别"
    tcWjyj="建议 "+P1+"同志的级别工资从 "+P3+" 年 "+P4+" 月起由 "+dtc(P10)+"级"+iif(P11>"0","倒"+dtc(alltrim(P11)),"")+" 晋升为 "+dtc(P12)+"级"+iif(P13>"0","倒";
    +dtc(alltrim(P13)),"")+"，工资额 "+ALLTRIM(STR(P14))+" 元。"

CASE p15="五年合格" AND P2="正常级别"
    tcWjyj="建议 "+P1+"同志的级别工资从 "+P3+" 年 "+P4+" 月起由 "+dtc(P10)+"级"+iif(P11>"0","倒"+dtc(alltrim(P11)),"")+" 晋升为 "+dtc(P12)+"级"+iif(P13>"0","倒";
    +dtc(alltrim(P13)),"")+"，工资额 "+ALLTRIM(STR(P14))+" 元。"

CASE p15="三年优秀" AND P2="正常级别"
    tcWjyj="建议 "+P1+"同志的级别工资从 "+P3+" 年 "+P4+" 月起由 "+dtc(P10)+"级"+iif(P11>"0","倒"+dtc(alltrim(P11)),"")+" 晋升为 "+dtc(P12)+"级"+iif(P13>"0","倒";
    +dtc(alltrim(P13)),"")+"，工资额 "+ALLTRIM(STR(P14))+" 元。"

ENDCASE

RETURN tcWjyj