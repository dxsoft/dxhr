FUNCTION pushgz

RETURN

SELECT ryjbxx &&½«»¨Ãû²áÓÒ±ß×óÒÆ

SELECT ALLTRIM(field_name) FROM fldgz INTO ARRAY laFldName1
v_reccnt=_tally
FOR i=1 TO v_reccnt
    lcFldname=ALLTRIM(STRTRAN(laFldName1[i,1],"2",""))+"1"
    REPLACE ryjbxx. &lcFldname WITH ryjbxx. &laFldName1[i,1] IN ryjbxx
ENDFOR
RELEASE laFldname1
REPLACE ryjbxx.zwbm1 WITH ryjbxx.zwbm2 IN ryjbxx
REPLACE ryjbxx.tgbl1 WITH ryjbxx.tgbl IN ryjbxx

RETURN 0