&&计算基础工资
FUNCTION jcgz

PARAMETERS cTbnd

IF TYPE("cTbnd")<>"C"
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
    USE jcglgzbz alias jcglgzbz
ENDIF

LOCATE FOR tbnd=cTbnd
SELECT (oldalias)

IF !EOF("jcglgzbz")
    m.jcgz=jcglgzbz.jcgz
*!*	    USE IN jcglgzbz
    RETURN m.jcgz
ELSE
*!*	    USE IN jcglgzbz
    RETURN 0
ENDIF
