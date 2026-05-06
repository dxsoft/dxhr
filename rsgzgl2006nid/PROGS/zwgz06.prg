&&计算职务工资
FUNCTION zwgz06

PARAMETERS cZwbm,cTbnd

&&转换司法辅助类到对应的综合管理类
DO CASE
CASE czwbm="0416" OR czwbm="0426"
    czwbm="0161"
CASE czwbm="0417" OR czwbm="0427" OR czwbm="0437"
    czwbm="0171"
CASE czwbm="0418" OR czwbm="0428" OR czwbm="0438"
    czwbm="0181"
CASE czwbm="0419" OR czwbm="0429" OR czwbm="0439"
    czwbm="0191"
CASE czwbm="041A" OR czwbm="042A" OR czwbm="043A"
    czwbm="01A1"
CASE czwbm="041B" OR czwbm="042B" OR czwbm="043B"
    czwbm="01B0"
CASE czwbm="043C"
    czwbm="01C0"
ENDCASE    
    
IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 00000
ENDIF

IF EMPTY(cZwbm)
    RETURN 00000
ENDIF

oldalias=ALIAS()
SELECT bz06_zwgz
LOCATE FOR tbnd=ALLTRIM(cTbnd) AND zwbm=ALLTRIM(cZwbm)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("bz06_zwgz")
    RETURN bz06_zwgz.bz
ELSE
    RETURN 00000
ENDIF
