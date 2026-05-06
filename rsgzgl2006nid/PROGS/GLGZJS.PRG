wait window at 20,60 '正在计算工龄工资，请等待！...' nowait
sele jcglgzbz
go bott
v_glgzbz=glgz
sele ryjbxx
if thisform.c1.op1.value=1
   set filt to
else
   set filt to dwbm=dwbm.dwbm
endi
sele gzbz
go top
v_tbnd=tbnd
sele ryjbxx
scan 
     v_gl=val(left(m.rq,4))-val(left(cjgzny,4))+1-zdgznx       
     if v_gl>0
        repl gznx with v_gl
     endi    
     if ryfl='公务员' and gznx>0 and !empt(zwgw2) and zwgw2<>'见习生'
        v_glgz=v_glgzbz*gznx
        x=at('*',ryjbxx.byyx,1)
        if x>0
           v_glzj=val(subs(byyx,x+1,2))
           v_glgz=v_glgz+v_glzj
        endi    
        if !empt(zwgw1) and zwgw1<>'见习生'
           repl hj1 with hj1-glgz1+v_glgz
           repl glgz1 with v_glgz
        else
          repl glgz1 with 0,hj1 with hj1-glgz1
        endi
        repl hj2 with hj2-glgz2+v_glgz
        repl glgz2 with v_glgz
     else
        repl hj2 with hj2-glgz2
        repl glgz2 with 0
     endi
ends
sele ryjbxx
set filt to
wait clear
do form ts1 with '计算完毕'
