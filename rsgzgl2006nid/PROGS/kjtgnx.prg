FUNCTION kjtgnx

PARAMETERS tcdwgrbm

LOCAL oldalias,kjnx
oldalias=ALIAS()
SELECT ndkh
m.kjnx=0
COUNT FOR !INLIST(khjg,"优秀","称职","合格","基本称职","基本合格","未定等次(试用期)","未定等次(见习期)") AND dwbm+grbm=tcdwgrbm AND BETWEEN(khnd,"1993","2005") TO m.kjnx
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

RETURN m.kjnx