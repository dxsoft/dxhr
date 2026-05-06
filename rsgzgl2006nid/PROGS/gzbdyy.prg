FUNCTION gzbdyy

PARAMETERS tcjslb,tcdwsx

DO case
CASE tcjslb="正常档次" AND tcdwsx>="07" AND tcdwsx<="10"
    RETURN "正常增加薪级工资"
    
CASE tcjslb="正常档次" AND tcdwsx="03"
    RETURN "员额晋档"

CASE tcjslb="正常档次" AND (tcdwsx<"07" OR tcdwsx>"20")
    RETURN "按年度考核结果晋升级别工资档次"

*!*	CASE tcjslb="职务变化" AND tcdwsx="03"
*!*	    RETURN "等级晋升工资变动"

CASE tcjslb="职务变化"
    RETURN "职务变化工资变动"

*!*	CASE tcjslb="职级套改"
*!*	    RETURN "职级套改工资变动"

*!*	CASE tcjslb="职级晋升"
*!*	    RETURN "职级晋升工资变动"

CASE tcjslb="警衔变化"
    RETURN "警衔变化工资变动"

CASE tcjslb="级别滚动"
    RETURN "级别滚动晋升"

CASE tcjslb="正常级别"
    RETURN "正常级别晋升"

CASE tcjslb="提前越级"
    RETURN "提前越级晋升"
    
CASE tcjslb="法检晋档"
    RETURN "法检晋档"

CASE tcjslb="警务套改" AND tcdwsx='21'
	RETURN "勤务警员套改"
	
CASE tcjslb="警务套改" AND tcdwsx='22'
	RETURN "警务技术套改"

OTHERWISE 
    RETURN tcjslb
ENDCASE
