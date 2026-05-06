FUNCTION zjcc

PARAMETERS tczwbm

DO case
CASE tczwbm='0151'
    RETURN '一级巡视员        '
CASE tczwbm='0161'
    RETURN '二级巡视员        '
CASE tczwbm='0171'
    RETURN '二级调研员        '
CASE tczwbm='0181'
    RETURN '四级调研员        '
CASE tczwbm='0191'
    RETURN '二级主任科员      '
CASE tczwbm='01A1'
    RETURN '四级主任科员      '
CASE tczwbm='01B0'
    RETURN '一级科员          '
CASE tczwbm='01C0'
    RETURN '二级科员          '
OTHERWISE
	IF INLIST(LEFT(tczwbm,2),'21','22','23','24','25','26','27','28')
		SELECT DISTINCT mc from dmb WHERE bm='026'+tczwbm INTO ARRAY lazjcc
		IF _tally>0
		    RETURN PADR(lazjcc[1],18,' ')
		ELSE
		    RETURN SPACE(18)
		ENDIF
	ELSE
	    RETURN SPACE(18)
	ENDIF
ENDCASE

