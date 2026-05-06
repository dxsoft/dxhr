FUNCTION gljbjs

PARAMETERS tcZwbm,tcGznx,tcJb,tcDjc

IF MOD(tnGznx,5) = 0 AND tnGznx>=10

	SELECT jbtgbz

	LOCATE FOR zwbm=tcZwbm

	i = INT(tnGznx/5)
	fldname="a"+ALLTRIM(STR(i,2,0))

    IF SUBSTR(jbtgbz.&fldname,2,1)='d'
        m.jb=SUBSTR(jbtgbz.&fldname,1,1)
        m.djc=SUBSTR(jbtgbz.&fldname,3,1)
    ELSE
        m.jb=jbtgbz.&fldname
        m.djc=0
    ENDIF

    SELECT jbbz
    LOCATE FOR zwbm=tcZwbm
    m.zgjb=jbbz.zgjb
    
    IF m.jb-m.djc<tcJb-tcDjc
    	RETURN IIF(m.jb<=m.zgjb,m.zgjb+ALLTRIM(STR(m.zgjb-m.jb)),m.jb+'0')
    ELSE
        RETURN 0
    ENDIF
    
ELSE
    RETURN 0
ENDIF
