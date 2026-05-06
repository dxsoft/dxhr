FUNCTION tg_jy_zw

PARAMETERS ttgnx,trznx,tzwbm,tdwbm,tgrbm,tjsny,tzjbm,tsfjzgb

&&职务级别低于工资职务（军转干部，研究生？）：按工资职务套最低（根据分析，按职务级别最高也只能套到工资职务的最低，按政策未达最低到最低）；职务级别等于工资职务：按工资职务套。
DO case
CASE tzwbm="0171"&&正处->一级警长
    RETURN "0201"
CASE tzwbm="0181"&&副处->二级警长
    RETURN "0202"
CASE tzwbm="0191"&&正科->三级警长
*!*	    IF tzjbm>tzwbm
*!*	        IF tsfjzgb="是" AND !EMPTY(tsfjzgb)
            RETURN "0203"
*!*	        ELSE
*!*		        DO case
*!*		        CASE tzjbm="01A1"
*!*				    IF ttgnx>=25 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
*!*				        RETURN "0203"
*!*				    ELSE
*!*				        IF ttgnx>=10 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
*!*				            RETURN "0204"
*!*				        ELSE
*!*				            RETURN "0205"
*!*				        ENDIF
*!*				    ENDIF
*!*				CASE tzjbm="01B0"
*!*				    IF ttgnx>=20 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
*!*				        RETURN "0205"
*!*				    ELSE
*!*				        RETURN "0206"
*!*				    ENDIF
*!*		        OTHERWISE
*!*		            RETURN ""
*!*		        ENDCASE
*!*		    ENDIF
*!*	    ELSE
*!*	        RETURN "0203"
*!*	    ENDIF

CASE tzwbm="01A1"&&副科—>三级警长、四级警长、一级警员
*!*	    IF tzjbm>tzwbm
*!*	        IF tsfjzgb="是" AND !EMPTY(tsfjzgb)
*!*	            RETURN "0205"
*!*	        ELSE
*!*		        DO case
*!*		        CASE tzjbm="01B0"
*!*		            IF ttgnx>=20 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
*!*				        RETURN "0205"
*!*				    ELSE
*!*				        RETURN "0206"
*!*				    ENDIF
*!*		        CASE tzjbm="01C0"
*!*				    IF ttgnx>15 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
*!*				        RETURN "0206"
*!*				    ELSE
*!*				        RETURN "0207"
*!*				    ENDIF
*!*				OTHERWISE
*!*				    RETURN ""
*!*				ENDCASE
*!*			ENDIF
*!*		ELSE
        IF tsfjzgb="是" AND !EMPTY(tsfjzgb)
            RETURN "0205"
        ELSE
	        IF ttgnx>=25 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
		        RETURN "0203"
		    ELSE
		        IF ttgnx>=10 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
		            RETURN "0204"
		        ELSE
		            RETURN "0205"
		        ENDIF
		    ENDIF
		ENDIF
*!*	    ENDIF

CASE tzwbm="01B0"
    IF tzjbm>tzwbm
        IF tsfjzgb="是" AND !EMPTY(tsfjzgb)
            RETURN "0206"
        ELSE
		    IF ttgnx>=20 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
		        RETURN "0205"
		    ELSE
		        RETURN "0206"
		    ENDIF
		ENDIF
    ELSE
	    IF ttgnx>=20 AND trznx>=3 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
	        RETURN "0205"
	    ELSE
	        RETURN "0206"
	    ENDIF
	ENDIF
	
CASE tzwbm="01C0"
    IF ttgnx>15 AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-3,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-2,4)),"优秀","称职","合格") AND INLIST(khjg(tdwbm+tgrbm,STR(VAL(LEFT(tjsny,4))-1,4)),"优秀","称职","合格")
        RETURN "0206"
    ELSE
        RETURN "0207"
    ENDIF
CASE tzwbm="01FF"
    RETURN "02FF"
OTHERWISE
    RETURN ""
ENDCASE
