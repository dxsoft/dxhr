FUNCTION jsbf
&&计算补发工资

PARAMETERS tdwbm,tgrbm

*!*	IF tdwbm="02601" AND tgrbm="00146"
*!*	aaaa=1
*!*	ENDIF

LOCAL jbgz,jt,ys,ze,ki,retv,y1,m.start,m.zwbm,m.latgz

DIMENSION latgz[1]

m.ys=0
m.ze=0
m.jbgz=0
m.jt=0
m.retv=""
m.y1=0
m.start="201410"
m.zwbm=""

SELECT dwbm,grbm,zwbm2,zwgzdc2,jbgzjb2,djc2,tgbl,jbtbz,jbtbz1,jsnf,jsyf,zwbm1,jslb,zwgzse2+jbgzse2+jxgz+jsdjgz2+jsfszwtg2+fdgz2+dfbt2+blfb2+jjjy2+jxjt+qtbt+gwjt2+jhljt+tgblbf+njbt as hj20,zwgzse1+jbgzse1+jxgz1+jsdjgz1+jsfszwtg1+fdgz1+dfbt1+blfb1+jjjy1+jxjt1+qtbt1+gwjt1+jhljt1+tgblbf1+njbt1 as hj10 FROM hisbase WHERE dwbm=tdwbm AND grbm=tgrbm ORDER BY jsnf,jsyf,hj2 INTO ARRAY latgz

IF _tally>0
    m.zze=0
	FOR i=1 TO ALEN(latgz,1)
	    IF latgz[i,10]="2014" AND latgz[i,11]="10" AND latgz[i,13]="调标晋升"
			m.zwgzse2 = zwgz06_gr(latgz[i,3],latgz[i,4],"201410")+zwgz06(latgz[i,3],"201410")-zwgz06_gr(latgz[i,3],latgz[i,4],"200607")-zwgz06(latgz[i,3],"200607")
		    m.jbgzse2 = IIF(INLIST(LEFT(latgz[i,3],2),"01","02","03"),jbgz06(latgz[i,5],ALLTRIM(STR(VAL(latgz[i,4])+VAL(latgz[i,6]))),"201410"),0)+xjgz06(latgz[i,4],latgz[i,6],"201410",latgz[i,3])-IIF(INLIST(LEFT(latgz[i,3],2),"01","02","03"),jbgz06(latgz[i,5],ALLTRIM(STR(VAL(latgz[i,4])+VAL(latgz[i,6]))),"200607"),0)-xjgz06(latgz[i,4],latgz[i,6],"200607",latgz[i,3])
			m.jsdjgz2 = jsdjgz06(latgz[i,3],"201410")-jsdjgz06(latgz[i,3],"200607")
			m.jxgz = 0 
			m.jsfszwtg2 = INT((m.zwgzse2+m.jbgzse2)*latgz[i,7]/100)
			m.jsdjgz2 = 0
			
		    m.fdgz2 = 0
		    m.dfbt2 = INT(jcjx(latgz[i,1],latgz[i,3],latgz[i,8])-jcjx(latgz[i,1],latgz[i,3],"201401"))
		    m.blfb2 = blfb(latgz[i,3])-blfb(latgz[i,12])
		    
		    m.zze=m.zwgzse2+m.jbgzse2+m.jsdjgz2+m.jsfszwtg2+m.blfb2+m.dfbt2
		    m.start="201410"
		    m.ze=0
        ELSE
            m.ze=m.ze+m.zze*((VAL(latgz[i,10])-VAL(LEFT(m.start,4)))*12+VAL(latgz[i,11])-VAL(SUBSTR(m.start,5)))
			m.zwgzse2 = zwgz06_gr(latgz[i,3],latgz[i,4],"201410")+zwgz06(latgz[i,3],"201410")-zwgz06_gr(latgz[i,3],latgz[i,4],"200607")-zwgz06(latgz[i,3],"200607")
		    m.jbgzse2 = IIF(INLIST(LEFT(latgz[i,3],2),"01","02","03"),jbgz06(latgz[i,5],ALLTRIM(STR(VAL(latgz[i,4])+VAL(latgz[i,6]))),"201410"),0)+xjgz06(latgz[i,4],latgz[i,6],"201410",latgz[i,3])-IIF(INLIST(LEFT(latgz[i,3],2),"01","02","03"),jbgz06(latgz[i,5],ALLTRIM(STR(VAL(latgz[i,4])+VAL(latgz[i,6]))),"200607"),0)-xjgz06(latgz[i,4],latgz[i,6],"200607",latgz[i,3])
			m.jsdjgz2 = jsdjgz06(latgz[i,3],"201410")-jsdjgz06(latgz[i,3],"200607")
			m.jxgz = 0 
			m.jsfszwtg2 = INT((m.zwgzse2+m.jbgzse2)*latgz[i,7]/100)
			m.jsdjgz2 = 0
			
		    m.fdgz2 = 0
		    m.dfbt2 = INT(jcjx(latgz[i,1],latgz[i,3],latgz[i,8])-jcjx(latgz[i,1],latgz[i,3],"201401"))
		    m.blfb2 = blfb(latgz[i,3])-blfb(latgz[i,12])
		    
		    m.zze=m.zwgzse2+m.jbgzse2+m.jsdjgz2+m.jsfszwtg2+m.blfb2+m.dfbt2
		    
		    m.start=latgz[i,10]+latgz[i,11]
        ENDIF        
	ENDFOR
    m.ze=m.ze+m.zze*((2017-VAL(LEFT(m.start,4)))*12+1-VAL(SUBSTR(m.start,5)))
ENDIF
