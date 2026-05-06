Func RPTPreview 
para cReportName,cTitleName 

#define ready_loc "預覽報表 ..." 
#define noRptFile_loc "報表文件&cReportName.不存在！" 
#define noData_loc "當前工作區沒有可共預覽的數據，終止！" 
#define RptExist_loc "是否替換原有的報表預覽？" 
#define error_loc "錯誤" 
#define NoRpt_loc "沒有報表可預覽" 
#define RptShut_loc "預覽報表已經關閉！" 
#define RptClose_loc "關閉打印預覽窗口？" 
#define Preview_name "報表名稱"
#define Time_Refresh 500 && 時鐘刷新頻率(微秒) 
set message to ready_loc 

#define isPicBtn .F. && 是否使用圖片按紐，如果為假時將采用文字按紐 
#define btn_width iif(isPicBtn,23,56) && 按紐的寬度 
#define btn_height iif(isPicBtn,22,25) && 按紐的高度 
#define printbz .T.

*-- 設定按紐的圖片及標題 
#define home_name iif(isPicBtn,"","首頁") 
#define home_pic iif(isPicBtn,"ico\btnhome.bmp","") 
#define prev_name iif(isPicBtn,"","上頁") 
#define prev_pic iif(isPicBtn,"ico\btnprev.bmp","") 
#define next_name iif(isPicBtn,"","下頁") 
#define next_pic iif(isPicBtn,"ico\btnnext.bmp","") 
#define end_name iif(isPicBtn,"","末頁") 
#define end_pic iif(isPicBtn,"ico\btnend.bmp","") 
#define print_name iif(isPicBtn,"","打印...") 
#define print_pic iif(isPicBtn,"ico\btnprint.bmp","") 
#define close_name iif(isPicBtn,"","關閉") 
#define close_pic iif(isPicBtn,"ico\btnclose.bmp","") 


if type("oPrvToolsBar")="O" and !isnull(oPrvToolsBar)&&-- 顯示預覽工具條 
&&-- 如果已存在打印工具條，提示信息及打開工具條上的檢查時鐘 
oPrvToolsBar.msg.rpt_info.caption = ready_loc 
oPrvToolsBar.ChkTimer.enabled = .T. 
else 
&&-- 如果不存在打印工具條，則創建該工具條 
public oPrvToolsBar && 定義工具條對象的全局變量 
oPrvToolsBar=CreateObject("preview") && 創建工具條 
oPrvToolsBar.msg.addobject("rpt_info","rpt_info") && 添加一個信息提示文本框 
oPrvToolsBar.dock(0) && 使工具條在屏幕的最上邊 
oPrvToolsBar.show && 顯示工具條 
endif 

*-- 得到正確的報表文件名和報表標題 
oPrvToolsBar.rptName = cReportName 
oPrvToolsBar.rptTitle = cTitleName 

*-- 定義預覽窗口 --*-- 這個窗口相當于一個容器，用來容納報表預覽窗口，預覽窗口的特征可以從這個窗口來繼承 --* 
DEFINE WINDOW WinRptPre ; 
FROM 0,0 TO SROWS()-10,SCOLS()-10 ; 
NOFLOAT SYSTEM NOCLOSE NOGROW NOZOOM NOMINIMIZE ;
TITLE Preview_name +": "+cTitleName 

report form &cReportName nocons environment&&-- 計算報表總頁數 
_pepage = _pageno && 將報表總頁數記錄在系統變量_pepage中， 
&& 注意該變量不可以小于等于0,否則系統出錯 
&& 當報表計算出來沒有可供顯示的內容時， 
&& 系統變量_pageno最小也是返回1,不會小于0 

TotalPages=_pageno && 此總頁數變量由用戶自定義,可以用到報表中顯示"共几頁"的形式 
&& 當使用該變量時，應實現用 public 來定義 

ZOOM WINDOW WinRptPre MAX &&-- 將預覽所要顯示的窗口最大化 

*-- 顯示報表，之前把原先有的先釋放！ 
if wexist(Preview_name) && 存在已有報表預覽窗口 
if messagebox(RptExist_loc,4+32,"詢問") = 7 && 詢問是否替換原有的窗口 
activate window Preview_name && 如果不替換，則激活原有的報表預覽窗口 
zoom window Preview_name max && 并將該窗口最大化 
winrptpre.alwaysontop=.t. 
retu .F. && 終止程序的繼續執行 
endif 
endif 

*-- 不管是否已經存在原有報表，首先釋放原有的報表預覽的窗口* 如果該窗口不存在的話，則不做任何動作 
release window Preview_name 

*-- 顯示預覽報表在剛才定義的窗口范圍內，并不等待，繼續執行，如沒有nowait參數，報表象一個模式窗口會停留不前， 
*-- 那么接下去的程序運行會因為沒有該報表的存在而產生錯誤,所以在這里這個NOWAIT很有必要，不可去掉！！！ 
REPORT FORM &cReportName PREVIEW WINDOW WinRptPre NOWAIT 

if wexist("打印預覽") &&-- 將系統打印預覽工具條移到屏幕右下角，并釋放她 
move window "打印預覽" to 100,100 
release window "打印預覽" 
endif 
if wexist("Print Preview") 
move window "Print Preview" to 100,100 
release window "Print Preview" 
endif 

release window WinRptPre &&-- 釋放原有的用來放置預覽窗口的"容器"窗口 

oPrvToolsBar.isInited = .T. &&-- 初始化工具條完成,并標記工具條上的一個是否已初始屬性為真. 

public pcRptName &&-- 定義報表文件名為全局變量 
pcRptName = cReportName 

set message to "預覽報表完成" 

Define Class rpt_info AS label &&-- 定義控件：用來顯示報表頁數狀態 
AutoSize = .T. 
Caption = "初始化報表 ..." 
Height = 16 
Left = 5 
Top = 6 
Width = 86 
ForeColor = RGB(0,0,0) 
Name = "rpt_info" 
Visible = .T. 
EndDefine 


DEFINE CLASS preview AS toolbar &&-- 定義控件：打印控制工具條 

Caption = "報表預覽打印控制" 
Name = "preview" 

*-- 對工具條新增以下屬性 
pageno = 0 
isinited = .F. && 是否初始完畢 
rptName = "" && 當前報表文件名 
rptTitle = norpt_loc && 當前報表名稱 
isRptTop = .T. && 報表是否最前 
isRptBot = .T. && 報表是否最尾 

ADD OBJECT separator1 AS separator && 加上工具條分隔欄 
ADD OBJECT separator2 AS separator 

ADD OBJECT msg AS container WITH ; 
Width = 110, ; 
Height = btn_height, ; 
SpecialEffect = 1, ; 
Name = "msg" 

ADD OBJECT separator5 AS separator 

ADD OBJECT cmdhome AS commandbutton WITH ; 
Height = btn_height, ; 
Width = btn_width, ; 
Caption = home_name, ; 
Name = "cmdHome", ; 
ToolTipText = "第一頁", ; 
Picture = home_pic 

ADD OBJECT cmdprev AS commandbutton WITH ; 
Height = btn_height, ; 
Width = btn_width, ; 
Caption = prev_name, ; 
Name = "cmdPrev", ; 
ToolTipText = "上一頁" , ; 
Picture = prev_pic 

ADD OBJECT separator7 AS separator 

ADD OBJECT cmdnext AS commandbutton WITH ; 
Height = btn_height, ; 
Width = btn_width, ; 
Caption = next_name, ; 
Name = "cmdNext", ; 
ToolTipText = "下一頁", ; 
Picture = next_pic 

ADD OBJECT cmdend AS commandbutton WITH ; 
Height = btn_height, ; 
Width = btn_width, ; 
Caption = end_name, ; 
Name = "cmdEnd", ; 
ToolTipText = "最后一頁" , ; 
Picture = end_pic 

ADD OBJECT separator9 AS separator 
ADD OBJECT separator10 AS separator 

ADD OBJECT cmdprint AS commandbutton WITH ; 
Height = btn_height, ; 
Width = btn_width, ; 
Caption = print_name, ; 
Name = "cmdPrint", ; 
ToolTipText = "報表打印", ; 
Picture = print_pic 

ADD OBJECT separator11 AS separator 
ADD OBJECT separator12 AS separator 

ADD OBJECT cmdclose AS commandbutton WITH ; 
Height = btn_height, ; 
Width = btn_width, ; 
Caption = close_name, ; 
Name = "cmdClose", ; 
ToolTipText = "關閉當前的報表", ; 
Picture = close_pic 

ADD OBJECT ChkTimer AS timer WITH ; &&-- 工具條的特殊之處是多了一個時鐘，用來檢查當前頁數，及其他的一些信息 
Interval = time_refresh , ; 
Name = "ChkTimer" 

*-- 控件添加完畢，下面開始控件過程 

PROCEDURE Init &&-- 工具條初始化 
this.ChkTimer.height=0 
this.ChkTimer.width=0 
this.cmdPrint.enabled=printbz 
this.refresh 
ENDPROC 

PROCEDURE cmdHome.Click &&-- 到首頁 
ACTI window Preview_name 
KEYBOARD '{HOME}' 
ENDPROC 

PROCEDURE cmdPrev.Click &&-- 上繙一頁 
ACTI window Preview_name 
KEYBOARD '{PGUP}' 
ENDPROC 

PROCEDURE cmdNext.Click &&-- 下繙一頁 
ACTI window Preview_name 
KEYBOARD '{PGDN}' 
ENDPROC 

PROCEDURE cmdEnd.Click &&-- 到頁尾 
ACTI window Preview_name 
KEYBOARD '{END}' 
ENDPROC 

PROCEDURE cmdprint.Click &&-- 打印選項 
do form dykz 
* do bb_prin with oPrvToolsBar.rptName,oPrvToolsBar.rptTitle &&-- 調用打印設置對話框，需要另一個程序的支持: RptPrint 
ENDPROC 

PROCEDURE cmdclose.Click &&-- 關閉報表預覽及打印工具條 
release window Preview_name &&-- 釋放預覽窗口 
this.parent.release &&-- 釋放打印控制工具條 
ENDPROC 

PROCEDURE Refresh &&-- 工具條刷新 

With this &&-- 刷新是對工具條上繙頁功能的可用不可用的控制 
do case 
case _pageno=1 and _pepage=1&&-- 當頁數處于第一，且總頁數也是只有一頁時，四者都不可用 
.isRptTop =.T. 
.isRptBot =.T. 
case _pageno=1 and _pepage>1&&-- 當頁數處于第一，且總頁數大于一頁時，前兩者不可用 
.isRptTop =.T. 
.isRptBot =.F. 
case _pageno>1 and _pageno<_pepage&&-- 當頁數在第一和最大之間時，四者都可用 
.isRptTop =.F. 
.isRptBot =.F. 
case _pageno>1 and _pageno=_pepage&&-- 當頁數在最后一頁時，后兩者不可用 
.isRptTop =.F. 
.isRptBot =.T. 
endcase 
if !this.isInited &&-- 可是當沒有初始化時，四者都不可用 
.isRptTop = .T. 
.isRptBot = .T. 
endif 
.cmdHome.enabled = !.isRptTop &&-- 甚至四個按紐的可用性，前兩個和后兩個分別協調一致 
.cmdPrev.enabled = !.isRptTop 
.cmdNext.enabled = !.isRptBot 
.cmdEnd. enabled = !.isRptBot 
Endwith 

ENDPROC 


PROCEDURE ChkTimer.Timer &&-- 時鐘刷新 

with this.parent 
if _pageno # .pageno and .isInited && -- 當報表預覽初始化，及改變了頁號時 
.pageno = _pageno && 當改變了報表的頁號后，重新改寫屬性.pageno的值 
.msg.rpt_info.caption = "第 "+allt(str(_pageno))+" 頁，共 "+allt(str(_pepage))+" 頁" 
.msg.width = .msg.rpt_info.width + 10 && 調整信息框的寬度使其寬度適中 
.refresh() && 刷新工具條，使重新改變按紐的可用性 
endif 
if !wexist(Preview_name) and .isInited && -- 當被預覽的報表不存在時 
set message to rptshut_loc 
.isInited = .F.&&-- 初始化失敗 
.setall("enabled",.F.,"commandbutton")&&-- 使全部按紐不可用. 
.cmdPrint.enabled=printbz&&-- 保留打印設置和關閉按紐的可用 
.cmdClose.enabled=.t. 
.msg.rpt_info.caption = noRpt_loc&&-- 顯示報表信息為沒有報表 
.rptTitle = noRpt_loc 
.pageno = 0&&-- 記錄當前頁數為0,以使下次判斷時，當前頁數和記錄的頁數始終不相等 
.ChkTimer.enabled = .F. &&-- 關閉時鐘的檢查 
endif 
endwith 

ENDPROC 

ENDDEFINE 
