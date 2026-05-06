FUNCTION gzbzdw
PARAMETERS tcBz,tcZwbm,tcTbnd

DO case
CASE tcBz="zwgzbz"
    tcZwbm=chgzwbm(tcZwbm)
    SEEK tcTbnd+tcZwbm ORDER tag ndbm IN zwgzbz
    IF FOUND("zwgzbz")
        RETURN .T.
    ELSE
        RETURN .F.
    ENDIF
    
CASE tcBz="jbtbz"
    IF LEFT(tcZwbm,2)>"10"
        tcZwbm="10"+RIGHT(tcZwbm,2)
    ENDIF
    
    SEEK tcTbnd+tcZwbm ORDER tag ndbm IN jbtbz
    IF FOUND("jbtbz")
        RETURN .T.
    ELSE
        RETURN .F.
    ENDIF

ENDCASE
