FUNCTION jxgz

PARAMETERS tcTbnd,tcRyflbm,tcXl,nNd

LOCAL oldalias

tcXl=SUBSTR(tcXl,3)
DO case
CASE tcXl="博士学位研究生毕业"
    lcXl="博士 "
CASE tcXl="硕士学位研究生毕业"
    lcXl="硕士"
CASE INLIST(tcXl,"研究生班毕业","无学位研究生","研究生结业","研究生肄业")
    lcXl="研究生"
CASE INLIST(tcXl,"双学士学位大学本科","6年制以上大学毕业") 
    lcXl="双学士"
CASE INLIST(tcXl,"大学本科毕业","大学本科结业")
    lcXl="本科"
CASE INLIST(tcXl,"相当大学毕业","大学肄业")
    lcXl="大普"
CASE INLIST(tcXl,"大专毕业","大专结业","相当大专毕业","大专肄业")
    lcXl="专科"
CASE INLIST(tcXl,"中专毕业")
    lcXl="中专"
CASE INLIST(tcXl,"中技毕业","相当中专/中技毕业","中专/中技肄业")
    lcXl="中技"
CASE INLIST(tcXl,"高中毕业","高中肄业")
    lcXl="高中"
CASE tcXl="职高毕业"
    lcXl="职高"
CASE INLIST(tcXl,"技校毕业","技校肄业")
    lcXl="技校生"
CASE tcXl="初中毕业"
    lcXl="初中"
OTHERWISE
    lcXl="其它"
ENDCASE

oldalias=ALIAS()

*!*	IF USED("jxgzbz")
    SELECT jxgzbz 
*!*	ELSE
*!*	    SELECT 0
*!*	    USE H+"\sysdata\jxgzbz" ALIAS jxgzbz
*!*	ENDIF

IF tcRyflbm>"10"
    tcRyflbm="10"
ENDIF

IF tcRyflbm="05" OR tcRyflbm="06" OR tcRyflbm="08" OR tcRyflbm="09"
    LOCATE FOR tbnd=tcTbnd AND ryflbm=tcRyflbm
ELSE
    nNd="1"
    IF EMPTY(tcXl)
		IF !EMPTY(oldalias)
		    SELECT (oldalias)
		ENDIF
        RETURN 0
    ELSE
        LOCATE FOR tbnd=tcTbnd AND ryflbm=tcRyflbm AND xl=ALLTRIM(lcXl)
    ENDIF
ENDIF

IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF FOUND("jxgzbz")
    IF nNd="1"
        RETURN jxgzbz.b1
    ENDIF
    IF nNd="2"
        RETURN jxgzbz.b2
    ENDIF
ENDIF

RETURN 0
