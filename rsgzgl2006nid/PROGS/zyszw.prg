FUNCTION zyszw

PARAMETERS tcRybm

LOCAL i,j,k,lat

DIMENSION lat[1,3]

SELECT zjbm,zwmc,rzsj FROM jdzw WHERE dwbm+grbm=tcrybm ORDER BY rzsj INTO ARRAY lat

IF _tally>0
    j=1
    DIMENSION p[j]
    p[j]=1
    k=1
    m.zjbm=lat[1,1]
    FOR i=1 TO _tally
        **IF LEFT(lat[i,1],3)<>LEFT(m.zjbm,3)
        IF LEFT(lat[i,1],4)<>LEFT(m.zjbm,4)
            j=j+1
            k=i
            DIMENSION p(j)
            p[j]=k
            m.zjbm=lat[i,1]
        ENDIF
    ENDFOR
    cret=""
    FOR i=j TO 1 STEP -1
        cret=cret+lat[p(i),1]+","+lat[p(i),2]+","+lat[p(i),3]+","
    ENDFOR 
    RETURN cret
ELSE
    RETURN SPACE(100)
ENDIF
