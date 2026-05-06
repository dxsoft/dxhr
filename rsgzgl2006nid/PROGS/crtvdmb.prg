FUNCTION crtvdmb

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select bm,mc,czbm,xt,sfsy from dmb","dmb")

IF ttupdate
	CURSORSETPROP("tables","dmb","dmb")
	CURSORSETPROP("keyfieldlist","bm","dmb")

	fn=""
	FOR i=1 TO FCOUNT("dmb")
        fn=fn+","+FIELD(i,"dmb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"dmb")

	fn=""
	FOR i=1 TO FCOUNT("dmb")
        fn=fn+","+FIELD(i,"dmb")+" dmb."+FIELD(i,"dmb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"dmb")

	CURSORSETPROP("sendupdates",.t.,"dmb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT dmb
INDEX ON bm TAG bm ADDITIVE
INDEX ON mc TAG mc ADDITIVE
INDEX ON "001"+ALLTRIM(mc) TAG zwmc ADDITIVE
