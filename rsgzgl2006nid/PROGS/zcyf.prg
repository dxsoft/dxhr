&&计算绩效工资
FUNCTION zcyf

PARAMETERS cZwbm,cTbnd,njxlb

IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 0000
ENDIF

IF EMPTY(cZwbm)
    RETURN 0000
ENDIF

oldalias=SELECT()
SELECT czyf

LOCATE FOR tbnd=ALLTRIM(cTbnd) AND ALLTRIM(bm)=ALLTRIM(cZwbm) AND jxlb=njxlb

SELECT (oldalias)

IF FOUND("czyf")
    RETURN czyf.dfbt2
ELSE
    RETURN 0000
ENDIF
