FUNCTION zzsj
PARAMETERS tcRyfl,tcCjgzsj

DO case
CASE tcRyfl="机关技术工人"
    RETURN STR(VAL(LEFT(tcCjgzsj,4))+1,4)+SUBSTR(tcCjgzsj,5,3)

CASE tcRyfl="事业技术工人"
    RETURN STR(VAL(LEFT(tcCjgzsj,4))+1,4)+SUBSTR(tcCjgzsj,5,3)

OTHERWISE
    RETURN STR(VAL(LEFT(tcCjgzsj,4))+1,4)+SUBSTR(tcCjgzsj,5,3)

ENDCASE
