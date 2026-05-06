FUNCTION srny1
PARAMETERS tcdwbm,tcgrbm,tczwbm

SELECT TOP 1 srny FROM ryzwbh WHERE dwbm=ALLTRIM(tcdwbm) AND grbm=tcgrbm AND zwbm=tczwbm ORDER BY srny INTO ARRAY latsrny

IF _tally>0
    RETURN LEFT(STRTRAN(latsrny[1],"."),4)+"."+RIGHT(ALLTRIM(STRTRAN(latsrny[1],".")),2)
ELSE
    RETURN "       "
ENDIF
