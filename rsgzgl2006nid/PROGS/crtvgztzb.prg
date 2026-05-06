FUNCTION crtvgztzb

PARAMETERS ttUpdates,tcConn,tdwbm,tgrbm

IF USED("gztzb")
    USE IN gztzb
ENDIF

&&这种方法在链错误时增资乱，暂使用下边方式
*!*	SQLEXEC(tcConn,"select a.jslb,a.jsnf,a.jsyf,a.zwgw2,a.jbgzjb2,a.zwgzdc2,a.hj2,a.hj2-b.hj2 as zze,a.bbz,a.xckhndjb,a.xckhndzw,a.id,a.dwbm,a.grbm from hisbase a left join hisbase b on a.id=b.sid where a.dwbm='"+tdwbm+"' and a.grbm='"+tgrbm+"' order by jsnf,jsyf,hj2,bbz","gztz")
*!*	UPDATE gztz SET zze=hj2 WHERE ISNULL(zze)


SQLEXEC(tcConn,"select jslb,jsnf,jsyf,zwgw2,jbgzjb2,zwgzdc2,hj2,hj2 as zze,bbz,xckhndjb,xckhndzw,id,dwbm,grbm,sid from hisbaseb where dwbm='"+ALLTRIM(tdwbm)+"' and grbm='"+tgrbm+"' order by jsnf,jsyf,hj2,bbz","gztzb")
m.ygz=0
SELECT gztzb

SCAN
    REPLACE zze WITH hj2-m.ygz IN gztzb
    m.ygz=hj2
ENDSCAN

IF ttUpdates
	CURSORSETPROP("tables","hisbaseb","gztzb")
	CURSORSETPROP("keyfieldlist","id","gztzb")

	fn=""
	FOR i=1 TO FCOUNT("gztzb")
	    IF UPPER(FIELD(i))<>"ZZE"
	    	fn=fn+","+FIELD(i,"gztzb")
	    ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"gztzb")

	fn=""
	FOR i=1 TO FCOUNT("gztzb")
	    IF UPPER(FIELD(i))<>"ZZE"
    	    fn=fn+","+FIELD(i,"gztzb")+" hisbaseb."+FIELD(i,"gztzb")
    	ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"gztzb")

	CURSORSETPROP("sendupdates",.t.,"gztzb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT gztzb
INDEX ON id TAG id
INDEX ON sid TAG sid
INDEX ON dwbm+grbm+jsnf+jsyf+STR(hj2)+bbz TAG bmlbhj
SET ORDER TO bmlbhj

CURSORSETPROP("Buffering",5,"gztzb")