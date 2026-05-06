PUBLIC h,m.bb,m.dwsplb
PUBLIC spnf,m.ctitle
PUBLIC c_dybz,c_dwbm,c_pwdy,reportName &&pwdy---批文打印

PUBLIC m.czy,m.yhh,m.yhmm,m.yhqx,c_mlbz,c_menu,pdwbm,pdwbz,pdwmc,pjkjs,pjtbz,m.blxs,m.swyz,m.ctitle,m.tDfbt,thadchk,hlptopic,m_quit,m.zzgddc,m.zzgddchgr,;
	pnyear,pnmonth,pnday,m.dyrq,m.dyym,m.bhxsdw,m.dwmc,m.dwjc,m.rq,m.sjfs,m.nf,m.yf,c_sjbm,m.yhxg,zc_pgbc,v_lock,m.lbmc,m.wjyj,m.dwyj,v_rybm,m.zwbhhjsdj,m.cdchjsdj,m.xq,m.ny,pjxbl,m.rsdl,xtlb,jxlb,dbname,sfqyhgl,zxzc

LOCAL lnSeconds
PUBLIC loSplash,lochkupdate,loBak,conn,conn_str,conn_strlt,m.ctitle1,spqk,ver,dbtype,pjfly
PUBLIC m.jytg&&推算工资时，是否进行警员套改
PUBLIC m.jzdc&&降职处分，每降低一个职务层次，相应降低的薪级，目前政策是降2级

m.jzdc=2
m.czy=""
m.zxzc='bz'

*!*	conn_str="DRIVER=SQL Server;SERVER=DXSERVER\SQLEXPRESS;Trusted_Connection=Yes;DATABASE=gzjsgl;"

loSplash = .NULL.
lnSeconds = 0

SET TALK OFF
SET SAFETY OFF

SET EXCLUSIVE OFF
SET MULTILOCKS ON
CURSORSETPROP("buffering", 3, 0)

h=sys(5)+sys(2003)
*!*	H='E:\RSGZGL2006NID'

SET DEFAULT TO h+"\"
SET PATH TO data,reports,forms,progs,libs,qt,sysdata,menu,pict,tfda,graphics,update
SET CLASSLIB TO gzjsgl ADDITIVE

&&
*!*	ON ERROR do cwsjcl with error(),message(),message(1),program() &&错误事件处理  

loReg=NEWOBJECT("oldinireg", h+"\libs\registry.vcx")

fname="'"+h+"\sys.ini'"
m.server=loReg.getinientry("","server","name",&fname)
m.dbname=loReg.getinientry("","server","dbname",&fname)
m.dbtype=loReg.getinientry("","server","dbtype",&fname)

IF TYPE("m.dbtype")='N'
    m.dbtype='sql server'
ENDIF

*!*	IF TYPE("dbname")="N"
*!*	    m.dbname='gzjsgl'
*!*	    m.server="."
*!*	    conn_str="DRIVER=SQL Server;SERVER=.\SQLEXPRESS;Integrated Security=True;DATABASE=master;"

*!*		Local oWSS As WScript.Shell
*!*		Local cExeFile

*!*		m.oWSS = CreateObject('WScript.Shell')
*!*			
*!*		conn=SQLSTRINGCONNECT(conn_str,.t.)

*!*		SQLEXEC(conn,"CREATE LOGIN dxsoft WITH PASSWORD = '6262105dx';")
*!*		SQLEXEC(conn,"EXEC sp_addsrvrolemember @loginame=N'dxsoft',@rolename=N'bulkadmin'")

*!*	&&注意用户字符串有空格必须用双引号，不能是单引号
*!*	*!*		m.cCmdLine="'cacls "+h+"\data\gzjsgl.mdf /E /G everyone:f'"
*!*	*!*		aa=m.oWSS.Run('cacls d:\鼎星工资数据库\gzjsgl.mdf /E /G "Authenticated Users":f', 0, .T.)

*!*	    m.dname=h+'\data\gzjsgl.mdf'
*!*	    m.cCmdLine='cacls '+m.dname+' /E /G "Authenticated Users":f'
*!*		aa=m.oWSS.Run(m.cCmdLine, 0, .T.)

*!*	*!*		m.cCmdLine="'cacls "+h+"\data\gzjsgl_log.ldf /E /G "Authenticated Users":f'"
*!*	*!*		aa=m.oWSS.Run('cacls d:\鼎星工资数据库\gzjsgl_log.ldf /E /G "Authenticated Users":f', 0, .T.)

*!*	    m.dblog=h+'\data\gzjsgl_log.ldf'
*!*	    m.cCmdLine='cacls '+m.dblog+' /E /G "Authenticated Users":f'
*!*		aa=m.oWSS.Run(m.cCmdLine, 0, .T.)
*!*			
*!*		SQLEXEC(conn,"EXEC sp_attach_db @dbname = 'gzjsgl', @filename1 = '"+m.dname+"',@filename2 = '"+m.dblog+"' ")
*!*	    
*!*	    loReg.writeinientry(m.server,"server","name",&fname)
*!*	    loReg.writeinientry(m.dbname,"server","dbname",&fname)

*!*		SQLEXEC(conn,"select SERVERPROPERTY('IsIntegratedSecurityOnly') as ff;",'cur')
*!*	    IF cur.ff="1"&&若是window验证，改成混合验证模式，并重启服务
*!*	    	SQLEXEC(conn,"EXEC xp_instance_regwrite N'HKEY_LOCAL_MACHINE', N'Software\Microsoft\MSSQLServer\MSSQLServer',N'LoginMode', REG_DWORD, 2;")
*!*	        SQLDISCONNECT(conn)
*!*	        
*!*	*!*			m.cCmdLine = "cmd /k net stop MSSQL$SQLEXPRESS"
*!*			aa=m.oWSS.Run("net stop MSSQL$SQLEXPRESS", 0, .T.)

*!*	*!*			m.cCmdLine = "cmd /k net start MSSQL$SQLEXPRESS"
*!*			aa=m.oWSS.Run("net start MSSQL$SQLEXPRESS", 0, .T.)

*!*	*!*	    	RUN net stop MSSQL$SQLEXPRESS
*!*	*!*	    	RUN net start MSSQL$SQLEXPRESS
*!*	    ELSE
*!*	        SQLDISCONNECT(conn)
*!*	    ENDIF
*!*	ENDIF

RELEASE loReg

*!*	IF TYPE("m.server")="C"
*!*	    conn_str="DRIVER=SQL Server;SERVER="+m.server+"\SQLEXPRESS;Trusted_Connection=Yes;DATABASE=gzjsgl;"
*!*	    conn_strlt="DRIVER=SQL Server;SERVER="+m.server+"\SQLEXPRESS;Trusted_Connection=Yes;DATABASE=ltryxx;"
*!*	ELSE
*!*	    conn_str="DRIVER=SQL Server;SERVER=DXSERVER\SQLEXPRESS;Trusted_Connection=Yes;DATABASE=gzjsgl;"
*!*	    conn_strlt="DRIVER=SQL Server;SERVER=DXSERVER\SQLEXPRESS;Trusted_Connection=Yes;DATABASE=ltryxx;"
*!*	ENDIF

DO case
CASE UPPER(m.dbtype)="MYSQL"
	conn_str= "DRIVER={MySQL ODBC 3.51 Driver};SERVER="+m.server+";Port=3306;DATABASE=gzjsgl;UID=root;PWD=dx262105;CHARSET=gbk;"
	CURSORSETPROP("UpdateType",2,0)&&删除后插入，mysql需要这种方式，否则会更新后本地视图内容被删

CASE UPPER(m.dbtype)="SQLITE"
	conn_str="driver={SQLite3 ODBC Driver};database=e:\rsgl\rsgl.db"
	CURSORSETPROP("UpdateType",2,0)&&删除后插入，mysql需要这种方式，否则会更新后本地视图内容被删

OTHERWISE
    IF m.server='127.0.0.1'
        m.server='.'
    ENDIF
	conn_str="DRIVER=SQL Server;SERVER="+m.server+"\SQLEXPRESS;uid=dxsoft; pwd=6262105dx;DATABASE="+dbname+";"
	conn_strlt="DRIVER=SQL Server;SERVER="+m.server+"\SQLEXPRESS;uid=dxsoft; pwd=6262105dx;DATABASE=ltryxx;"
	CURSORSETPROP("UpdateType",1,0)&&老数据更新旧数据，sql server需要这种方式，否则如果有主键时更新提示不能为主键插入值
ENDCASE

*!*	conn_str="Driver={Microsoft Visual FoxPro Driver};UID=;PWD=;SourceDB=E:\rsgzgl2006\data\gzjsgl.dbc;SourceType=DBC;Exclusive=No;BackgroundFetch=Yes;Collate=Machine;Null=Yes;Deleted=Yes;"

*!*	conn_str="Driver=MYSQL ODBC 3.51 Driver;Server=192.168.1.139;Uid=root;Port=3306;Pwd=123;Database=rsgl;charset=gbk"


*!*	conn_str= "DRIVER={MySQL ODBC 8.0 ANSI Driver};SERVER=127.0.0.1;Port=3309;DATABASE=rsgzgl;UID=root;PWD=dx262105;charset=gbk"


*!*	nn=SQLSTRINGCONNECT("driver={SQLite3 ODBC Driver};database=e:\sqlite\cyj3.db",.T.)
*!*	?SQLEXEC(nn,"select * from zd_test")


conn=SQLSTRINGCONNECT(conn_str,.t.)
AERROR(laerr)


IF !DIRECTORY(h+"\temp")
    MD h+"\temp"
ELSE
*!*		DELETE FILE H+"\temp\*.*"&&不删除，在线升级时替换update.exe可能会出警告而替换不成功
ENDIF

&&
*!*	*!*	&&判断更新
*!*	lcUrl = "http://www.dxsoft.com.cn/updates/zz/updatejy/update.txt?ranparam="+ALLTRIM(STR(RAND(-1)*10000000))
*!*	oHtml = Createobject("MicroSoft.XmlHttp")

*!*	Local FHandle,nSize,cString,cString1
*!*	cString=""
m.ver=""

&&获取版本号
FHandle = FOPEN(H+"\update.ini")
nSize =  FSEEK(FHandle, 0, 2)     && Move pointer to EOF
IF nSize <= 0
ELSE
    = FSEEK(FHandle, 0, 0)      && Move pointer to BOF
    m.ver = "V"+FREAD(FHandle, nSize)
ENDIF
= FCLOSE(FHandle)


*!*	&&不使用缓存
*!*	*!*	oHtml.setRequestHeader('If-Modified-Since','0');

*!*	oHtml.Open([Get],lcUrl,.F.)
*!*	TRY
*!*		oHtml.Send(null)
*!*	CATCH
*!*	FINALLY
*!*	ENDTRY

*!*	IF oHtml.status=200&&下载网上资源，若不存在，从缓存查找，成功200
*!*		= StrToFile(oHtml.ResponseText,h+"\temp\update.txt")  &&下载文本文件


*!*		FHandle = FOPEN(H+"\temp\update.txt")
*!*		nSize =  FSEEK(FHandle, 0, 2)     && Move pointer to EOF
*!*		IF nSize <= 0
*!*		ELSE
*!*		    = FSEEK(FHandle, 0, 0)      && Move pointer to BOF
*!*		    cString = FREAD(FHandle, nSize)
*!*		ENDIF
*!*		= FCLOSE(FHandle)


*!*		IF m.ver<"V"+cstring
*!*		    m.ver="V"+cstring

*!*			lcUrl = "http://www.dxsoft.com.cn/updates/zz/updatejy/update.exe?ranparam="+ALLTRIM(STR(RAND(-2)*100000000))
*!*			RELEASE oHtml
*!*			oHtml = Createobject("MicroSoft.XmlHttp")
*!*			oHtml.Open([Get],lcUrl,.F.)
*!*			oHtml.setRequestHeader("Cache-Control", "no-cache")
*!*			oHtml.Send
*!*			
*!*			IF oHtml.status=200
*!*				= StrToFile(oHtml.responseBody,H+"\temp\update.exe")&&下载二进制文件

*!*				IF FILE(h+"\temp\update.txt")
*!*				    COPY FILE h+"\temp\update.txt" TO h+"\update.ini"
*!*				    DELETE FILE h+"\temp\update.txt"
*!*				ENDIF
*!*				
*!*			    _shellexecute1=CREATEOBJECT("_shellexecute")
*!*				_shellexecute1.shellexecute("update.exe","",H+"\temp\",,)

*!*				release all
*!*				release window
*!*				clos all
*!*				clea all
*!*				clear events
*!*				quit
*!*				RETURN
*!*			ELSE
*!*			    oHtml=.null.
*!*				RELEASE oHtml
*!*			ENDIF
*!*		ENDIF
*!*	ELSE
*!*	    oHtml=.null.
*!*		RELEASE oHtml
*!*	ENDIF

*!*	RELEASE oHtml
*!*	********判断更新结束

loSplash = NEWOBJECT("SPLASH", H+"\libs\gzjsgl.vcx")
IF VARTYPE(loSplash) = "O"   
    lnSeconds = SECONDS()
    loSplash.Show()
ENDIF

_screen.Visible= .F.
*!*	=INKEY(1-(SECONDS()-lnSeconds),"MH")
SET CENTURY ON
SET STATUS OFF
SET STATUS BAR OFF
SET DATE ANSI
SET DELE ON
SET SAFE OFF
SET ESCA OFF
SET EXCLU OFF
SET SYSMENU OFF
SET EXACT OFF
SET MULTILOCKS ON
SET CONSOLE OFF
SET NOTIFY OFF
SET RESOURCE OFF
ON KEY LABEL F1 dohelp(hlptopic)

m.pdwbm=""
m.hlptopic=""

IF !DIRECTORY(h+"\backup")
    MD h+"\backup"
ENDIF

IF !DIRECTORY(h+"\qt\")
    MD h+"\qt\"
ENDIF

IF !USED("cyxx")
    nn=SQLEXEC(conn,"select * from cyxx","cyxx")
ENDIF
GO BOTTOM IN cyxx
SELECT cyxx

aaa=LEN(STRCONV(cyxx.shrq,13))

*!*	bbb=STRCONV(aaa,14)




IF EOF("cyxx")
    DO FORM unlock WITH "NO"
ELSE
    DO FORM unlock WITH "YES"
ENDIF

*!*	IF !EMPTY(FIELD("单位编码","cyxx"))
*!*	    SQLEXEC(conn,"EXEC sp_rename 'cyxx.单位编码', 'dwbm', 'COLUMN';")
*!*	    SQLEXEC(conn,"EXEC sp_rename 'cyxx.单位名称', 'dwmc', 'COLUMN';")
*!*	    SQLEXEC(conn,"EXEC sp_rename 'cyxx.单位级次', 'dwjc', 'COLUMN';")
*!*	    SQLEXEC(conn,"EXEC sp_rename 'cyxx.主管人员', 'zgry', 'COLUMN';")
*!*	    SQLEXEC(conn,"EXEC sp_rename 'cyxx.在职人数', 'zzrs', 'COLUMN';")
*!*	    SQLEXEC(conn,"EXEC sp_rename 'cyxx.所在地市县', 'szds', 'COLUMN';")
*!*	    
*!*	    SQLEXEC(conn,"alter table cyxx add ID int identity(1,1) PRIMARY KEY")
*!*	    SQLEXEC(conn,"alter table yhgl add ID int identity(1,1) PRIMARY KEY")
*!*	    USE IN cyxx
*!*	    nn=SQLEXEC(conn,"select * from cyxx","cyxx")    
*!*	ENDIF

*!*	SQLEXEC(conn,"delete from bz06_zw_jb_xj where left(zwbm,2)='07'")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('0706','53','26')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('0707','50','21')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('0708','47','17')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('0709','44','12')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('070A','41','08')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('070B','38','04')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('070C','35','01')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('0717','50','21')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('0718','47','17')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('0719','44','12')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('071A','41','08')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('071B','38','04')")
*!*	SQLEXEC(conn,"insert into bz06_zw_jb_xj (zwbm,max,min) values ('071C','35','01')")

m.dwsplb=cyxx.dwsplb
m.blxs=cyxx.blxs
m.swyz=cyxx.swyz
m.zwbhhjsdj=cyxx.zwbhhjsdj
m.cdchjsdj=cyxx.cdchjsdj
m.lbshrq=cyxx.shrq
m.chkupdate=cyxx.chkupdate
m.zzgddc=cyxx.jqdm-1


IF m.dbtype='MySQL'
    m.zzgddchgr=BITTEST(cyxx.zzrs,1)
ELSE
    m.zzgddchgr=BITTEST(cyxx.zzrs,1)
ENDIF
m.xq=cyxx.szds
m.ny=""
xtlb=''

IF VAL(cyxx.sdbtmc)<>0
	m.jzdc=VAL(cyxx.sdbtmc)
ENDIF

IF m.zzgddc<0
    m.zzgddc=0
ENDIF
m.bhxsdw=.F.

m.yhh="   "
c_menu=.f.
c_mlbz='no'
pdwbm=''
pdwbz=''
pdwmc=""
pjxbl=""
pjfly=''
m.tDfbt=1
m.dwjc="  "
m.pnyear=YEAR(DATE())
m.pnmonth=MONTH(DATE())
m.pnday=DAY(DATE())
m.rq=dtoc(date())
IF MOD(VAL(cyxx.skbz),2)=1
    m.dyrq=.T.
ELSE
    m.dyrq=.F.
ENDIF
IF MOD(MOD(VAL(cyxx.skbz),2),2)=1
    m.dyym=.T.
ELSE
    m.dyym=.F.
ENDIF

m.rsdl=.f.&&登录时从cyxx里取，这里设置的只是默认值

m.spqk="审批通过"
m.jytg=0

USE IN cyxx

*!*	refdb()
IF loSplash.c2.width=276
    loSplash.timer1.interval=0
ENDIF

IF VARTYPE(loSplash) = "O"
   IF SECONDS() < lnSeconds + 3
       =INKEY(3-(SECONDS()-lnSeconds),"MH")
   ENDIF

   loSplash.Release()
   loSplash = .NULL.
ENDIF

DO FORM login WITH .T. && 用户注册
READ EVENTS


 _SCREEN.show
 ZOOM WINDOW screen MAX
 IF TYPE('_screen.image1')<>'O'
    _SCREEN.addobject('image1', 'image')
 ENDIF
 _SCREEN.image1.width = _SCREEN.width
 _SCREEN.image1.height = _SCREEN.height
 _SCREEN.image1.visible = .T.
 _SCREEN.image1.stretch = 2
 _SCREEN.image1.picture = 'main.jpg'

*!*	_screen.Picture='main.jpg'

DO gzjsgl.mpr    && 系统菜单
ACTIVATE MENU _msysmenu
_screen.WindowState= 2
_screen.Closable= .F.
_screen.Show(0)

IF m.chkupdate=1
	lochkupdate = CREATEOBJECT("chkupdate", m.lbshrq)
	IF VARTYPE(lochkupdate) = "O"   
	    lochkupdate.Show()
	    IF lochkupdate.check()
	        QUIT
	    ENDIF
	ENDIF
ENDIF

READ events

release all
release window
clos all
clea all
SQLDISCONNECT(conn)
clear events
SET STATUS  bar On
SET DELE Off
SET SAFE On
SET ESCA On
SET SYSMENU On
SET SYSMENU TO DEFA
QUIT
