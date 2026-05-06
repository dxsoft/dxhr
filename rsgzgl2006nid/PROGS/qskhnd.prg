FUNCTION qskhnd
PARAMETERS tcdwbm,tcgrbm

oldalias=ALIAS()

csql="SELECT top 1 xckhndjb+xckhndzw as nd FROM hisbase WHERE dwbm='"+tcdwbm+"' and grbm='"+tcgrbm+"' and  jsnf<'2015' order by jsnf desc,jsyf desc into cursor ll"
&csql

SELECT ll
LOCAL qskhnd
qskhnd=ll.nd
USE IN ll
SELECT (oldalias)
RETURN qskhnd
