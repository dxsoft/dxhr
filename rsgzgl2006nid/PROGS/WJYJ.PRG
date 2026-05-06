FUNCTION wjyj

PARAMETERS P1,P2,P3

tcWjyj=""

DO case
CASE P2="正常职务"
    tcWjyj="根据"+ALLTRIM(p3)+"文件规定，正常晋升职务工资。"

CASE P2="职务变化"
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，职务晋升变动工资。"

CASE P2="提前越级"
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，提前晋升职务工资。"

CASE P2="调入定资"
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，正常变动工资。"

CASE P2="转业定资"
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，确定军队转业干部工资。"

CASE P2="提前越级"
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，奖励晋升职务工资。"

CASE p1='上年合格'
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，工作年限达到上一级别对应工作年限，晋升级别工资。"

CASE p1="五年合格"
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，正常晋升级别工资。"

CASE p1="三年优秀"
    tcWjyj="根据"+ALLTRIM(P3)+"文件规定，年度考核连续三年优秀，提前晋升级别工资。"

ENDCASE

RETURN tcWjyj