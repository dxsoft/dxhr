&&计算工龄工资
FUNCTION glgz

PARAMETERS cTbnd,nGznx

IF TYPE("cTbnd")<>"C" OR TYPE("nGznx")<>"N"
   RETURN 0
ENDIF

IF EMPTY(cTbnd)
    RETURN 0
ENDIF

oldalias=ALIAS()

IF USED("jcglgzbz")
    SELECT jcglgzbz
ELSE
    SELECT 0
    USE jcglgzbz
ENDIF

SELECT jcglgzbz
LOCATE FOR tbnd=cTbnd
SELECT (oldalias)

IF !EOF("jcglgzbz")
    m.glgz=jcglgzbz.glgz*nGznx
*!*	    USE IN jcglgzbz
    RETURN m.glgz
ELSE
*!*	    USE IN jcglgzbz
    RETURN 0
ENDIF
