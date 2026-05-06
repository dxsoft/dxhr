FUNCTION dxvalid

PARAMETERS tcxq,tcdwbm,tcdwmc,tny


lcUrl = "http://www.dxsoft.cn:7099/dxmanage/customer/selectCus.dx?membership="+STRCONV(STRCONV(STRCONV(ALLTRIM(tcxq),1),9),13)+"&code="+ALLTRIM(tcdwbm)+"&name="+STRCONV(STRCONV(STRCONV(ALLTRIM(tcdwmc),1),9),13)+"&selectType=0"

RELEASE oHtml
oHtml = Createobject("MicroSoft.XmlHttp")


&&²»Ê¹ÓÃ»º´æ
*!*	oHtml.setRequestHeader('If-Modified-Since','0');

oHtml.Open([Get],lcUrl,.F.)
oHtml.setRequestHeader("Cache-Control", "no-cache")

TRY
	oHtml.Send(null)
CATCH
*!*		fh=FCREATE("c:\logs.txt")
*!*		FWRITE(fh,error.Message)
*!*		FCLOSE(fh)
FINALLY
ENDTRY

IF oHtml.status=200
    aa=CAST(oHtml.responseBody as C(200))
    IF AT('200',aa)>0
	    aa=SUBSTR(STRCONV(aa,11),9,4)
		IF tny>aa
		    RETURN -1
		ENDIF
	ELSE
	    RETURN -1
	ENDIF
ELSE
	prg1=CREATEOBJECT("prg")
	IF tny>"20"+SUBSTR(prg1.jiemi(dwbm.bin),LEN(dwbm.dwbm+dwbm.dwmc)+1,2)
	    RETURN -1
	ENDIF
ENDIF

RETURN 0