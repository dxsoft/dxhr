&&计算职务工资
FUNCTION jdzwgzse

PARAMETERS cZwbm,cZwdc,cTbnd

IF TYPE("cZwbm")<>"C" OR TYPE("cZwdc")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 0
ENDIF

IF EMPTY(cZwbm) OR EMPTY(cZwdc)
    RETURN 0
ENDIF

isfound=.F.
FOR i=1 TO FCOUNT("jdgzbz")
    IF UPPER("A"+ALLTRIM(cZwdc))=UPPER(FIELD(i,"jdgzbz"))
        isfound=.T.
        EXIT
    ENDIF
ENDFOR

IF !isfound
    MESSAGEBOX("档次错误。",64,"错误信息")
    RETURN 0
ENDIF

oldalias=ALIAS()
SELECT jdgzbz
LOCATE FOR tbnd=ALLTRIM(cTbnd) AND zwbm=ALLTRIM(cZwbm)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF !EOF("jdgzbz")
   loc="jdgzbz.A"+ALLTRIM(cZwdc)
   RETURN &loc
ELSE
   RETURN 0
ENDIF
