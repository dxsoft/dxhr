&&计算浮动工资
FUNCTION fdgz

PARAMETERS cTbnd,cZwbm,cZwgzdc,cFddc,nJtbz

IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C" OR TYPE("cZwgzdc")<>"C" OR TYPE("cFddc")<>"C"
   RETURN 0
ENDIF

IF  EMPTY(cTbnd) OR EMPTY(cZwbm) OR EMPTY(cZwgzdc) OR EMPTY(cFddc)
    RETURN 0
ENDIF

IF VAL(cFddc)=0
    RETURN 0
ENDIF

oldalias=ALIAS()

IF USED("zwgzbz")
    SELECT zwgzbz
ELSE
    SELECT 0
    USE zwgzbz
ENDIF

SEEK cTbnd+cZwbm ORDER tag ndbm IN zwgzbz

IF FOUND("zwgzbz")
    v_dc1=allt(str(val(cZwgzdc)+val(cFddc)))
    v_fdgz=zwgzbz.a&v_dc1-zwgzbz.a&cZwgzdc

    IF cyxx.fdgz=2&&档次津贴均浮动
        v_fdgz=v_fdgz*(1+njtbz)
    ENDIF
    v_fdgz=zround(v_fdgz)

    SELECT (oldalias)
    RETURN v_fdgz
ELSE
    SELECT (oldalias)
    RETURN 0
ENDIF