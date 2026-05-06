FUNCTION crtvryjbxxb

PARAMETERS ttUpdates,tcConn

IF USED("ryjbxxb")
    USE IN ryjbxxb
ENDIF

SQLEXEC(tcConn,"select * from dryjbxxb where dwbm='"+ALLTRIM(m.pdwbm)+"' order by grbm","ryjbxxb")

IF ttUpdates
	CURSORSETPROP("tables","dryjbxxb","ryjbxxb")
	CURSORSETPROP("keyfieldlist","uid","ryjbxxb")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxxb")
	    IF FIELD(i,"ryjbxxb")<>"UID"
            fn=fn+","+FIELD(i,"ryjbxxb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryjbxxb")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxxb")
        fn=fn+","+FIELD(i,"ryjbxxb")+" dryjbxxb."+FIELD(i,"ryjbxxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryjbxxb")

	CURSORSETPROP("sendupdates",.t.,"ryjbxxb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryjbxxb
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON dwbm TAG dwbm ADDITIVE

