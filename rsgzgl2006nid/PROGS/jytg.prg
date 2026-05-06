FUNCTION jytg

PARAMETERS tcjb,tcdc,tcyzwbm,tczwbm

&&2018年警员套改
**套改等级等于原级别-7，套改后等级未达到套改后职务对应最低等级的，进到最低等级，否则等级不变，这只是对平套情况，实际情况有套改滞后，套改后职务层次高于原职务的职务层次的，应该晋升1级，且不受最低等级影响（先平套再晋级）
**涉及如下几种情况
**0207、01C0—>2111（2112）
**0206、01B0->2110、2109（2111）
**0204、0205、01A1、01A0->2108、2107（）
**0203、0191、0190->2106、2105
**0202、0181、0180->2104、2103
**0201、0171、0170->2102

**先判断是否达到平套职务的最低级别，再晋级，从各职务最低级别分析，如果未达平套职务最低级别，必定不达套改职务最低级别，分两种情况：1不达平套职务最低级别，进最低，此最低仍低于套改职务最低级别，进到套改职务最低级别；2、已达平套职务最低级别，未达套改职务最低级别，进到套改职务最低级别；3、已达套改职务最低级别，进1级
**结论：未达套改职务最低级别，进最低级别，已达最低级别，进1级

&&返回：级别+档次+是否超档差
LOCAL zddj
m.zddj=PADL(ALLTRIM(STR(VAL(LEFT(jbscope(tczwbm),2))+7)),2,'0')

IF VAL(tcjb)>VAL(zddj)&&未达最低，进到最低
    RETURN PADL(ALLTRIM(STR(VAL(m.zddj)-7)),2,' ')+jbjs06(tcjb,tcdc,zddj,'201807')
ELSE
    IF (INLIST(tcyzwbm,"0207","01C0") and tczwbm="2111");
     or (INLIST(tcyzwbm,"0206","01B0") and INLIST(tczwbm,"2110","2109"));
      or (INLIST(tcyzwbm,"0204","0205","01A1","01A0") and INLIST(tczwbm,"2108","2107"));
       or (INLIST(tcyzwbm,"0203","0191","0190") and INLIST(tczwbm,"2106","2105"));
        or (INLIST(tcyzwbm,"0202","0181","0180") and INLIST(tczwbm,"2104","2103"));
         or (INLIST(tcyzwbm,"0201","0171","0170") and tczwbm="2102")
        RETURN PADL(ALLTRIM(STR(VAL(tcjb)-8)),2,' ')+jbjs06(PADL(ALLTRIM(STR(VAL(tcjb)-7)),2,' '),tcdc,PADL(ALLTRIM(STR(VAL(tcjb)-8)),2,' '),'201807')
    ELSE
        RETURN PADL(ALLTRIM(STR(VAL(tcjb)-7)),2,' ')+PADL(ALLTRIM(tcdc),2,' ')+'0'
    ENDIF
ENDIF
