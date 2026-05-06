FUNCTION ayb
PARAMETERS tcdwbm,tcgrbm,lat
EXTERNAL ARRAY lat

oldalias=ALIAS()

csql="SELECT jsnf,jsyf,jslb,jbgzjb2,zwgzdc2 FROM hisbase WHERE dwbm='"+tcdwbm+"' and grbm='"+tcgrbm+"' and ((charindex(jslb,'正常级别正常档次级别滚动')>0 and jsnf>'2010') or (jsnf='2010' and jslb='警员套改')) and jsnf<'2015' order by jsnf,jsyf,jslb"
SQLEXEC(conn,csql,"ll")

SELECT ll
COPY TO ARRAY lat
USE IN ll
SELECT (oldalias)
RETURN _tally
