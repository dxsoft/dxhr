FUNCTION pushcur

LPARAMETERS tcjsnf,tcjsyf,tcjslb

RETURN

*!*	SELECT ryjbxx
*!*	SCATTER MEMVAR

*!*	SELECT hisbase

*!*	SEEK ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb+ryjbxx.zwbm2+ryjbxx.jbgzjb2+ryjbxx.zwgzdc2+ryjbxx.tbnd ORDER tag xbmlb IN hisbase

*!*	IF !FOUND("hisbase")
*!*	    INSERT INTO hisbase FROM MEMVAR 
*!*	ELSE
*!*	    GATHER MEMVAR
*!*	ENDIF
*!*	REPLACE yznf WITH "",yzyf WITH "",yzjslb WITH ""


	    =TABLEUPDATE(0,.T.,"hisbase")
        AERROR(aab)
        SQLEXEC(conn,"select @@identity as newid","lsid")
        newid=lsid.newid
        
        csql='update hisbase set sid = '+STR(newid)+' where id='+STR(pid)
		SQLEXEC(conn,csql)

        =TABLEUPDATE(1,.T.,"ryjbxx")