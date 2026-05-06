FUNCTION jb

PARAMETERS tcJb,tcDjc

RETURN dtc(ALLTRIM(tcJb))+"¼¶"+IIF(EMPTY(tcDjc),"","µ¹"+dtc(ALLTRIM(tcDjc)))