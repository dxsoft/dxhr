FUNCTION xlmc

PARAMETERS tcxlmc
DO case
CASE INLIST(tcxlmc,"研究生班毕业","无学位研究生","研究生结业","研究生肄业","双学士学位大学本科","6年制以上大学毕业")
    RETURN '大学本科  '
CASE AT('本科',tcxlmc)>0
    RETURN '大学本科  '
CASE AT('研究生',tcxlmc)>0
    RETURN '研究生    '
CASE AT('大专',tcxlmc)>0 OR AT('专科',tcxlmc)>0 OR AT('大学专科',tcxlmc)>0
    RETURN '大学专科  '
OTHERWISE
    RETURN '中专及以下'
ENDCASE
