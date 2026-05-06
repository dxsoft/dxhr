*!*	h="g:\rsgzgl2006"

IF USED("dwinfo")
    USE IN dwinfo
ENDIF
IF USED("jbinfo")
    USE IN jbinfo
ENDIF
IF USED("ndkh")
    USE IN ndkh
ENDIF
IF USED("zwbh")
    USE IN zwbh
ENDIF
IF USED("ndkh")
    USE IN ndkh
ENDIF
IF USED("zydj")
    USE IN zydj
ENDIF

WAIT "生成数据...." WINDOW AT 60,80 NOWAIT

*!*	SELECT dwbm FROM dwbm WHERE LEN(ALLTRIM(dwbm))=3 INTO CURSOR lstdw
*!*	IF _tally>0

	&&单位信息里缺少主管单位，筛选不出信息，有主管单位信息，执行这里,局用版一般有一级单位
	SQLEXEC(conn,"SELECT z.dwmc as zgdw,z.dwmc as zgdwmc,d.dwmc,d.dwmc,'' as dm,'公益一类' as dwsx,d.jfly,d.jb,'专技岗位' as zglx,'主要面向社会提供公益服务' as fwxz,'' as lxr,'' as lxdh FROM dwbm d "+"LEFT JOIN dwbm z ON LEFT(d.dwbm,3)=z.dwbm WHERE LEN(rTRIM(z.dwbm))=3 ORDER BY d.dwbm","dwinfo")

	SQLEXEC(conn,"SELECT z.dwmc as zgdw,'' as dwmc,d.dwmc,a.xm,a.xb,a.sfzh,a.csny,a.zzmm,'' as jg,a.mz,a.cjgzny,d.jfly,b.xl as zgxl,'' as zgxlzy,b.byyx as zgxlyx,"+"b.bysj as zgxlsj,c.xl as dyxl,'' as dixlzy,c.byyx as dyxlyx,c.bysj as dyxlsj,a.jrfs,case when jrny='.' or jrny='' then cjgzny else jrny end,a.zwbm2,a.zwbm2 as zwgw2,"+"a.xrzw,a.srny,'否' as jz,'' as jzgwdj,'' as jzgwmc,'' as jzsj,'否' as sflt FROM dryjbxx a LEFT JOIN (SELECT * FROM dxl WHERE dwbm+grbm+xlbm in "+"(SELECT dwbm+grbm+MIN(xlbm) FROM dxl GROUP BY dwbm,grbm)) b ON a.dwbm=b.dwbm AND a.grbm=b.grbm LEFT JOIN (SELECT * FROM dxl WHERE dwbm+grbm+bysj in"+" (SELECT dwbm+grbm+MIN(bysj) FROM dxl GROUP BY dwbm,grbm)) c ON a.dwbm=c.dwbm AND a.grbm=c.grbm LEFT join dwbm d ON a.dwbm=d.dwbm LEFT JOIN dwbm z ON LEFT(a.dwbm,3)=z.dwbm WHERE LEN(rTRIM(z.dwbm))=3 ORDER BY a.dwbm,a.grbm","jbinfo")

	SQLEXEC(conn,"SELECT z.dwmc as zgdw,d.dwmc,b.xm,b.sfzh,a.khnd,a.khjg,'' as bz FROM dndkh a LEFT JOIN dryjbxx b ON a.dwbm=b.dwbm AND a.grbm=b.grbm"+" LEFT JOIN dwbm d ON a.dwbm=d.dwbm LEFT JOIN dwbm z ON LEFT(a.dwbm,3)=z.dwbm WHERE LEN(rTRIM(z.dwbm))=3 ORDER BY a.dwbm,a.grbm,a.khnd","ndkh")

	SQLEXEC(conn,"SELECT z.dwmc as zgdw,d.dwmc,a.xm,a.sfzh,b.zwbm,b.zwbm as xzzw,b.xrzw,b.srny,'否' as jz,'' as jzlx,'' as jzgwdj,'' as jzgwmc,'' as jzgwsj FROM dryzwbh b"+" LEFT JOIN dryjbxx a ON b.dwbm=a.dwbm AND b.grbm=a.grbm LEFT JOIN dwbm d ON b.dwbm=d.dwbm LEFT JOIN dwbm z ON LEFT(a.dwbm,3)=z.dwbm WHERE LEN(rTRIM(z.dwbm))=3 ORDER BY b.dwbm,b.grbm","zwbh")

	SQLEXEC(conn,"SELECT z.dwmc as zgdw,d.dwmc,a.xm,a.sfzh,b.zwbm,b.srny,'否' as jz,'' as jzlx,'' as jzgwdj,'' as jzgwmc,'' as jzgwsj FROM dryzwbh b LEFT JOIN dryjbxx a ON b.dwbm=a.dwbm AND b.grbm=a.grbm"+" LEFT JOIN dwbm d ON b.dwbm=d.dwbm LEFT JOIN dwbm z ON LEFT(a.dwbm,3)=z.dwbm WHERE LEN(rTRIM(z.dwbm))=3 AND b.zwbm like '071%' ORDER BY a.dwbm,a.grbm","zydj")

*!*	ELSE
*!*		SELECT '' as zgdw,'' as zgdwmc,d.dwmc,d.dwmc,'' as dm,'公益一类' as dwsx,a.jfly,a.jb,'专技岗位' as zglx,'主要面向社会提供公益服务' as fwxz,'' as lxr,'' as lxdh FROM dwbm d ORDER BY d.dwbm INTO CURSOR dwinfo

*!*		SELECT '' as zgdw,'' as dwmc,d.dwmc,a.xm,a.xb,a.sfzh,a.csny,a.zzmm,'' as jg,a.mz,a.cjgzny,d.jfly,b.xl as zgxl,'' as zgxlzy,b.byyx as zgxlyx,b.bysj as zgxlsj,c.xl as dyxl,'' as dixlzy,c.byyx as dyxlyx,c.bysj as dyxlsj,a.jrfs,IIF(EMPTY(STRTRAN(jrny,'.')),cjgzny,jrny),a.zwbm2,a.zwbm2 as zwgw2,a.xrzw,a.srny,'否' as jz,'' as jzgwdj,'' as jzgwmc,'' as jzsj,'否' as sflt FROM dryjbxx a LEFT JOIN (SELECT * FROM dxl WHERE dwbm+grbm+xlbm in (SELECT dwbm+grbm+MIN(xlbm) FROM dxl GROUP BY dwbm,grbm)) b ON a.dwbm=b.dwbm AND a.grbm=b.grbm LEFT JOIN (SELECT * FROM dxl WHERE dwbm+grbm+bysj in (SELECT dwbm+grbm+MIN(bysj) FROM dxl GROUP BY dwbm,grbm)) c ON a.dwbm=c.dwbm AND a.grbm=c.grbm LEFT join dwbm d ON a.dwbm=d.dwbm ORDER BY a.dwbm,a.grbm INTO CURSOR jbinfo

*!*		SELECT '' as zgdw,d.dwmc,b.xm,b.sfzh,a.khnd,a.khjg,'' as bz FROM dndkh a LEFT JOIN dryjbxx b ON a.dwbm=b.dwbm AND a.grbm=b.grbm LEFT JOIN dwbm d ON a.dwbm=d.dwbm ORDER BY a.dwbm,a.grbm INTO CURSOR ndkh

*!*		SELECT '' as zgdw,d.dwmc,a.xm,a.sfzh,b.zwbm,b.zwbm as xzzw,b.xrzw,b.srny,'否' as jz,'' as jzlx,'' as jzgwdj,'' as jzgwmc,'' as jzgwsj FROM dryzwbh b LEFT JOIN dryjbxx a ON b.dwbm=a.dwbm AND b.grbm=a.grbm LEFT JOIN dwbm d ON b.dwbm=d.dwbm ORDER BY b.dwbm,b.grbm INTO CURSOR zwbh

*!*		SELECT '' as zgdw,d.dwmc,a.xm,a.sfzh,b.zwbm,b.srny,'否' as jz,'' as jzlx,'' as jzgwdj,'' as jzgwmc,'' as jzgwsj FROM dryzwbh b LEFT JOIN dryjbxx a ON b.dwbm=a.dwbm AND b.grbm=a.grbm LEFT JOIN dwbm d ON b.dwbm=d.dwbm where b.zwbm like '071%' ORDER BY a.dwbm,a.grbm INTO CURSOR zydj

*!*	ENDIF


xlApp = CreateObject("Excel.Application")
if error()=1733
    messagebox('该功能需要Excel支持，请安装。',0,'系统提示')
    return
endif
xlapp.StandardFontSize = "9"
xlapp.SheetsInNewWorkbook = 1

wait "导出数据......." windows at 60,80 nowait

if !file(H+"\info.xlsx")
    messagebox('未找到模板文件。',0,'系统提示')
    return
endif

xlBook = xlApp.Workbooks.Add(h+"\info.xlsx")
xlSheet = xlBook.Worksheets(1)


&&导出单位信息
SELECT dwinfo
k=2
SCAN
	FOR i=1 to 12
	    mm=alltrim(FIELD(i))
	    IF !EMPTY(mm)
		    IF TYPE(mm)="N"
		        IF &mm =0
		            LOOP
		        ENDIF
		    ENDIF

            xlSheet.cells(k,i).value = ALLTRIM(&mm)
        ENDIF
	ENDFOR

    k=k+1
ENDSCAN


&&导出基本信息
xlSheet = xlBook.Worksheets(2)

SELECT jbinfo
k=2
SCAN
	FOR i=1 to FCOUNT("jbinfo")
	    mm=alltrim(FIELD(i))
	    IF !EMPTY(mm)
		    IF TYPE(mm)="N"
		        IF &mm =0
		            LOOP
		        ENDIF
		    ENDIF

            DO case
            CASE INLIST(mm,"CJGZNY","SRNY","JRNY","CSNY")
                xlSheet.cells(k,i).value = STRTRAN(&mm,'.')
            CASE mm="JRFS"
                IF AT("调入",&mm)>0
                    xlSheet.cells(k,i).value = "调入"
                ENDIF
            CASE mm="ZWBM2"
                IF LEFT(&mm,2)="07"
                    xlSheet.cells(k,i).value = "管理岗位"
                ENDIF
                IF LEFT(&mm,2)="08" OR LEFT(&mm,2)="09"
                    xlSheet.cells(k,i).value = "工勤岗位"
                ENDIF
                IF LEFT(&mm,2)="10"
                    xlSheet.cells(k,i).value = "专技岗位"
                ENDIF

                
            CASE mm="ZWGW2"
                IF &mm="070C"
                    xlSheet.cells(k,i).value = "十级(办事员)"
                ENDIF
                IF &mm="070B"
                    xlSheet.cells(k,i).value = "九级(科员)"
                ENDIF
                IF &mm="070A"
                    xlSheet.cells(k,i).value = "八级(副科)"
                ENDIF
                IF &mm="0709"
                    xlSheet.cells(k,i).value = "七级(正科)"
                ENDIF
                IF &mm="0708"
                    xlSheet.cells(k,i).value = "六级(副处)"
                ENDIF
                IF &mm="0707"
                    xlSheet.cells(k,i).value = "五级(正处)"
                ENDIF
                IF &mm="0706"
                    xlSheet.cells(k,i).value = "四级(副厅)"
                ENDIF
                IF &mm="0705"
                    xlSheet.cells(k,i).value = "三级(正厅)"
                ENDIF
                IF LEFT(&mm,2)="07" AND AT("F",&mm)>0
                    xlSheet.cells(k,i).value = "管理见习期"
                ENDIF
                IF &mm="1013"
                    xlSheet.cells(k,i).value = "十三级(员级)"
                ENDIF
                IF &mm="1012"
                    xlSheet.cells(k,i).value = "十二级(初级)"
                ENDIF
                IF &mm="1011"
                    xlSheet.cells(k,i).value = "十一级(初级)"
                ENDIF
                IF &mm="1010"
                    xlSheet.cells(k,i).value = "十级(中级)"
                ENDIF
                IF &mm="1009"
                    xlSheet.cells(k,i).value = "九级(中级)"
                ENDIF
                IF &mm="1008"
                    xlSheet.cells(k,i).value = "八级(中级)"
                ENDIF
                IF &mm="1007"
                    xlSheet.cells(k,i).value = "七级(副高)"
                ENDIF
                IF &mm="1006"
                    xlSheet.cells(k,i).value = "六级(副高)"
                ENDIF
                IF &mm="1005"
                    xlSheet.cells(k,i).value = "五级(副高)"
                ENDIF
                IF &mm="1004"
                    xlSheet.cells(k,i).value = "四级(正高)"
                ENDIF
                IF &mm="1003"
                    xlSheet.cells(k,i).value = "三级(正高)"
                ENDIF
                IF &mm="1002"
                    xlSheet.cells(k,i).value = "二级(正高)"
                ENDIF
                IF &mm="1001"
                    xlSheet.cells(k,i).value = "一级(正高)"
                ENDIF
                IF LEFT(&mm,2)="10" AND AT("F",&mm)>0
                    xlSheet.cells(k,i).value = "专技见习期"
                ENDIF
                
                IF &mm="0805"
                    xlSheet.cells(k,i).value = "五级(初级工)"
                ENDIF
                IF &mm="0804"
                    xlSheet.cells(k,i).value = "四级(中级工)"
                ENDIF
                IF &mm="0803"
                    xlSheet.cells(k,i).value = "三级(高级工)"
                ENDIF
                IF &mm="0802"
                    xlSheet.cells(k,i).value = "二级(技师)"
                ENDIF
                IF &mm="0801"
                    xlSheet.cells(k,i).value = "一级(高级技师)"
                ENDIF
                IF &mm="0901"
                    xlSheet.cells(k,i).value = "事业单位普通工"
                ENDIF
                IF LEFT(&mm,2)="08" AND AT("F",&mm)>0
                    xlSheet.cells(k,i).value = "事业单位学徒熟练期"
                ENDIF
                
            CASE mm="ZGXL" OR mm="DYXL"
                DO CASE
                case AT("博士",&mm)>0
                    xlSheet.cells(k,i).value = "博士研究生"
                case &mm="硕士研究生毕业" or &mm=="硕士研究生"
                    xlSheet.cells(k,i).value = "硕士研究生"
                case &mm="大学本科毕业" or &mm=="双学士学位大学本科"
                    xlSheet.cells(k,i).value = "本科"
                case &mm="大专毕业"
                    xlSheet.cells(k,i).value = "专科"
                case &mm="中专毕业"
                    xlSheet.cells(k,i).value = "中专"
                case &mm="中技毕业" OR &mm="技校毕业"
                    xlSheet.cells(k,i).value = "中技"
                CASE &mm="高中毕业" OR &mm="职高毕业"
                    xlSheet.cells(k,i).value = "高中"
                case &mm="初中毕业"
                    xlSheet.cells(k,i).value = "初中"
                case &mm="小学毕业"
                    xlSheet.cells(k,i).value = "小学"
                OTHERWISE
                    xlSheet.cells(k,i).value = ALLTRIM(&mm)
                ENDCASE
            CASE mm="ZZMM"
                xlSheet.cells(k,i).value = ALLTRIM(STRTRAN(&mm,'它','他'))
            OTHERWISE
                xlSheet.cells(k,i).value = ALLTRIM(&mm)
            ENDCASE 
        ENDIF
	ENDFOR

    k=k+1
ENDSCAN


&&导出年度考核结果信息
xlSheet = xlBook.Worksheets(3)
SELECT ndkh
k=2
SCAN
	FOR i=1 to FCOUNT("ndkh")
	    mm=alltrim(FIELD(i))
	    IF !EMPTY(mm)
		    IF TYPE(mm)="N"
		        IF &mm =0
		            LOOP
		        ENDIF
		    ENDIF

            IF mm="KHJG"
                IF AT("未定等次",&mm)>0
                    xlSheet.cells(k,i).value = "未定等次"
                ELSE
                    xlSheet.cells(k,i).value = ALLTRIM(STRTRAN(STRTRAN(&mm,"参加",''),"称职",'合格'))
                ENDIF
            ELSE
                xlSheet.cells(k,i).value = ALLTRIM(&mm)
            ENDIF
        ENDIF
	ENDFOR

    k=k+1
ENDSCAN


&&导出岗位聘任信息
xlSheet = xlBook.Worksheets(4)
SELECT zwbh
k=2
SCAN
	FOR i=1 to FCOUNT("zwbh")
	    mm=alltrim(FIELD(i))
	    IF !EMPTY(mm)
		    IF TYPE(mm)="N"
		        IF &mm =0
		            LOOP
		        ENDIF
		    ENDIF

            DO CASE
            CASE mm="ZWBM"
                IF LEFT(&mm,2)="07"
                    xlSheet.cells(k,i).value = "管理岗位"
                ENDIF
                IF LEFT(&mm,2)="08" OR LEFT(&mm,2)="09"
                    xlSheet.cells(k,i).value = "工勤岗位"
                ENDIF
                IF LEFT(&mm,2)="10"
                    xlSheet.cells(k,i).value = "专技岗位"
                ENDIF

                
            CASE mm="XZZW"
                IF &mm="070C"
                    xlSheet.cells(k,i).value = "十级(办事员)"
                ENDIF
                IF &mm="070B"
                    xlSheet.cells(k,i).value = "九级(科员)"
                ENDIF
                IF &mm="070A"
                    xlSheet.cells(k,i).value = "八级(副科)"
                ENDIF
                IF &mm="0709"
                    xlSheet.cells(k,i).value = "七级(正科)"
                ENDIF
                IF &mm="0708"
                    xlSheet.cells(k,i).value = "六级(副处)"
                ENDIF
                IF &mm="0707"
                    xlSheet.cells(k,i).value = "五级(正处)"
                ENDIF
                IF &mm="0706"
                    xlSheet.cells(k,i).value = "四级(副厅)"
                ENDIF
                IF &mm="0705"
                    xlSheet.cells(k,i).value = "三级(正厅)"
                ENDIF
                IF LEFT(&mm,2)="07" AND AT("F",&mm)>0
                    xlSheet.cells(k,i).value = "管理见习期"
                ENDIF
                IF &mm="1013"
                    xlSheet.cells(k,i).value = "十三级(员级)"
                ENDIF
                IF &mm="1012"
                    xlSheet.cells(k,i).value = "十二级(初级)"
                ENDIF
                IF &mm="1011"
                    xlSheet.cells(k,i).value = "十一级(初级)"
                ENDIF
                IF &mm="1010"
                    xlSheet.cells(k,i).value = "十级(中级)"
                ENDIF
                IF &mm="1009"
                    xlSheet.cells(k,i).value = "九级(中级)"
                ENDIF
                IF &mm="1008"
                    xlSheet.cells(k,i).value = "八级(中级)"
                ENDIF
                IF &mm="1007"
                    xlSheet.cells(k,i).value = "七级(副高)"
                ENDIF
                IF &mm="1006"
                    xlSheet.cells(k,i).value = "六级(副高)"
                ENDIF
                IF &mm="1005"
                    xlSheet.cells(k,i).value = "五级(副高)"
                ENDIF
                IF &mm="1004"
                    xlSheet.cells(k,i).value = "四级(正高)"
                ENDIF
                IF &mm="1003"
                    xlSheet.cells(k,i).value = "三级(正高)"
                ENDIF
                IF &mm="1002"
                    xlSheet.cells(k,i).value = "二级(正高)"
                ENDIF
                IF &mm="1001"
                    xlSheet.cells(k,i).value = "一级(正高)"
                ENDIF
                IF LEFT(&mm,2)="10" AND AT("F",&mm)>0
                    xlSheet.cells(k,i).value = "专技见习期"
                ENDIF
                
                IF &mm="0805"
                    xlSheet.cells(k,i).value = "五级(初级工)"
                ENDIF
                IF &mm="0804"
                    xlSheet.cells(k,i).value = "四级(中级工)"
                ENDIF
                IF &mm="0803"
                    xlSheet.cells(k,i).value = "三级(高级工)"
                ENDIF
                IF &mm="0802"
                    xlSheet.cells(k,i).value = "二级(技师)"
                ENDIF
                IF &mm="0801"
                    xlSheet.cells(k,i).value = "一级(高级技师)"
                ENDIF
                IF &mm="0901"
                    xlSheet.cells(k,i).value = "事业单位普通工"
                ENDIF
                IF LEFT(&mm,2)="08" AND AT("F",&mm)>0
                    xlSheet.cells(k,i).value = "事业单位学徒熟练期"
                ENDIF
            CASE mm="SRNY"
                xlSheet.cells(k,i).value = ALLTRIM(STRTRAN(&mm,'.'))
            OTHERWISE
                xlSheet.cells(k,i).value = ALLTRIM(&mm)
            ENDCASE
        ENDIF
	ENDFOR

    k=k+1
ENDSCAN


&&导出职员等级信息
xlSheet = xlBook.Worksheets(5)
SELECT zydj
k=2
SCAN
	FOR i=1 to FCOUNT("zydj")
	    mm=alltrim(FIELD(i))
	    IF !EMPTY(mm)
		    IF TYPE(mm)="N"
		        IF &mm =0
		            LOOP
		        ENDIF
		    ENDIF

            DO CASE
            CASE mm="XZZW"
                IF &mm="071C"
                    xlSheet.cells(k,i).value = "管理十级"
                ENDIF
                IF &mm="071B"
                    xlSheet.cells(k,i).value = "管理九级"
                ENDIF
                IF &mm="071A"
                    xlSheet.cells(k,i).value = "管理八级"
                ENDIF
                IF &mm="0719"
                    xlSheet.cells(k,i).value = "管理七级"
                ENDIF
                IF &mm="0718"
                    xlSheet.cells(k,i).value = "管理六级"
                ENDIF
            CASE mm="SRNY"
                xlSheet.cells(k,i).value = ALLTRIM(STRTRAN(&mm,'.'))
            OTHERWISE
                xlSheet.cells(k,i).value = ALLTRIM(&mm)
            ENDCASE
        ENDIF
	ENDFOR

    k=k+1
ENDSCAN

IF USED("dwinfo")
    USE IN dwinfo
ENDIF
IF USED("jbinfo")
    USE IN jbinfo
ENDIF
IF USED("zwbh")
    USE IN zwbh
ENDIF
IF USED("ndkh")
    USE IN ndkh
ENDIF
IF USED("zydj")
    USE IN zydj
ENDIF

*!*	*Setting the title
*!*	RangeString = xlsheet.cells(1,FldCnt).address
*!*	Rangestring ="$A$1:"+Rangestring
*!*	*!*	xlSheet.range(RangeString).select
*!*	*!*	xlSheet.range(RangeString).clear
*!*	*!*	xlSheet.range(RangeString).merge
*!*	xlSheet.cells(1,1).value = m.cTitle
*!*	*!*	xlSheet.cells(1,1).font.size = 18
*!*	*!*	xlSheet.cells(1,1).font.name = "黑体"
*!*	xlSheet.PageSetup.PrintTitleRows="$1:$"+ALLTRIM(STR(StartRow-1))
*!*	*!*	xlSheet.cells(1,1).HorizontalAlignment = 3
*!*	xlSheet.cells(2,3).value=ALLTRIM(dwmc(m.pdwbm))

*!*	RangeString = xlsheet.cells(2,FldCnt).address
*!*	Rangestring ="$A$2:"+Rangestring
*!*	*!*	xlSheet.range(RangeString).font.name="黑体"

*!*	SELECT (aTable)
*!*	k=0
*!*	SCAN
*!*	    xlSheet.cells(StartRow+k,1).value = (k+1+1)/2
*!*		FOR i=1 to FldCnt-1
*!*		    mm=alltrim(FIELD(i))
*!*		    IF !EMPTY(mm)
*!*			    IF TYPE(mm)="N"
*!*			        IF &mm =0
*!*			            LOOP
*!*			        ENDIF
*!*			    ENDIF

*!*	            xlSheet.cells(StartRow+k,i+1).value = &mm
*!*	        ENDIF
*!*		ENDFOR

*!*	    IF INT(k/2)=k/2
*!*			Rangestring ="$A$"+ALLTRIM(STR(StartRow+k))+":"+"$A$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$B$"+ALLTRIM(STR(StartRow+k))+":"+"$B$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$C$"+ALLTRIM(STR(StartRow+k))+":"+"$C$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$V$"+ALLTRIM(STR(StartRow+k))+":"+"$V$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$W$"+ALLTRIM(STR(StartRow+k))+":"+"$W$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*	    ENDIF

*!*	    k=k+1
*!*	ENDSCAN
*!*	Rangestring ="$A$"+ALLTRIM(STR(StartRow+k-2))+":"+"$C$"+ALLTRIM(STR(StartRow+k-1))
*!*	xlSheet.range(RangeString).select
*!*	xlSheet.range(RangeString).clear
*!*	xlSheet.range(RangeString).merge
*!*	xlSheet.cells(StartRow+k-2,1).value = "合计"
*!*	xlSheet.cells(StartRow+k-2,1).HorizontalAlignment = 3
*!*	xlSheet.cells(StartRow+k-2,1).VerticalAlignment = 2
*!*	FOR i=0 to FldCnt
*!*	    xlSheet.cells(StartRow+k-2,i+1).font.name = "黑体"
*!*	    xlSheet.cells(StartRow+k-2,i+1).font.bold = .t.
*!*	    xlSheet.cells(StartRow+k-1,i+1).font.name = "黑体"
*!*	    xlSheet.cells(StartRow+k-1,i+1).font.bold = .t.
*!*	ENDFOR
*!*		
*!*	*set the border's style and align mode
*!*	RangeString = xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*	RangeString ="$A$3:"+ RangeString
*!*	xlSheet.range(RangeString).borders(1).LineStyle = 7
*!*	xlSheet.range(RangeString).borders(2).LineStyle = 7
*!*	xlSheet.range(RangeString).borders(3).LineStyle = 7
*!*	xlSheet.range(RangeString).borders(4).LineStyle = 7
*!*	*xlSheet.range(RangeString).HorizontalAlignment = 3
*!*	*!*	xlSheet.range(RangeString).VerticalAlignment = 2
*!*	xlSheet.range("$A$"+ALLTRIM(STR(StartRow))+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address).Rowheight=14.25
*!*	xlSheet.range(RangeString).NumberFormatLocal = "@"
*!*	*xlSheet.columns(1).NumberFormatLocal = "@"
*!*	xlSheet.range(RangeString).font.name = "宋体"
*!*	xlSheet.range(RangeString).font.size = 9

*!*	RangeString = "$A$3:"+xlsheet.cells(3,FldCnt).address
*!*	xlSheet.range(RangeString).borders(3).LineStyle = 7&&12
*!*	RangeString = xlsheet.cells(RecCnt+StartRow-1,1).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*	xlSheet.range(RangeString).borders(4).LineStyle = 7&&12
*!*	RangeString = "$A$3:"+xlsheet.cells(RecCnt+StartRow-1,1).address
*!*	xlSheet.range(RangeString).borders(1).LineStyle = 7&&12
*!*	RangeString = xlsheet.cells(3,FldCnt).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*	xlSheet.range(RangeString).borders(2).LineStyle = 7&&12

xlApp.visible = .t.
RELEASE xlApp