FUNCTION newid

IF UPPER(m.dbtype)="MYSQL"
    SQLEXEC(conn,'select uuid() as id','ll')
ELSE
    SQLEXEC(conn,'select newid() as id','ll')
ENDIF
RETURN ll.id