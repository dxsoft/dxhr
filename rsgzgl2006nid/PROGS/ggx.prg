FUNCTION ggx

PARAMETERS czwbm,cate

LOCAL aa
DIMENSION aa[1]

IF AT("F",czwbm)>0
    csql="SELECT "+cate+" FROM ggx WHERE LEFT(zwbm,3)='"+LEFT(czwbm,3)+"' INTO ARRAY aa"
ELSE
    csql="SELECT "+cate+" FROM ggx WHERE zwbm='"+czwbm+"' INTO ARRAY aa"
ENDIF
&csql

IF _tally>0
    RETURN aa[1]
ELSE
    RETURN 0000
ENDIF