FUNCTION refDb

LOCAL ltIschanged
ltIschanged=.F.

OPEN DATABASE h+"\data\gzjsgl"

IF USED("dryjbxx")
    USE IN dryjbxx
ENDIF
IF USED("hisbase")
    USE IN hisbase
ENDIF
IF USED("bdry")
    USE IN bdry
ENDIF

USE H+"\data\dryjbxx.dbf" ALIAS dryjbxx IN 0 EXCLUSIVE
USE H+"\data\hisbase.dbf" ALIAS hisbase IN 0 EXCLUSIVE
USE H+"\data\bdry.dbf" ALIAS bdry IN 0 EXCLUSIVE

IF !USED("fldjbxx")
    USE H+"\data\fldjbxx.dbf" ALIAS fldjbxx IN 0
ENDIF
IF !USED("fldgz")
    USE H+"\data\fldgz.dbf" ALIAS fldgz IN 0
ENDIF

AFIELDS(aFldjbxx,"dryjbxx")
AFIELDS(aFldxzjsgzb,"hisbase")
AFIELDS(aFldbdry,"bdry")

SELECT fldjbxx
SCAN FOR sfsy="¡Ì" AND UPPER(tblname)="RYJBXX"
	DO CASE
	CASE UPPER(fldjbxx.field_type)="C"
		cExpr=ALLTRIM(fldjbxx.field_name)+" "+"C("+STR(field_len)+")"
	CASE UPPER(fldjbxx.field_type)="N"
		cExpr=ALLTRIM(fldjbxx.field_name)+" "+"N("+STR(field_len)+","+STR(field_dec)+")"
	CASE UPPER(fldjbxx.field_type)="D"
		cExpr=ALLTRIM(fldjbxx.field_name)+" "+"C(7)"
	ENDCASE

	**modi the table "dryjbxx"
	tExist=.F.
	tChanged=.F.
	FOR i=1 TO ALEN(aFldjbxx,1)
	    IF UPPER(ALLTRIM(aFldjbxx[i,1]))=UPPER(ALLTRIM(fldjbxx.field_name)) AND LEN(ALLTRIM(aFldjbxx[i,1]))=LEN(ALLTRIM(fldjbxx.field_name))  &&exist
	        tExist=.T.
	        IF aFldjbxx[i,2]<>STRTRAN(fldjbxx.field_type,"D","C") OR aFldjbxx[i,3]<>fldjbxx.field_len OR aFldjbxx[i,4]<>fldjbxx.field_dec
	            tChanged=.T.
	        ENDIF
	        
	        EXIT
	    ENDIF
	ENDFOR

	IF tExist
     	IF tChanged
     	    ALTER TABLE dryjbxx alter &cExpr
     	    ltIschanged=.T.
     	ENDIF
	ELSE
	    ALTER TABLE dryjbxx add &cExpr
	    ltIschanged=.T.
	ENDIF
	****
ENDSCAN

FOR j=1 TO ALEN(aFldjbxx,1)
	IF UPPER(ALLTRIM(aFldjbxx[j,2]))="N"
		cExpr=ALLTRIM(ALLTRIM(aFldjbxx[j,1]))+" "+"N("+ALLTRIM(STR(aFldjbxx[j,3]))+","+ALLTRIM(STR(aFldjbxx[j,4]))+")"
	ELSE
    	cExpr=ALLTRIM(aFldjbxx[j,1])+" "+ALLTRIM(aFldjbxx[j,2])+"("+ALLTRIM(STR(aFldjbxx[j,3]))+")"
    ENDIF

	**modi the table "hisbase"
	tExist=.F.
	tChanged=.F.
	FOR i=1 TO ALEN(aFldxzjsgzb,1)
	    IF UPPER(ALLTRIM(aFldxzjsgzb[i,1]))=UPPER(ALLTRIM(aFldjbxx[j,1])) AND LEN(ALLTRIM(aFldxzjsgzb[i,1]))=LEN(ALLTRIM(aFldjbxx[j,1]))    &&exist
	        tExist=.T.
	        IF aFldxzjsgzb[i,2]<>aFldjbxx[j,2] OR aFldxzjsgzb[i,3]<>aFldjbxx[j,3] OR aFldxzjsgzb[i,4]<>aFldjbxx[j,4]
	            tChanged=.T.
	        ENDIF
	        
	        EXIT
	    ENDIF
	ENDFOR

	IF tExist
     	IF tChanged
     	    ALTER TABLE hisbase alter &cExpr
     	    ltIschanged =.T.
     	ENDIF
	ELSE
	    ALTER TABLE hisbase add &cExpr
	    ltIschanged =.T.
	ENDIF

	**modi the table "bdry"
	tExist=.F.
	tChanged=.F.
	FOR i=1 TO ALEN(aFldbdry,1)
	    IF UPPER(ALLTRIM(aFldbdry[i,1]))=UPPER(ALLTRIM(aFldjbxx[j,1])) AND LEN(ALLTRIM(aFldbdry[i,1]))=LEN(ALLTRIM(aFldjbxx[j,1]))  &&exist
	        tExist=.T.
	        IF aFldbdry[i,2]<>aFldjbxx[j,2] OR aFldbdry[i,3]<>aFldjbxx[j,3] OR aFldbdry[i,4]<>aFldjbxx[j,4]
	            tChanged=.T.
	        ENDIF
	        
	        EXIT
	    ENDIF
	ENDFOR

	IF tExist
     	IF tChanged
     	    ALTER TABLE bdry alter &cExpr
     	    ltIschanged=.T.
     	ENDIF
	ELSE
	    ALTER TABLE bdry add &cExpr
	    ltIschanged=.T.
	ENDIF
	****
ENDFOR

****
SELECT fldjbxx
SCAN FOR sfsy="¡Ì" AND UPPER(tblname)="RYJBXX"
    DBSETPROP("dryjbxx."+ALLTRIM(fldjbxx.field_name),"FIELD","Caption",ALLTRIM(fldjbxx.field_cap))
ENDSCAN

SELECT fldgz
SCAN
    DBSETPROP("dryjbxx."+ALLTRIM(fldgz.field_name),"FIELD","Caption",ALLTRIM(fldgz.field_cap))
ENDSCAN

IF USED("dryjbxx")
    USE IN dryjbxx
ENDIF
IF USED("hisbase")
    USE IN hisbase
ENDIF
IF USED("bdry")
    USE IN bdry
ENDIF

USE IN fldjbxx
USE IN fldgz
