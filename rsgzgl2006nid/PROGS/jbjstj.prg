FUNCTION jbjstj

PARAMETERS tcdwbm,tcgrbm,tcxckhndjb,tcjsnf

IF tcjsnf<="2006"
    RETURN ""
ENDIF

&&要考虑去除重复考核结果
SELECT DISTINCT khnd ;
FROM ndkh ;
WHERE dwbm+grbm=tcdwbm+tcgrbm AND BETWEEN(khnd,tcxckhndjb,ALLTRIM(STR(VAL(tcjsnf)-1))) AND INLIST(khjg,"优秀","称职","合格") ;
 INTO ARRAY lat
IF _tally>=5
    RETURN '五年称职'
ELSE
    RETURN ""
ENDIF
