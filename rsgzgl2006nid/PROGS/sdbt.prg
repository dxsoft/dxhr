FUNCTION sdbt

PARAMETERS tcdwbm,tczwbm,tcbz

LOCAL  latdfbt
DIMENSION latdfbt[1]

IF AT("F",tczwbm)>0&&试用期无工作性津贴
    RETURN 0000
ENDIF

*!*	IF LEFT(tczwbm,2)="03"
*!*	    tczwbm="01"+STRTRAN(STRTRAN(STRTRAN(SUBSTR(tczwbm,4,1),"B","A"),"C","B"),"D","B")+"0"
*!*	ENDIF

DO case
CASE tczwbm='0427'
    tczwbm='2707'
CASE tczwbm='0428'
    tczwbm='2708'
CASE tczwbm='0429'
    tczwbm='2709'
CASE tczwbm='042A'
    tczwbm='2710'
CASE tczwbm='042B'
    tczwbm='2711'

CASE tczwbm='0417'
    tczwbm='2607'
CASE tczwbm='0418'
    tczwbm='2608'
CASE tczwbm='0419'
    tczwbm='2609'
CASE tczwbm='041A'
    tczwbm='2610'
CASE tczwbm='041B'
    tczwbm='2611'

CASE tczwbm='0437'
    tczwbm='2807'
CASE tczwbm='0438'
    tczwbm='2808'
CASE tczwbm='0439'
    tczwbm='2809'
CASE tczwbm='043A'
    tczwbm='2810'
CASE tczwbm='043B'
    tczwbm='2811'

ENDCASE


*!*	SELECT bz FROM bz06_jbt WHERE UPPER(item)=="SDBT" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=1 INTO ARRAY latdfbt

SELECT bz FROM bz06_jbt WHERE UPPER(item)=="SDBT" AND zwbm=tczwbm AND tbnd=tcbz INTO ARRAY latdfbt
		
IF _tally>0
    m.lnBz=latdfbt[1]
ELSE
    m.lnBz=0000
ENDIF

	
RETURN m.lnBz
