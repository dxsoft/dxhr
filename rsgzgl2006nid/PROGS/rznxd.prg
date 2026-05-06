FUNCTION rznxd

PARAMETERS tcrzsj,tnlow,tnsize,tnhight

DO case
CASE YEAR(DATE())-VAL(LEFT(tcrzsj,4))+1<tnlow
    RETURN 1
CASE YEAR(DATE())-VAL(LEFT(tcrzsj,4))+1<tnhight
    i=2
    DO WHILE .t.
        IF YEAR(DATE())-VAL(LEFT(tcrzsj,4))+1<tnlow+(i-1)*tnsize
            RETURN i
        ELSE
            i=i+1
        ENDIF
    ENDDO
OTHERWISE
    RETURN INT((tnhight-tnlow)/tnsize) +2
ENDCASE
