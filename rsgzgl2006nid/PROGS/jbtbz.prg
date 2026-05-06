FUNCTION jbtbz

PARAMETERS cdwbm,cny

LOCAL ajxlb,ajbtbz

DIMENSION ajxlb[1],ajbtbz[1]
 
*!*	IF cny>'201607'
	select jxlb from dwbm where dwbm=cdwbm INTO ARRAY ajxlb
	m.jxlb=ajxlb[1]
    IF m.jxlb=2
		SELECT distinct tbnd FROM bz06_jbt where item='DFBT2' and jxlb=5 AND tbnd<=cny ORDER BY tbnd DESC INTO ARRAY ajbtbz
    ELSE
		SELECT distinct tbnd FROM bz06_jbt where item='DFBT2' and jxlb=m.jxlb AND tbnd<=cny ORDER BY tbnd DESC INTO ARRAY ajbtbz    
    ENDIF
*!*	ELSE
*!*		SELECT distinct tbnd FROM bz06_jbt where item='DFBT2' AND tbnd<=cny ORDER BY tbnd DESC INTO ARRAY ajbtbz
*!*	ENDIF
IF _tally>0 AND !ISNULL(ajbtbz[1])
	RETURN ajbtbz[1]
ELSE
    RETURN "      "
ENDIF