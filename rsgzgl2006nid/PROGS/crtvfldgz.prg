FUNCTION crtvfldgz

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from fldgz","fldgz")

IF ttUpdates
	CURSORSETPROP("tables","fldgz","fldgz")
	CURSORSETPROP("keyfieldlist","id","fldgz")

	fn=""
	FOR i=1 TO FCOUNT("fldgz")
	    IF FIELD(i,'fldgz')<>'ID'
            fn=fn+","+FIELD(i,"fldgz")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"fldgz")

	fn=""
	FOR i=1 TO FCOUNT("fldgz")
        fn=fn+","+FIELD(i,"fldgz")+" fldgz."+FIELD(i,"fldgz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"fldgz")

	CURSORSETPROP("sendupdates",.t.,"fldgz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT fldgz
INDEX ON UPPER(field_name) TAG fldname ADDITIVE
