FUNCTION agz

PARAMETERS tcRybm,lagz
EXTERNAL ARRAY lagz

SELECT hj2,zwgw2,jbgzjb2,zwgzdc2,zwgzse2,jbgzse2,jxgz,jsfszwtg2,jhljt,fdgz2,blfb2,jjjy2,gwjt2,qtbt,xckhndjb,xckhndzw,jsdjgz2,jxjt,dfbt2 FROM hisbase WHERE dwbm+grbm=tcrybm AND EMPTY(sid) INTO ARRAY lagz

RETURN _tally
