&&计算级别工资
FUNCTION jbgzse

PARAMETERS cTbnd,cJb,cDjc

IF TYPE("cJb")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 0
ENDIF

IF  EMPTY(cJb) OR EMPTY(cTbnd)
    RETURN 0
ENDIF

IF VAL(cJb)>15
    RETURN 0
ENDIF

oldalias=SELECT()

IF !USED("jbgzbz")
    crtvjbgzbz(.f.,conn)
ENDIF

SELECT jbgzbz
LOCATE FOR tbnd=cTbnd
SELECT (oldalias)

IF !EOF("jbgzbz")

    v_se=jbgzbz.a&cJb 
    IF cDjc>'0'
        cJb1=allt(str(val(cJb)+1)) 
        v_djc=val(cDjc) &&计算倒级差
        v_djcse=jbgzbz.a&cJb-jbgzbz.a&cJb1
        v_se=v_se+v_djcse*v_djc
    ENDIF

    RETURN v_se
ELSE
    RETURN 0
ENDIF
