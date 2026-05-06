FUNCTION rzsj
PARAMETERS tcdwbm,tcgrbm,tczwbm

*!*	SELECT * FROM ryzwbh INTO TABLE c:\aa

SELECT TOP 1 srny FROM ryzwbh WHERE dwbm=ALLTRIM(tcdwbm) AND grbm=tcgrbm AND zjbm=tczwbm ORDER BY srny INTO ARRAY latsrny

IF _tally>0
    RETURN LEFT(STRTRAN(latsrny[1],"."),4)+"."+RIGHT(ALLTRIM(STRTRAN(latsrny[1],".")),2)
ELSE
    RETURN "       "
ENDIF
