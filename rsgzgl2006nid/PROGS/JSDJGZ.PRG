&&计算职务工资
FUNCTION jsdjgz

PARAMETERS cTbnd,cZwbm

IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 0
ENDIF

IF  EMPTY(cZwbm) OR EMPTY(cTbnd)
    RETURN 0
ENDIF

SEEK cTbnd+cZwbm ORDER tag ndbm IN jsdjgzbz

IF FOUND("jsdjgzbz")
   RETURN jsdjgzbz.jsdjgz
ELSE
   RETURN 0
ENDIF