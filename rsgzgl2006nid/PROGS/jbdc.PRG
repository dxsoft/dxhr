FUNCTION jbdc

PARAMETERS tcjb,tcdc

DO case
CASE ALLTRIM(tcjb)=="1" AND VAL(tcdc)>6
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="2" AND VAL(tcdc)>7
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="3" AND VAL(tcdc)>8
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="4" AND VAL(tcdc)>9
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="5" AND VAL(tcdc)>10
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="6" AND VAL(tcdc)>11
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="7" AND VAL(tcdc)>11
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="8" AND VAL(tcdc)>11
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="9" AND VAL(tcdc)>11
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="10" AND VAL(tcdc)>11
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="11" AND VAL(tcdc)>12
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="12" AND VAL(tcdc)>13
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="13" AND VAL(tcdc)>14
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="14" AND VAL(tcdc)>14
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="15" AND VAL(tcdc)>14
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="16" AND VAL(tcdc)>14
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="17" AND VAL(tcdc)>13
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="18" AND VAL(tcdc)>13
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="19" AND VAL(tcdc)>12
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="20" AND VAL(tcdc)>11
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="21" AND VAL(tcdc)>10
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="22" AND VAL(tcdc)>9
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="23" AND VAL(tcdc)>8
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="24" AND VAL(tcdc)>8
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="25" AND VAL(tcdc)>7
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="26" AND VAL(tcdc)>6
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
CASE ALLTRIM(tcjb)=="27" AND VAL(tcdc)>6
    RETURN "6도돌"+ALLTRIM(STR(VAL(tcdc)-6))
OTHERWISE
    RETURN ALLTRIM(tcdc)+"도"
ENDCASE
