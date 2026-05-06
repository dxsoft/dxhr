SELECT ryjbxx
COUNT TO cn

IF cn>0
    DO FORM progressbar1 NAME progressbar1 WITH "¹¤Áä¼ÆËã......"
	progressbar1.pgb.max=cn

	SELECT ryjbxx
	SCAN
	     v_gl=m.pnyear-VAL(LEFT(ryjbxx.cjgzny,4))+1-ryjbxx.zdgznx       
	     IF v_gl>0 AND v_gl<80
	         REPLACE ryjbxx.gznx WITH v_gl IN ryjbxx
	     ELSE
	         REPLACE ryjbxx.gznx WITH 0 IN ryjbxx
	     ENDIF
     
	     progressbar1.pgb.value = progressbar1.pgb.value + 1
	     
	     SELECT ryjbxx
	ENDSCAN

	IF CURSORGETPROP("Buffering","ryjbxx")=5
    	result=TABLEUPDATE(.t.,.T.,"ryjbxx")
    ENDIF
    
	progressbar1.visible=.F.
	progressbar1.release
ENDIF