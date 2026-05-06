FUNCTION crtvryzwbhv

PARAMETERS ttUpdate,tcConn

IF USED("ryzwbh")
    USE IN ryzwbh
ENDIF

m=SQLEXEC(tcConn,"select * from (SELECT *, row_number() over(partition by dwbm,grbm order by srny) rk from (SELECT b.* FROM [gzjsgl].[dbo].[dryjbxx] a,[gzjsgl].[dbo].[dryzwbh] b where a.dwbm=b.dwbm and a.grbm=b.grbm and b.srny>a.srny and dwbm='"+m.pdwbm+"') a) t where rk=1","ryzwbh")

IF ttUpdate
	CURSORSETPROP("tables","dryzwbh","ryzwbh")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,zwbm,srny","ryzwbh")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbh")
	    fn=fn+","+FIELD(i,"ryzwbh")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryzwbh")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbh")
	    fn=fn+","+FIELD(i,"ryzwbh")+" dryzwbh."+FIELD(i,"ryzwbh")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryzwbh")

	CURSORSETPROP("sendupdates",.t.,"ryzwbh")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryzwbh
INDEX ON dwbm+grbm+xrzwbz TAG bmbz ADDITIVE
INDEX ON dwbm+grbm+zwbm+srny TAG bmny ADDITIVE
INDEX ON srny TAG srny ADDITIVE
INDEX ON dwbm+grbm+LEFT(zwbm,2)+srny TAG bmlbny ADDITIVE

SET ORDER TO srny IN ryzwbh