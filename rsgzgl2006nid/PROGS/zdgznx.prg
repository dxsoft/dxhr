FUNCTION zdgznx

PARAMETERS tcdwbm,tcgrbm,tccjgzny,tcny

SELECT SUM(1) FROM ndkh WHERE dwbm=tcdwbm and grbm=tcgrbm AND khjg="Î´¿¼ºË(ÖÐ¶ÏÄêÏÞ)" AND BETWEEN(khnd,tccjgzny,tcny) INTO ARRAY las
IF ISNULL(las[1])
    RETURN 0
ELSE
	RETURN las[1]
ENDIF