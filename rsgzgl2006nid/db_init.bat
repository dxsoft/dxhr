cd %~dp0

del %cd%\.ini
echo 删除完成
 
echo [backup]>> sys.ini
echo path=F:\>> sys.ini
echo basedir=%cd%>> sys.ini
echo auto=0>> sys.ini
echo ask=0>> sys.ini
echo chkupdate=1>> sys.ini
echo [server]>> sys.ini
echo name=.>> sys.ini
echo dbname=gzjsgl>> sys.ini
echo sys.ini生成成功