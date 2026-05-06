FUNCTION sumname
PARAMETERS tczwbm

DO case
CASE LEFT(tczwbm,2)="01"
    RETURN "公务员小计"
CASE LEFT(tczwbm,2)="05"
    RETURN "机关技工小计"
CASE LEFT(tczwbm,2)="06"
    RETURN "机关普工小计"
CASE LEFT(tczwbm,2)="07"
    RETURN "职员小计"
CASE LEFT(tczwbm,2)="08"
    RETURN "事业技工小计"
CASE LEFT(tczwbm,2)="09"
    RETURN "事业普工小计"
CASE LEFT(tczwbm,2)>="10"
    RETURN "专技小计"
ENDCASE
