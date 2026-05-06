FUNCTION ndkhjsdc

PARAMETERS tcdwbm,tcgrbm,tckhnd,curdate

IF tckhnd+2<curdate
    RETURN 0
ELSE
    SEEK curdata+dwbm+grbm ORDER ndbm IN ndkh
    IF FOUND('ndkh')
        khjg2=ndkh.khjg
    ELSE
        RETURN 0
    ENDIF

    SEEK ALLTRIM(STR(VAL(curdata)-1))+dwbm+grbm ORDER ndbm IN ndkh
    IF FOUND('ndkh')
        khjg1=ndkh.khjg
    ELSE
        RETURN 0
    ENDIF
    
    IF khjg1<>"不合格" AND khjg1<>'未定等次' AND khjg2<>'不合格' AND khjg2<>'未定等次' AND khjg1<>'不称职' AND khjg2<>'不称职'
        RETURN 1
    ELSE
        RETURN 0
    ENDIF
ENDIF
