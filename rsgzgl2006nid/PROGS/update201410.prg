PROCEDURE update201410

SELECT dryjbxx
SCAN
    INSERT INTO  hisbase SELECT top 1 * FROM hisbase WHERE dwbm=dryjbxx.dwbm AND grbm=dryjbxx.grbm AND jsnf+jsyf<="201410" order by jsnf DESC
    SELECT hisbase
    REPLACE jslb WITH "µ÷±ê½úÉý",jsnf WITH "2014",jsyf WITH "10",tbnd with "201410",zwbm1 WITH zwbm2,zwgw1 WITH  zwgw2,zwgzse1 WITH zwgzse2,jbgzse1 WITH jbgzse2,zwgzdc1 WITH zwgzdc2,jbgzjb1 WITH jbgzjb2,jsdjgz1 WITH jsdjgz2,jsfszwtg1 WITH jsfszwtg2,fdgz1 WITH fdgz2,hj1 WITH hj2
    SELECT dryjbxx
ENDSCAN

SELECT hisbase
REPLACE ALL tbnd1 WITH "201410",tbnd WITH "201410" FOR jsnf+jsyf>"201410"
GO TOP IN hisbase
SCAN
    gzjs06h()
ENDSCAN
