PROCEDURE quitt

IF m_quit=.T.
    KEYBOARD '{CTRL+F4}'
    m_quit=.F.
*!*	    MESSAGEBOX("请先关闭打印预览窗口",64,"提示")
    RETURN
ENDIF

IF VARTYPE(lochkupdate) = "O"   
    lochkupdate.release
    lochkupdate=.NUll.
ENDIF

wcnt=_screen.FormCount
FOR i=1 TO m.wcnt
    _screen.Forms[1].release
ENDFOR

IF !USED("cyxx")
    =SQLEXEC(conn,"select * from cyxx","cyxx")
ELSE
    SELECT cyxx
ENDIF

fn="'"+h+"\temp\cyxx2.dbf'"
COPY TO &fn

fn="'"+h+"\temp\xxbak2026.id'"
fhand=FCREATE(&fn)
=FWRITE(fhand,jiami(cyxx.dwbm+cyxx.dwmc))
=FCLOSE(fhand)

loReg=NEWOBJECT("oldinireg", H+"\libs\registry.vcx")

fname="'"+h+"\sys.ini'"
m.autobak=loReg.getinientry("","backup","auto",&fname)
m.ask=loReg.getinientry("","backup","ask",&fname)
m.bak_path=loReg.getinientry("","backup","path",&fname)

m.sfbf="0"
IF m.ask="0" OR m.autobak="1"
    IF m.autobak="1"
        m.sfbf="1"
    ENDIF
ELSE
    DO FORM ask TO m.sfbf
ENDIF

IF m.sfbf="1"
	loBak = NEWOBJECT("bak", H+"\libs\gzjsgl.vcx")
	IF VARTYPE(loBak) = "O"   
	    loBak.Show()

		v_qdq=h+"\temp"
		DELETE FILE &v_qdq\*.*

		tt=TIME()
		IF !EMPTY(bak_path)
		    IF !DIRECTORY(ALLTRIM(bak_path))
		        MD (ALLTRIM(bak_path))
		    ENDIF
		    v_zip=ALLTRIM(bak_path)+ALLTRIM(cyxx.dwmc)+"人事工资信息备份"+CAST(DATE() as c(11))+LEFT(tt,2)+"时"+SUBSTR(tt,4,2)+"分"+SUBSTR(tt,7,2)+"秒"+".zl"
		ELSE
		    IF !DIRECTORY(JUSTDRIVE(H)+"\鼎星工资数据备份")
		        MD JUSTDRIVE(H)+"\鼎星工资数据备份"
		    ENDIF
		    v_zip=JUSTDRIVE(H)+"\鼎星工资数据备份\"+ALLTRIM(cyxx.dwmc)+"人事工资信息备份"+CAST(DATE() as c(11))+LEFT(tt,2)+"时"+SUBSTR(tt,4,2)+"分"+SUBSTR(tt,7,2)+"秒"+".zl"
		    cValue="'"+JUSTDRIVE(H)+"\鼎星工资数据备份\'"
		    fname="'"+h+"\sys.ini'"
		    loBak.oldinireg1.writeinientry(&cValue,"backup","path",&fname)
		ENDIF

		loBak.zip.zipfilename=v_zip

		=SQLEXEC(conn,"select * from sjwj","sjwj")
		SELECT sjwj
		SCAN FOR ALLTRIM(UPPER(sjwj.wjlj))="SBSH"
			IF loBak.c2.Width<360
			    loBak.c2.Width=loBak.c2.Width+12
			ELSE
			    loBak.c2.Width=0
			ENDIF
		    v_name=ALLTRIM(sjwj.fname)
		    v_dat=v_name+'2'
		    =SQLEXEC(conn,"select * from "+v_name,v_dat)
		    SELECT (v_dat)
		    COPY TO &v_qdq\&v_dat
		    USE IN (v_dat)
		    
		    IF ALLTRIM(UPPER(sjwj.wjlj))="SBSH" AND ALLTRIM(UPPER(sjwj.fname))<>"DWBM" AND ALLTRIM(UPPER(sjwj.fname))<>"BDRY" AND ALLTRIM(UPPER(sjwj.fname))<>"RYJBXXB" AND ALLTRIM(UPPER(sjwj.fname))<>"SC" AND ALLTRIM(UPPER(sjwj.fname))<>"YS" AND ALLTRIM(UPPER(sjwj.fname))<>"LCZJLTXF"
			    v_name=ALLTRIM(sjwj.fname)+"b"
			    v_dat=v_name+'2'
			    =SQLEXEC(conn,"select * from "+v_name,v_dat)
			    SELECT (v_dat)
			    COPY TO &v_qdq\&v_dat  
			    USE IN (v_dat)
			ENDIF
		    
		    loBak.c2.refresh
		    loBak.refresh
		    
		    SELECT sjwj   
		ENDSCAN
		loBak.c2.Width=360
		
		USE IN sjwj

		loBak.zip.sourcefile=h+'\temp\*.*'
		loBak.zip.dozip

		DELETE FILE &h\temp\*.*

*!*			    loBak.backup()
	    loBak.Release()
        loBak = .NULL.
	ENDIF
    WAIT CLEAR
ENDIF

release window
release all
clos all
*!*	clea all  &&执行清除命令有时不能清除正在使用的对象造成出错
CLEAR EVENTS
QUIT
