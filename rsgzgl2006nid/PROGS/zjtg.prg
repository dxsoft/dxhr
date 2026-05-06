FUNCTION zjtg

PARAMETERS tcjb,tcdc,tcyzwbm,tczwbm

&&返回：级别+档次+是否超档差
&&未达最低级别，进最低，档次就近就高确定，在级别范围内的，级别不变
LOCAL zddj
m.zddj=PADL(ALLTRIM(STR(VAL(LEFT(jbscope(tczwbm),2)))),2,'0')

IF VAL(tcjb)>VAL(zddj)&&未达最低，进最低
    RETURN PADL(ALLTRIM(STR(VAL(m.zddj))),2,' ')+jbjs06(tcjb,tcdc,zddj,'201807')
ELSE&&已达最低，直接高套到上一职务层次，即使在级别范围内的也应该晋升一个级别（先套改、再晋升）
    IF (tcyzwbm='01C0' AND (tczwbm='2311' OR tczwbm='2411' OR tczwbm='2511' OR tczwbm='2611' OR tczwbm='2611' OR tczwbm='2611')) ;
    OR (tcyzwbm='01B0' AND (tczwbm='2310' OR tczwbm='2410' OR tczwbm='2510' OR tczwbm='2610' OR tczwbm='2610' OR tczwbm='2610')) ;
    OR (LEFT(tcyzwbm,3)='01A' AND (tczwbm='2308' OR tczwbm='2408' OR tczwbm='2508' OR tczwbm='2608' OR tczwbm='2608' OR tczwbm='2608')) ;
    OR (LEFT(tcyzwbm,3)='019' AND (tczwbm='2306' OR tczwbm='2406' OR tczwbm='2506' OR tczwbm='2606' OR tczwbm='2606' OR tczwbm='2606')) ;
    OR (LEFT(tcyzwbm,3)='018' AND (tczwbm='2304' OR tczwbm='2404' OR tczwbm='2504' OR tczwbm='2604' OR tczwbm='2604' OR tczwbm='2604')) ;
    OR (LEFT(tcyzwbm,3)='017' AND (tczwbm='2302' OR tczwbm='2402' OR tczwbm='2502' OR tczwbm='2602' OR tczwbm='2602' OR tczwbm='2602')) ;
    OR (INLIST(LEFT(tcyzwbm,2),'23','24','25','26','27','28') AND INLIST(left(tczwbm,2),'23','24','25','26','27','28') AND ((RIGHT(tcyzwbm,2)='12' AND RIGHT(tczwbm,2)='11') OR (RIGHT(tcyzwbm,2)='11' AND RIGHT(tczwbm,2)='10') OR (RIGHT(tcyzwbm,2)='10' AND RIGHT(tczwbm,2)='08') OR (RIGHT(tcyzwbm,2)='09' AND RIGHT(tczwbm,2)='08') OR (RIGHT(tcyzwbm,2)='07' AND RIGHT(tczwbm,2)='06') OR (RIGHT(tcyzwbm,2)='05' AND RIGHT(tczwbm,2)='04') OR (RIGHT(tcyzwbm,2)='03' AND RIGHT(tczwbm,2)='02'))) &&经分析，这几种高套（职务层次变化了），级别晋一级
        m.zddj=PADL(ALLTRIM(STR(VAL(tcjb)-1)),2,'0')
        RETURN PADL(ALLTRIM(STR(VAL(m.zddj))),2,' ')+jbjs06(tcjb,tcdc,zddj,'201807')
    ENDIF
ENDIF

RETURN PADL(ALLTRIM(STR(VAL(tcjb))),2,' ')+PADL(ALLTRIM(tcdc),2,' ')+'0'
