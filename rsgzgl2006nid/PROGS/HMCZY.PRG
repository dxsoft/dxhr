PARAMETERS v_jsnf,v_jsyf,v_jslb

v_dwjc=m.dwjc
SELECT ryjbxx  &&将当前花名册压栈
SCATTER MEMVAR
v_rybm=m.dwbm+m.grbm
SELECT hisbase
SEEK ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb ORDER tag bmlb IN hisbase
IF !FOUND("hisbase")
    INSERT INTO hisbase (yznf,yzyf,yzjslb) value (v_jsnf,v_jsyf,v_jslb)  
ENDIF
GATHER MEMVAR
REPLACE yznf WITH v_jsnf,yzyf WITH v_jsyf,yzjslb WITH v_jslb

SELECT ryjbxx &&将花名册右边左移
REPLACE jsnf with v_jsnf,jsyf with v_jsyf,jslb with v_jslb;
     zwbm1 WITH zwbm2,zwgw1 with zwgw2,zwgzdc1 with zwgzdc2,zwgzse1 with zwgzse2,jbgzjb1 with jbgzjb2,djc1 with djc2,;
     jx1 WITH jx, ;
     jbgzse1 with jbgzse2,jsdjgz1 with jsdjgz2,grjj1 with grjj2,;
     jcgz1 with jcgz2,glgz1 with glgz2,blfb1 with blfb2,jjjy1 with jjjy2,dfbt1 with dfbt2,;
     gwjt1 with gwjt2,hj1 with hj2,jsfszwtg1 with jsfszwtg2,jt1 with jt2,fdgz1 with fdgz2,;
     jxjt1 with jxjt,zwjt1 with zwjt,jxgz1 with jxgz,nzgwsf1 with nzgwsf,dsznf1 with dsznf,;
     sdbt1 with sdbt,qtbt1 with qtbt,zfbt1 with zfbt,jzmcbt1 with jzmcbt,jsqtbny with jshtbny,;
     pgbc1 WITH pgbc 

REPLACE bbz with '申报'
m.dwjc=v_dwjc
