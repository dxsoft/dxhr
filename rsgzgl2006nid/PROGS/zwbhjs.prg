FUNCTION zwbhjs

PARAMETERS tcyzwbm,tcyZwgzdc,tcZwbm,tcSrny

IF tcZwbm='01B1'
	tcZwbm='01B0'
ENDIF

v_j=""
m.xckhndzw="9999"

v_jsyf=ALLTRIM(STR(val(subs(tcSrny,6,2))+1)) &&计算晋升时间
v_jsyf=right('0'+v_jsyf,2)
v_jsnf=left(tcSrny,4)

IF v_jsyf>'12'
   v_jsyf='01'
   v_jsnf=allt(str(val(v_jsnf)+1))
ENDIF

tcTbnd=tbnd(v_jsnf+v_jsyf,"zwgzbz")

v_ygzse=zwgzse(tcyZwbm,tcyZwgzdc,tcTbnd)
v_xzwgz=zwgzse(tcyZwbm,ALLTRIM(STR(VAL(tcyZwgzdc)+1)),tcTbnd)
IF v_xzwgz=0&&已达最高档
    v_yce=zwgzse(tcyZwbm,tcyZwgzdc,tcTbnd)-zwgzse(tcyZwbm,ALLTRIM(STR(VAL(tcyZwgzdc)-1)),tcTbnd)
ELSE
    v_yce=v_xzwgz-zwgzse(tcyZwbm,tcyZwgzdc,tcTbnd)&&原档差
ENDIF

FOR i=1 TO 16
    v_j=ALLTRIM(STR(i))
    v_xgzse=zwgzse(tcZwbm,v_j,tcTbnd)
    IF cyxx.zwbhhjsdj='√'
        IF (v_xgzse+jsdjgz(tcTbnd,tcZwbm))>(v_ygzse+jsdjgz(tcTbnd,tcyZwbm))
	        v_zze=(v_xgzse+jsdjgz(tcTbnd,tcZwbm))-(v_ygzse+jsdjgz(tcTbnd,tcyZwbm))&&职务工资+技术等级工资
	        IF v_zze>v_yce&&增资额超过原档差
	            IF RIGHT(tcSrny,2)>='10' and cyxx.zwbh10='√' 
	                m.xckhndzw=allt(str(val(LEFT(tcSrny,4))+1))&&下一年
	            ELSE
	                m.xckhndzw=v_jsnf&&晋升年份
	            ENDIF
	        ENDIF
	        EXIT
        ENDIF
    ELSE
        IF v_xgzse>v_ygzse
	        v_zze=(v_xgzse+jsdjgz(tcTbnd,tcZwbm))-(v_ygzse+jsdjgz(tcTbnd,tcyZwbm))&&职务工资+技术等级工资
	        IF v_zze>v_yce&&增资额超过原档差
	            IF RIGHT(tcSrny,2)>='10' and cyxx.zwbh10='√' 
	                m.xckhndzw=allt(str(val(LEFT(tcSrny,4))+1))&&下一年
	            ELSE
	                m.xckhndzw=v_jsnf&&晋升年份
	            ENDIF
	        ENDIF
	        EXIT
        ENDIF
    ENDIF
ENDFOR

RETURN m.xckhndzw+v_j