FUNCTION tgszw

PARAMETERS tcdwgrbm

SELECT zwmc,rzsj,ALLTRIM(STR(rznx)),zwmc1,rzsj1 ,ALLTRIM(STR(rznx1)),ALLTRIM(STR(kjnx)),zwkjnx,zwkjnx1 FROM tgxx WHERE dwbm+grbm=tcdwgrbm INTO ARRAY lat

IF _tally>0
    RETURN lat[1]+","+lat[2]+","+lat[3]+","+lat[4]+","+lat[5]+","+lat[6]+","+lat[7]+","+ALLTRIM(STR(lat[8]))+","+ALLTRIM(STR(lat[9]))+","
ELSE
    RETURN ""
ENDIF
