FUNCTION infromtemp


v_filename=GETFILE("EXCEL表:XLS,XLSX","打开文件","确定",0,"选择文件")

IF EMPTY(V_FILENAME)
    RETURN
ENDIF

IF !INLIST(UPPER(JUSTEXT(v_filename)),"XLS","XLSX")
    RETURN
ENDIF

WAIT "正在导入，请稍后......" WINDOW AT SROWS()/2,SCOLS()/2

crtvhisbase(.t.,conn)
crtvryzwbh(.t.,conn)
crtvndkh(.t.,conn)
crtvxl(.t.,conn)
crtvryjbxx(.t.,conn)

CURSORSETPROP("Buffering",5,"ryjbxx")
CURSORSETPROP("Buffering",5,"ryzwbh")
CURSORSETPROP("Buffering",5,"xl")
CURSORSETPROP("Buffering",5,"ndkh")
CURSORSETPROP("Buffering",5,"hisbase")


*!*	TRY
	xlApp = CreateObject("Excel.Application")
	if error()=1733
	    messagebox('该功能需要Excel支持，请安装。',0,'系统提示')
	    return
	endif
	xlapp.StandardFontSize = "9"
	xlapp.SheetsInNewWorkbook = 1

	xlBook = xlApp.Workbooks.open(v_filename,,,,'69690358')

	xlSheet = xlBook.Worksheets(1)

	SET DECIMALS TO 2
	
    CREATE CURSOR lst(dwbm c(9),grbm c(5),xm c(10),xb c(2),sfzh c(18),csny c(7),cjgzny c(7),jrny c(7),dynkh c(11),zgxl c(18),byxx c(18),bysj c(7),khqk c(10),zwjb c(10),srny c(7),denkh c(20))
	j=2
	DO WHILE .t.  
	    IF isnull(xlSheet.cells(j,2).value)
	        EXIT
	    ELSE
			m.grbm = CAST(xlSheet.cells(j,1).value as i)
			m.grbm = PADL(ALLTRIM(STR(m.grbm)),5,'0')
		    m.xm = CAST(xlSheet.cells(j,2).value as c(18))
		    m.xb = CAST(xlSheet.cells(j,3).value as c(2))
			IF ISNULL(xlSheet.cells(j,4).value)
				m.sfzh = ''
			ELSE
				m.sfzh = CAST(xlSheet.cells(j,4).value as c(18))
			ENDIF
			IF ISNULL(xlSheet.cells(j,5).value)
				m.csny = ''
			ELSE
				m.csny = CAST(xlSheet.cells(j,5).value as c(7))
			ENDIF
			IF ISNULL(xlSheet.cells(j,6).value)
				m.cjgzny = ''
			ELSE
				m.cjgzny = CAST(xlSheet.cells(j,6).value as c(7))
			ENDIF
			IF ISNULL(xlSheet.cells(j,7).value)
				m.jrny = ''
			ELSE
				m.jrny = CAST(xlSheet.cells(j,7).value as c(7))
			ENDIF
			IF ISNULL(xlSheet.cells(j,8).value)
				m.dynkh = ''
			ELSE
				m.dynkh = CAST(xlSheet.cells(j,8).value as c(11))
			ENDIF
			IF ISNULL(xlSheet.cells(j,9).value)
				m.zgxl = ''
			ELSE
				m.zgxl = CAST(xlSheet.cells(j,9).value as c(18))
			ENDIF
			IF ISNULL(xlSheet.cells(j,10).value)
				m.byxx = ''
			ELSE
				m.byxx = CAST(xlSheet.cells(j,10).value as c(18))
			ENDIF
			IF ISNULL(xlSheet.cells(j,11).value)
				m.bysj = ''
			ELSE
				m.bysj = CAST(xlSheet.cells(j,11).value as c(18))
			ENDIF
			IF ISNULL(xlSheet.cells(j,12).value)
				m.khqk = ''
			ELSE
				m.khqk = CAST(xlSheet.cells(j,12).value as c(18))
			ENDIF
			IF ISNULL(xlSheet.cells(j,13).value)
				m.zwjb = ''
			ELSE
				m.zwjb = CAST(xlSheet.cells(j,13).value as c(18))
			ENDIF
			IF ISNULL(xlSheet.cells(j,14).value)
				m.srny = ''
			ELSE
				m.srny = CAST(xlSheet.cells(j,14).value as c(7))
			ENDIF
			IF ISNULL(xlSheet.cells(j,15).value)
				m.denkh = ''
			ELSE
				m.denkh = CAST(xlSheet.cells(j,15).value as c(20))
			ENDIF


		    insert into lst (dwbm,grbm,xm,sfzh,csny,cjgzny,jrny,dynkh,zgxl,byxx,bysj,khqk,zwjb,srny,denkh);
		     values ;
		     (m.pdwbm,m.grbm,m.xm,m.sfzh,m.csny,m.cjgzny,m.jrny,m.dynkh,m.zgxl,m.byxx,m.bysj,m.khqk,m.zwjb,m.srny,m.denkh)
		     
		     insert into ryjbxx (dwbm,grbm,xm,sfzh,xb,csny,ryfl,dwsx,gwfl,cjgzny,zzny,jrny,jrfs,zdgznx,gznx,jhlqsny,zdjhlnx,xlbm,zgxl,bjglxlnx,jx1,tc,txsj,xckhndzw,xckhndjb,bgdwjc,zwjb,zjbm,xrzw,srny,jx,tgbl,tgbl1,jtbl,jtbl1,fddc,fddc1,jsnf,jsyf,jslb,khqk,dynkh,denkh,bbz,hj1,zwbm1,zwgw1,zwgzdc1,zwgzse1,jbgzjb1,djc1,jbgzse1,jcgz1,glgz1,jsdjgz1,grjj1,blfb1,jsfszwtg1,jt1,fdgz1,jjjy1,dfbt1,gwjt1,hj2,zwbm2,zwgw2,zwgzdc2,zwgzse2,jbgzjb2,djc2,jbgzse2,jcgz2,glgz2,jsdjgz2,grjj2,blfb2,jsfszwtg2,jt2,fdgz2,jjjy2,dfbt2,gwjt2,bh,jxgz,zzbc,zwjt,zfbt,dsznf,nzgwsf,jzmcbt,sdbt1,sdbt,grsds,zfgjj,ylbxf,ylf,qtdk,bfyqgz,kjyqgz,sfgz,qtbt,jxjt,gryhzh,jxjt1,jxgz1,zwjt1,dsznf1,nzgwsf1,jzmcbt1,qtbt1,tfnf,tfyf,spdw,tbnd1,tbnd,jxjtbz1,jxjtbz,jbtbz1,jbtbz,jhljt,jhljt1,zfbt1,mz,pgbc,pgbc1,sidbt,zzmm,fdgd,fdsj,jzgb,ydwzw,yzwrzsj,nrjxgzbf,tgblbf,nrjxgzbf1,tgblbf1,jcjtbz1,jcjtbz,spjtbz1,spjtbz,dah,njbt1,njbt,gwjtbz1,gwjtbz,gwjtlb1,gwjtlb,sfjzgb,fdgd1,yctxsj);
		     values ;
		     (m.pdwbm,m.grbm,m.xm,m.sfzh,m.xb,m.csny,'专业技术人员','10','专业技术岗位',m.cjgzny,'',m.jrny,'',0,0,'',0,'',m.zgxl,0,'','已定工资','','2022','',m.zgxl,m.zwjb,'',m.zwjb,m.srny,'',0,0,'','','','','','','',m.khqk,m.dynkh,m.denkh,'',0,'','','',0,'','',0,0,0,0,0,0,0,0,0,0,0,0,0,'','','',0,'','',0,0,0,0,0,0,0,0,0,0,0,0,'',0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,'',0,0,0,0,0,0,0,'','','','','','','','','',0,0,0,'',0,0,0,'',0,'','','','',0,0,0,0,'','','','','',0,0,'','','','','',0,0)

              INSERT INTO ryzwbh (dwbm,grbm,xrzwbm,xrzw,zwjb,zjbm,zwbm,xzzw,zwlb,srny,kjnx,xrzwbz,jsbz) values (m.pdwbm,m.grbm,'',m.zwjb,m.zwjb,'','',m.zwjb,'技术',m.srny,0,'0','0')

              INSERT INTO xl (dwbm,grbm,xlbm,xl,byyx,rxsj,bysj,xz,xllb,bz) values (m.pdwbm,m.grbm,'',m.zgxl,m.byxx,'',m.bysj,0,'普通全日制','')


		     
	    ENDIF
	    j=j+1
	ENDDO
*!*	FINALLY
	xlBook.close()
	RELEASE xlapp
*!*	ENDTRY



*!*	INSERT INTO ryzwbh (dwbm,grbm,xrzwbm,xrzw,zwjb,zjbm,zwbm,xzzw,zwlb,srny,kjnx,xrzwbz,jsbz) SELECT dwbm,grbm,'',zwjb,zwjb,'','',zwjb,'技术',srny,0,0,0 FROM ryjbxx

*!*	INSERT INTO xl (dwbm,grbm,xlbm,xl,byyx,rxsj,bysj,xz,xllb,bz) SELECT dwbm,grbm,'',zgxl,byyx,'',bysj,'','普通全日制','' FROM ryjbxx

nn=TABLEUPDATE(1,.t.,"ryjbxx")

nn=TABLEUPDATE(1,.t.,"ryzwbh")
nn=TABLEUPDATE(1,.t.,"xl")


SQLEXEC(conn,"update dryjbxx set csny=SUBSTRING(sfzh,7,4)+'.'+SUBSTRING(sfzh,11,2) where csny=''")
  
SQLEXEC(conn,"update dryjbxx set jrny=LEFT(jrny,5)+'0'+SUBSTRING(jrny,6,2) where LEN(jrny)<7 AND jrny<>''")
SQLEXEC(conn,"update dryjbxx set srny=LEFT(srny,5)+'0'+SUBSTRING(srny,6,2) where LEN(srny)<7 AND srny<>''")
  
SQLEXEC(conn,"update dryjbxx set xlbm='12' where zgxl='硕士研究生'")
SQLEXEC(conn,"update dryjbxx set xlbm='23' where zgxl='本科'")
SQLEXEC(conn,"update dryjbxx set xlbm='31' where zgxl='专科' or zgxl='大专'")
SQLEXEC(conn,"update dryjbxx set xlbm='41' where zgxl='中专'")
SQLEXEC(conn,"update dryjbxx set xlbm='51' where zgxl='技校'")
SQLEXEC(conn,"update dryjbxx set xlbm='61' where zgxl='职高'")
SQLEXEC(conn,"update dryjbxx set xlbm='62' where zgxl='高中'")
  

SQLEXEC(conn,"update a set a.zjbm=substring(b.bm,4,4) from dryjbxx a inner join dmb b on a.zwjb=b.mc where left(b.bm,3)='00124'")


SQLEXEC(conn,"update dxl set xlbm='12' where xl='硕士研究生'")
SQLEXEC(conn,"update dxl set xlbm='23' where xl='本科'")
SQLEXEC(conn,"update dxl set xlbm='31' where xl='专科' or xl='大专'")
SQLEXEC(conn,"update dxl set xlbm='41' where xl='中专'")
SQLEXEC(conn,"update dxl set xlbm='51' where xl='技校'")
SQLEXEC(conn,"update dxl set xlbm='61' where xl='职高'")
SQLEXEC(conn,"update dxl set xlbm='62' where xl='高中'")

SQLEXEC(conn,"update dryzwbh set srny=LEFT(srny,5)+'0'+SUBSTRING(srny,6,2) where LEN(srny)<7 and srny<>''")
SQLEXEC(conn,"update a set a.xrzwbm=substring(b.bm,4,4),a.zjbm=substring(b.bm,4,4) from dryzwbh a inner join dmb b on a.zwjb=b.mc where left(b.bm,3)='001'")
SQLEXEC(conn,"update dryzwbh set zwbm='1004',xzzw='四级专业技术岗位' where SUBSTRING(zjbm,3,2)='01'")
SQLEXEC(conn,"update dryzwbh set zwbm='1007',xzzw='七级专业技术岗位' where SUBSTRING(zjbm,3,2)='02'")
SQLEXEC(conn,"update dryzwbh set zwbm='1010',xzzw='十级专业技术岗位' where SUBSTRING(zjbm,3,2)='03'")
SQLEXEC(conn,"update dryzwbh set zwbm='1012',xzzw='十二级专业技术岗位' where SUBSTRING(zjbm,3,2)='04'")
SQLEXEC(conn,"update dryzwbh set zwbm='1013',xzzw='十三级专业技术岗位' where SUBSTRING(zjbm,3,2)='05'")

SQLEXEC(conn,"update a set a.zjbm=substring(b.bm,4,4) from dryjbxx a inner join dmb b on a.zwjb=b.mc where left(b.bm,3)='001'")
SQLEXEC(conn,"update dryjbxx set zwbm2='1004',zwgw2='四级专业技术岗位' where SUBSTRING(zjbm,3,2)='01'")
SQLEXEC(conn,"update dryjbxx set zwbm2='1007',zwgw2='七级专业技术岗位' where SUBSTRING(zjbm,3,2)='02'")
SQLEXEC(conn,"update dryjbxx set zwbm2='1010',zwgw2='十级专业技术岗位' where SUBSTRING(zjbm,3,2)='03'")
SQLEXEC(conn,"update dryjbxx set zwbm2='1012',zwgw2='十二级专业技术岗位' where SUBSTRING(zjbm,3,2)='04'")
SQLEXEC(conn,"update dryjbxx set zwbm2='1013',zwgw2='十三级专业技术岗位' where SUBSTRING(zjbm,3,2)='05'")
SQLEXEC(conn,"update dryjbxx set tbnd='202110',jbtbz='201807',spdw='人事局',xckhndzw='2022'")

WAIT CLEAR


MESSAGEBOX("数据提取完毕",64,"提示信息",2000)
