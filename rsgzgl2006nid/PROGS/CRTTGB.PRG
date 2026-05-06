PROCEDURE crttgb

SELECT * FROM tgb_zj WHERE low="99" AND high="99" INTO ARRAY lat1
SELECT tgb_zj
FOR k=1 TO ALEN(lat1,1)
	SCAN FOR low<>"99" AND LEFT(zwbm,2)=LEFT(lat1[k,1],2)
	    v_ts=VAL(LEFT(lat1[k,4],AT("-",lat1[k,4])-1))
	    SCATTER TO lat
	    FOR i=4 TO ALEN(lat)
	        v_tn=VAL(SUBSTR(lat1[k,i],AT("-",lat1[k,i])+1))
	        IF !EMPTY(lat[i])
	            INSERT INTO bz06_tgb (zwbm,rzns,rznz,tgns,tgnz,jb,dc) VALUES (lat[1],VAL(lat[2]),VAL(lat[3]),v_ts,v_tn,IIF(AT(".",lat[i])>0,LEFT(lat[i],AT(".",lat[i])-1),""),IIF(AT(".",lat[i])>0,SUBSTR(lat[i],AT(".",lat[i])+1),LEFT(lat[i],2)))
	            IF i<ALEN(lat)
	                v_ts=VAL(LEFT(lat1[k,i+1],AT("-",lat1[k,i+1])-1))
	            ENDIF
	        ENDIF
	    ENDFOR
	ENDSCAN
ENDFOR
