FUNCTION tzcs

PARAMETERS a,b

**根据变动前a,和变动后b的值，计算出定额调整数和调整比例
FOR i=1 TO 100
	IF ROUND(a*i/1000-INT(a*i/1000),2)=(b-a)-INT(b-a)
	    RETURN STR(i/10,3,1)+SPACE(2)+STR(b-a*i/1000-a)
	ENDIF 
ENDFOR
RETURN ""