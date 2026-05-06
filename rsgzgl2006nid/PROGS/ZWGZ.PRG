FUNCTION zwgz&&职务工资

PARAMETERS tcZwbm,tcTbnd

IF TYPE("tcZwbm")<>"C" OR TYPE("tcTbnd")<>"C"
   RETURN 0
ENDIF

IF EMPTY(tcZwbm) OR EMPTY(tcTbnd)
    RETURN 0
ENDIF

oldalias=ALIAS()
SELECT zwgzbz
LOCATE FOR zwbm=tcZwbm AND tbnd=tcTbnd
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF !EOF("zwgzbz")
   RETURN zwgzbz.zwgz
ELSE
   RETURN 0
ENDIF
