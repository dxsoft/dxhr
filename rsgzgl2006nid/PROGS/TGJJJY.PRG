FUNCTION tgjjjy

PARAMETERS tcZwbm,tcCjgzny,tcZdgznx

v_gznx93=iif(xtcs.tgjjjy,1994,1993)-val(left(tcCjgzny,4))+1
IF v_gznx93 > tcZdgznx+1
    v_gznx93 = v_gznx93 - tcZdgznx
ENDIF

IF v_gznx93<1
    v_gznx93=1
ENDIF

DO case
CASE LEFT(tcZwbm,2)='01'

CASE LEFT(tcZwbm,2)='04'
    tcZwbm='01'+RIGHT(tcZwbm,2)
    
CASE LEFT(tcZwbm,2)='02' OR LEFT(tcZwbm,2)='03' OR LEFT(tcZwbm,2)='06' OR LEFT(tcZwbm,2)='07'
    tcZwbm='9999'

OTHERWISE
    tcZwbm='01'+RIGHT(ALLTRIM(STR(VAL(RIGHT(tcZwbm,2))*2+106)),2)

ENDCASE

IF tcZwbm='9999'
    i=IIF(INT((v_gznx+5)/10)+1<5,INT((v_gznx+5)/10)+1,5)

ELSE
    i=IIF(INT(v_gznx/10)+1<4,INT(v_gznx/10)+1,4)

ENDIF

SELECT tgjjjy
LOCATE FOR zwbm=tcZwbm &&ÐÐ¶¨Î»

fldname='a'+ALLTRIM(STR(i,1))
RETURN tgjjjy.&fldname 
