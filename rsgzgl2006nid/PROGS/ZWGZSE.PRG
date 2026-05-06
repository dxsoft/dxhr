&&计算职务工资
FUNCTION zwgzse

PARAMETERS cZwbm,cZwdc,cTbnd

IF TYPE("cZwbm")<>"C" OR TYPE("cZwdc")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 0
ENDIF

IF EMPTY(cZwbm) OR EMPTY(cZwdc)
    RETURN 0
ENDIF

IF VAL(cZwdc)>18
    RETURN 0
ENDIF

SEEK ALLTRIM(cTbnd)+ALLTRIM(cZwbm) ORDER tag ndbm IN zwgzbz

IF !EOF("zwgzbz")
   loc="zwgzbz.A"+ALLTRIM(cZwdc)
   RETURN &loc
ELSE
   RETURN 0
ENDIF
