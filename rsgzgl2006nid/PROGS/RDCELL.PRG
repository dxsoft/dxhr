if !used('bbdy') or !used('bbmc') or type('c_gzxm')<>'C'
   retu
endi
if bbmc.flbm='spb'
   v_maxls=20
else
   v_maxls=50
endi
v_sheet=this.getcursheet()
this.setcursheet(v_sheet)
   sele bbdy
   dele for bbdm=bbmc.bbdm 
	   for j=1 to 50 &&行号
	       for i=1 to v_maxls &&列号
	    	   x=this.getcellstring(i,j,v_sheet)
		       if !empt(x) && 查找字符区
                  x=allt(x)
                  y=left(x,3)
                  sele gzxmdm
                  loca for jsqzddm=y or jshzddm=y
                  if found() 
                     insert into bbdy (bbdm,hh,lh,jsgs) value(bbmc.bbdm,j,i,x)        
                  endi   
	    	   endi
	       endf
	   endf &&一页结尾
proc mouselclick
if type('c_gzxm')='C' and !empt(c_gzxm)
   this.setcellstring(col,row,this.getcursheet(),c_gzxm)
endi
retu