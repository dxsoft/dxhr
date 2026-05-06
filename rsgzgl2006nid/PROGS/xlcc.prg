FUNCTION xlcc

PARAMETERS tcxlmc
DO case
CASE INLIST(tcxlmc,"博士学位研究生毕业")
    RETURN '1'
CASE INLIST(tcxlmc,"硕士学位研究生毕业")
    RETURN '2'
CASE INLIST(tcxlmc,"研究生班毕业","无学位研究生","研究生结业","研究生肄业","双学士学位大学本科","6年制以上大学毕业")
    RETURN '3'
CASE INLIST(tcxlmc,"大学本科毕业","大学本科结业","相当大学毕业","大学肄业","大普")
    RETURN '4'
CASE INLIST(tcxlmc,"大专毕业","大专结业","相当大专毕业","大专肄业")
    RETURN '5'
OTHERWISE
    RETURN '5'
ENDCASE
