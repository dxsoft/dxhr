FUNCTION rzjlall
&&ÈÎÖ°¼òÀú
PARAMETERS tcRybm,tcDwsx

SELECT DISTINCT tcdwsx+SUBSTR(zjbm,3,1)+IIF(tcdwsx="07" OR INLIST(zjbm,"01B0","01C0","01B1","01C1"),"0","1") as zwbm,zwjb,rzsj as srny,"1" as jdzw,00 as zwkjnx FROM jdzw;
 WHERE dwbm+grbm=tcrybm AND STRTRAN(rzsj,".","")>"195001" AND !EMPTY(rzsj) AND rzsj<>"    .  " UNION ;
SELECT DISTINCT zwbm,xzzw as zwjb,srny,"0" as jdzw,kjnx as zwkjnx FROM ryzwbh;
 WHERE dwbm+grbm=tcrybm AND LEFT(zwbm,2)=IIF(tcDwsx<"10",tcDwsx,"10") AND STRTRAN(srny,".","")>"195001" AND !EMPTY(srny) AND srny<>"    .  ";
  ORDER BY srny desc,zwbm DESC INTO ARRAY lat

IF _tally>0
    m.k=1
    m.zwjb=lat[1,2]
    m.srny=lat[1,3]
    m.jdzw=lat[1,4]
    m.kjnx=lat[1,5]
    m.zjbm=lat[1,1]
    IF _tally>1
        m.zdnx=0
	    FOR i=2 TO _tally
	        m.xzjbm=lat[i,1]
	        IF IIF(INLIST(LEFT(m.xzjbm,2),"01","02","03"),LEFT(m.xzjbm,3)+"0",m.xzjbm)<=IIF(INLIST(LEFT(m.zjbm,2),"01","02","03"),LEFT(m.zjbm,3)+"0",m.zjbm)
	            IF lat[i,4]="1"
		            m.zjbm=m.xzjbm
		            m.zwjb=lat[i,2]
				    m.srny=lat[i,3]
				    m.jdzw=lat[i,4]
				    m.kjnx=m.kjnx+lat[i,5]
		        ELSE
		            m.srny=lat[i,3]
				    m.kjnx=m.kjnx+lat[i,5]+m.zdnx
				    m.zdnx=0
		        ENDIF
		        k=i
	        ELSE
	            m.zdnx=m.zdnx+VAL(LEFT(lat[i-1,3],4))-VAL(LEFT(lat[i,3],4))
	        ENDIF
	    ENDFOR
	ENDIF

    cret=m.zjbm+","+m.zwjb+","+m.srny+","+m.jdzw+","+ALLTRIM(STR(m.kjnx))
    DO WHILE m.k<_tally
        k=k+1
	    m.zwjb=lat[k,2]
	    m.srny=lat[k,3]
	    m.jdzw=lat[k,4]
	    m.kjnx=lat[k,5]
        m.zjbm=lat[k,1]

	    m.zdnx=0
	    FOR i=k TO _tally
	        m.xzjbm=lat[i,1]
	        IF IIF(INLIST(LEFT(m.xzjbm,2),"01","02","03"),LEFT(m.xzjbm,3)+"0",m.xzjbm)<=IIF(INLIST(LEFT(m.zjbm,2),"01","02","03"),LEFT(m.zjbm,3)+"0",m.zjbm)
	            IF lat[i,4]="1"
		            m.zjbm=m.xzjbm
		            m.zwjb=lat[i,2]
				    m.srny=lat[i,3]
				    m.jdzw=lat[i,4]
				    m.kjnx=m.kjnx+lat[i,5]
		        ELSE
		            m.srny=lat[i,3]
				    m.kjnx=m.kjnx+lat[i,5]+m.zdnx
				    m.zdnx=0
		        ENDIF
		        k=i
	        ELSE
	            m.zdnx=m.zdnx+VAL(LEFT(lat[i-1,3],4))-VAL(LEFT(lat[i,3],4))
	        ENDIF
	    ENDFOR
	    cret=cret+","+m.zjbm+","+m.zwjb+","+m.srny+","+m.jdzw+","+ALLTRIM(STR(m.kjnx))
    ENDDO
    cret=cret+","
    
    RETURN LEFT(cret+SPACE(200),200)
ELSE
    RETURN SPACE(200)
ENDIF
