FUNCTION crtvryjbxxbb

PARAMETERS ttUpdates,tcConn

IF USED("ryjbxxbb")
    USE IN ryjbxxbb
ENDIF


SQLEXEC(tcConn,"select * from ryjbxxb where dwbm='"+m.pdwbm+"' order by grbm","ryjbxxbb")

IF ttUpdates
	CURSORSETPROP("tables","ryjbxxb","ryjbxxbb")
	CURSORSETPROP("keyfieldlist","id","ryjbxxbb")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxxbb")
	    IF FIELD(i,"ryjbxxbb")<>"ID"
            fn=fn+","+FIELD(i,"ryjbxxbb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryjbxxbb")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxxbb")
        fn=fn+","+FIELD(i,"ryjbxxbb")+" ryjbxxb."+FIELD(i,"ryjbxxbb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryjbxxbb")

	CURSORSETPROP("sendupdates",.t.,"ryjbxxbb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryjbxxbb
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON dwbm TAG dwbm ADDITIVE

