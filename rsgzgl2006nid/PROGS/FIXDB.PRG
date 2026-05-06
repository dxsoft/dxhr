PROCEDURE fixdb

PARAMETERS tnError,tcfname

tcname=JUSTFNAME(tcfname)
tcname=LEFT(tcname,AT(".",tcname)-1)

DO case
CASE tnError=1542
	SET DATABASE TO gzjsgl
	REMOVE TABLE &tcname
	ADD TABLE (tcfname)
	USE (tcfname) EXCLUSIVE
	RETURN
CASE tnError=15
    MESSAGEBOX("数据表["+ALLTRIM(JUSTFNAME(tcfname))+"]已损坏, 如果你有数据备份, 请通过重新安装解决. ",64,"提示信息")
    RETURN

ENDCASE

