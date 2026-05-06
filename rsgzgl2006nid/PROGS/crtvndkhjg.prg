FUNCTION crtvndkhjg

PARAMETERS ttUpdates,tcConn,tcKhnd

*!*	nn=SQLEXEC(tcConn,"SELECT  a.grbm, a.xm, khnd, khjg, b.dwbm, a.xckhndzw, a.xckhndjb, a.dwsx,(SELECT TOP 1 zjbm+xrzw FROM dryzwbh WHERE dwbm+grbm=a.dwbm+a.grbm AND LEFT(zwbm,2)=a.dwsx AND srny<'"+tckhnd+".12' ORDER BY srny DESC) as zjbm, ' ' as bh FROM  dryjbxx a,dndkh b WHERE a.dwbm + a.grbm = b.dwbm + b.grbm and b.khnd='"+tckhnd+"' and a.dwbm='"+m.pdwbm+"' order by a.grbm,b.khnd","ndkhjg")

IF USED("ndkhjg")
    TABLEREVERT(.t.,"ndkhjg")
    USE IN ndkhjg
ENDIF

nn=SQLEXEC(tcConn,"SELECT b.grbm, a.xm, khnd, khjg, b.dwbm,a.dwsx,' ' as bh,b.id FROM  dryjbxx a,dndkh b WHERE a.dwbm + a.grbm = b.dwbm + b.grbm and b.khnd='"+tckhnd+"' and a.dwbm='"+ALLTRIM(m.pdwbm)+"' order by a.grbm,b.khnd","ndkhjg")

IF ttUpdates
	CURSORSETPROP("tables","dndkh","ndkhjg")
	CURSORSETPROP("keyfieldlist","id","ndkhjg")
	CURSORSETPROP("updatablefieldlist","id,dwbm,grbm,khnd,khjg","ndkhjg")
	CURSORSETPROP("updatenamelist","id dndkh.id,dwbm dndkh.dwbm,grbm dndkh.grbm,khnd dndkh.khnd,khjg dndkh.khjg","ndkhjg")
	CURSORSETPROP("sendupdates",.t.,"ndkhjg")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ndkhjg
INDEX ON khnd TAG khnd ADDITIVE
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE

CURSORSETPROP("Buffering",5,"ndkhjg")
