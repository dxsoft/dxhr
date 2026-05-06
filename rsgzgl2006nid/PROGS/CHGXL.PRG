PROCEDURE chgxl
PARAMETERS tcxl

DO case
CASE tcxl="博士"
    RETURN "11博士学位研究生毕业"

CASE tcxl="硕士"
    RETURN "12硕士学位研究生毕业"

CASE tcxl="研究生"
    RETURN "13研究生班毕业"

CASE tcxl="双学士"
    RETURN "21双学士学位大学本科"

CASE tcxl="本科"
    RETURN "23大学本科毕业"

CASE tcxl="大普"
    RETURN "28相当大学毕业"

CASE tcxl="专科"
    RETURN "31大专毕业"

CASE tcxl="中专"
    RETURN "41中专毕业"

CASE tcxl="中技"
    RETURN "51技校毕业"

CASE tcxl="职高"
    RETURN "61职高毕业"

CASE tcxl="高中"
    RETURN "62高中毕业"

CASE tcxl="初中"
    RETURN "71初中毕业"

CASE tcxl="小学"
    RETURN "79小学以下"

OTHERWISE
    RETURN SPACE(18)
ENDCASE
