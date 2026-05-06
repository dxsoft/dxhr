FUNCTION akhjg
PARAMETERS tcdwbm,tcgrbm,tcnd,lakhjg
EXTERNAL ARRAY lakhjg

oldalias=ALIAS()

csql="SELECT top 3 khnd,khjg FROM dndkh WHERE dwbm='"+tcdwbm+"' and grbm='"+tcgrbm+"' and khnd<'"+tcnd+"' order by khnd desc"
SQLEXEC(conn,csql,"ll")

SELECT ll
COPY TO ARRAY lakhjg
USE IN ll
SELECT (oldalias)
RETURN _tally
