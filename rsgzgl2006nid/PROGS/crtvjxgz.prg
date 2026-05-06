FUNCTION crtvjxgz

PARAMETERS ttUpdates,tcConn

IF EMPTY(m.ny)
    SQLEXEC(tcConn,"select a.*,b.cjgzny,b.zzny,b.xb,0000 as hj from djxgz a,dryjbxx b where a.dwbm='"+m.pdwbm+"' and a.dwbm=b.dwbm and a.grbm=b.grbm","jxgz")
ELSE
    SQLEXEC(tcConn,"select a.*,b.cjgzny,b.zzny,b.xb,0000 as hj from djxgz a,dryjbxx b where a.dwbm='"+m.pdwbm+"' and a.ny='"+m.ny+"' and a.dwbm=b.dwbm and a.grbm=b.grbm","jxgz")
ENDIF

IF ttUpdates
	CURSORSETPROP("tables","djxgz","jxgz")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,ny","jxgz")

	fn=""
	FOR i=1 TO FCOUNT("jxgz")
	    IF !INLIST(FIELD(i,"jxgz"),"CJGZNY","ZZNY","XB","HJ")
            fn=fn+","+FIELD(i,"jxgz")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxgz")

	fn=""
	FOR i=1 TO FCOUNT("jxgz")
	    IF !INLIST(FIELD(i,"jxgz"),"CJGZNY","ZZNY","XB","HJ")
            fn=fn+","+FIELD(i,"jxgz")+" djxgz."+FIELD(i,"jxgz")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxgz")

	CURSORSETPROP("sendupdates",.t.,"jxgz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jxgz
INDEX ON dwbm+grbm+ny TAG bm ADDITIVE

