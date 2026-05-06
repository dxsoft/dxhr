FUNCTION pushhis

LPARAMETERS tcjsnf,tcjsyf,tcjslb

oGUID = CreateObject("scriptlet.typelib")
cGUID = substr( oGUID.GUID, 2, 36 )
	    
SELECT hisbase
SEEK ryjbxx.dwbm+ryjbxx.grbm ORDER tag xbm

IF FOUND("hisbase")
	SCATTER MEMVAR
	REPLACE sid WITH cguid

	m.jslb=tcjslb
	m.jsnf=tcjsnf
	m.jsyf=tcjsyf
	m.id = cguid
    m.tfnf = ALLTRIM(STR(m.pnyear))
    m.tfyf = PADL(ALLTRIM(STR(m.pnmonth)),2,'0')
    m.bgdwjc = PADL(ALLTRIM(STR(m.pnday)),2,'0')
    m.denkh = m.czy

	INSERT INTO hisbase FROM MEMVAR
ELSE
    AFIELDS(afld,"hisbase")
	FOR i=1 TO ALEN(afld,1)
	    IF afld[i,2]="N" OR afld[i,2]="I" OR afld[i,2]="B"
	        m. &afld[i,1] = 0
	    ELSE
	        m. &afld[i,1] = ''
	    ENDIF
	ENDFOR	
    SELECT ryjbxx
    SCATTER MEMVAR
    m.id = cguid
    m.sid=''
	m.jslb=tcjslb
	m.jsnf=tcjsnf
	m.jsyf=tcjsyf    
    m.tfnf = ALLTRIM(STR(m.pnyear))
    m.tfyf = PADL(ALLTRIM(STR(m.pnmonth)),2,'0')
    m.bgdwjc = PADL(ALLTRIM(STR(m.pnday)),2,'0')
    m.denkh = m.czy

    SELECT hisbase
    INSERT INTO hisbase FROM MEMVAR
ENDIF
