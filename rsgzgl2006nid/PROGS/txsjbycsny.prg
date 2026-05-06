FUNCTION txsjbycsny

LPARAMETERS tcsny,cate

&&cate 1-男职工，2-女干部，3-女工勤
LOCAL m_ycys,m_txsj

DO case
case cate=1
    &&1965.01前不延长
	IF tcsny<'1965.01'
	    m_ycys=0
	ELSE
		m_ycys=INT(((VAL(LEFT(tcsny,4))-1965)*12+VAL(SUBSTR(tcsny,6,2))+3)/4)
		IF m_ycys>36
		    m_ycys=36
		ENDIF
	ENDIF
	IF m_ycys/12=INT(m_ycys/12)&&
	    m_txsj=STR(VAL(LEFT(tcsny,4))+m_ycys/12+60,4)+SUBSTR(tcsny,5,3)
	ELSE
	    IF MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2))>12
	        m_txsj=STR(VAL(LEFT(tcsny,4))+INT(m_ycys/12)+61,4)+"."+PADL(ALLTRIM(STR(MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2))-12)),2,'0')
	    ELSE
	        m_txsj=STR(VAL(LEFT(tcsny,4))+INT(m_ycys/12)+60,4)+"."+PADL(ALLTRIM(STR(MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2)))),2,'0')
	    ENDIF
	ENDIF    
case cate=2
    &&1970.01前不延长
	IF tcsny<'1970.01'
	    m_ycys=0
	ELSE
		m_ycys=INT(((VAL(LEFT(tcsny,4))-1970)*12+VAL(SUBSTR(tcsny,6,2))+3)/4)
		IF m_ycys>36
		    m_ycys=36
		ENDIF
	ENDIF  
	IF m_ycys/12=INT(m_ycys/12)&&
	    m_txsj=STR(VAL(LEFT(tcsny,4))+m_ycys/12+55,4)+SUBSTR(tcsny,5,3)
	ELSE
	    IF MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2))>12
	        m_txsj=STR(VAL(LEFT(tcsny,4))+INT(m_ycys/12)+56,4)+"."+PADL(ALLTRIM(STR(MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2))-12)),2,'0')
	    ELSE
	        m_txsj=STR(VAL(LEFT(tcsny,4))+INT(m_ycys/12)+55,4)+"."+PADL(ALLTRIM(STR(MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2)))),2,'0')
	    ENDIF
	ENDIF
case cate=3
	&&1975.01之前不延长
	IF tcsny<'1975.01'
	    m_ycys=0
	ELSE
		m_ycys=INT(((VAL(LEFT(tcsny,4))-1975)*12+VAL(SUBSTR(tcsny,6,2))+1)/2)
		IF m_ycys>60
		    m_ycys=60
		ENDIF
	ENDIF
	IF m_ycys/12=INT(m_ycys/12)&&
	    m_txsj=STR(VAL(LEFT(tcsny,4))+m_ycys/12+50,4)+SUBSTR(tcsny,5,3)
	ELSE
	    IF MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2))>12
	        m_txsj=STR(VAL(LEFT(tcsny,4))+INT(m_ycys/12)+51,4)+"."+PADL(ALLTRIM(STR(MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2))-12)),2,'0')
	    ELSE
	        m_txsj=STR(VAL(LEFT(tcsny,4))+INT(m_ycys/12)+50,4)+"."+PADL(ALLTRIM(STR(MOD(m_ycys,12)+VAL(SUBSTR(tcsny,6,2)))),2,'0')
	    ENDIF
	ENDIF    
ENDCASE

RETURN m_txsj