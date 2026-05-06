FUNCTION poppre

LPARAMETERS tcrybm,tcjsnf,tcjsyf,tcjslb,tczwbm2,tcjbgzjb2,tczwgzdc2,tctbnd

SELECT ryjbxx
SEEK tcrybm ORDER tag dwgrbm IN ryjbxx

*!*	xbmlb=ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb+ryjbxx.zwbm2+ryjbxx.jbgzjb2+ryjbxx.zwgzdc2+ryjbxx.tbnd

*!*	&&记下当前信息，如果按链表找不到上次信息时，按此信息查找
*!*	m.bmlbhj=hisbase.dwbm+hisbase.grbm+hisbase.zwbm1+hisbase.jbgzjb1+hisbase.zwgzdc1+hisbase.tbnd1+hisbase.jbtbz1+hisbase.jx1+hisbase.jxjtbz1&&不知道指针停在哪儿，取的值无效
*!*	m.rec=RECNO("hisbase")


*!*	*!*	SEEK ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb+ryjbxx.zwbm1+ryjbxx.jbgzjb1+ryjbxx.zwgzdc1+ryjbxx.tbnd1 ORDER tag yzbm IN hisbase

*!*	SEEK ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jslb+ryjbxx.jsnf+ryjbxx.jsyf ORDER tag pre IN hisbase


*!*	IF !FOUND("hisbase") OR RECNO("hisbase")=m.rec
*!*	*!*	    SEEK m.bmlbhj ORDER tag bmlbhj IN hisbase
*!*	*!*	    IF !FOUND("hisbase")
*!*		    SET FILTER TO dwbm + grbm = ryjbxx.dwbm+ryjbxx.grbm IN hisbase
*!*		    SET ORDER TO bmlbhj IN hisbase
*!*		    GO TOP IN hisbase
*!*		    
*!*		    DO FORM forms\sel TO ret
*!*		    IF ret
*!*				SELECT hisbase
*!*				SCATTER MEMVAR
*!*				   
*!*				SELECT ryjbxx
*!*				SEEK tcrybm ORDER tag dwgrbm IN ryjbxx
*!*				GATHER MEMVAR

*!*				SELECT hisbase
*!*				REPLACE yznf WITH "",yzyf WITH "",yzjslb WITH "" IN hisbase
*!*				SKIP IN hisbase
*!*				DELETE NEXT 100 IN hisbase

*!*				RETURN 0
*!*			ELSE
*!*				RETURN -1
*!*			ENDIF
*!*	*!*	    ENDIF
*!*	ENDIF

*!*	SELECT hisbase
*!*	SCATTER MEMVAR
*!*	   
*!*	SELECT ryjbxx
*!*	SEEK tcrybm ORDER tag dwgrbm IN ryjbxx
*!*	GATHER MEMVAR

*!*	SELECT hisbase
*!*	REPLACE yznf WITH "",yzyf WITH "",yzjslb WITH "" IN hisbase

*!*	SEEK m.xbmlb ORDER tag xbmlb IN hisbase
*!*	IF FOUND("hisbase")
*!*	    DELETE IN hisbase
*!*	ENDIF


SELECT hisbase
LOCATE FOR dwbm=ryjbxx.dwbm AND grbm=ryjbxx.grbm AND EMPTY(sid)
v_id=hisbase.id


DELETE FROM hisbase WHERE id=v_id

LOCATE FOR sid=v_id
REPLACE sid WITH ""

*!*	SCATTER MEMVAR
*!*	   
*!*	SELECT ryjbxx
*!*	SEEK tcrybm ORDER tag dwgrbm IN ryjbxx
*!*	GATHER MEMVAR
REPLACE xrzw WITH hisbase.xrzw,zwjb WITH hisbase.zwjb,srny WITH hisbase.srny IN ryjbxx



RETURN 0
