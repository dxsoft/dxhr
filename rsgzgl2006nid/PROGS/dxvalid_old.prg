FUNCTION dxvalid

PARAMETERS tcxq,tcdwbm,tcdwmc,tny

IF m.rsdl
	m.xq=ALLTRIM(m.xq)
	DO case
	CASE m.xq="浉河区卫生局"
	    fname="updateshws.dat"
	CASE m.xq="光山县人才交流中心"
	    fname="updatersdl.dat"
	OTHERWISE
	    fname="updatersdl.dat"
    ENDCASE
ELSE
	m.xq=ALLTRIM(m.xq)
	DO case
	CASE m.xq="商城县"
	    fname="updatesc.dat"
	CASE m.xq="罗山县"
	    fname="updatels.dat"
	CASE m.xq="光山县"
	    fname="updategs.dat"
	CASE m.xq="潢川县"
	    fname="updatehc.dat"
	CASE m.xq="新县"
	    fname="updatexx.dat"
	CASE m.xq="息县"
	    fname="updatexix.dat"
	CASE m.xq="淮滨县"
	    fname="updatehb.dat"
	CASE m.xq="平桥区"
	    fname="updatepq.dat"
	CASE m.xq="浉河区"
	    fname="updatesh.dat"
	CASE m.xq="羊山"
	    fname="updateys.dat"
	CASE m.xq="南湾"
	    fname="updatenw.dat"
	CASE m.xq="工业城"
	    fname="updategyc.dat"
	CASE m.xq="鸡公山"
	    fname="updatejgs.dat"
	CASE m.xq="上天梯"
	    fname="updatestt.dat"
	CASE m.xq="信阳市"
	    fname="updatesz.dat"
	CASE m.xq="潢川开发区"
	    fname="updatehckfq.dat"
	OTHERWISE
	    fname="update.dat"
	ENDCASE
ENDIF

ON ERROR do cwsjcl with error(),message(),message(1),program() &&错误事件处理  

lcUrl = "http://www.dxsoft.com.cn/updates/zz/"+fname+"?ranparam="+ALLTRIM(STR(RAND(-2)*100000000))
RELEASE oHtml
oHtml = Createobject("MicroSoft.XmlHttp")
oHtml.Open([Get],lcUrl,.F.)
oHtml.setRequestHeader("Cache-Control", "no-cache")
oHtml.Send

IF oHtml.status=200
	= StrToFile(oHtml.responseBody,H+"\temp\update.dat")&&下载二进制文件
			
	fhd=FOPEN(h+"\temp\update.dat")
	dw=FGETS(fhd)
	IF m.rsdl
		DO WHILE !EMPTY(dw)
		    IF LEFT(dw,LEN(dw)-2)==ALLTRIM(cyxx.单位名称)
				EXIT
		    ENDIF
		    dw=FGETS(fhd)
		ENDDO
	ELSE
		DO WHILE !EMPTY(dw)
		    IF LEFT(dw,LEN(dw)-2)==ALLTRIM(dwbm.dwbm)+ALLTRIM(dwbm.dwmc)
				EXIT
		    ENDIF
		    dw=FGETS(fhd)
		ENDDO
	ENDIF
	FCLOSE(fhd)

	IF EMPTY(dw) OR tny>"20"+RIGHT(dw,2)&&未找到或已过期
	    DO FORM forms\sj
	    RETURN -1
	ENDIF
ELSE
	prg1=CREATEOBJECT("prg")
	IF tny>"20"+SUBSTR(prg1.jiemi(dwbm.bin),LEN(dwbm.dwbm+dwbm.dwmc)+1,2)
	    DO FORM forms\sj
	    RETURN -1
	ENDIF
ENDIF

RETURN 0