package fr.esgi.ideal.internal;

import lombok.AllArgsConstructor;
import org.jooq.SQLDialect;

@AllArgsConstructor
public enum  SqlParam {
    DERBY(SQLDialect.DERBY, P6Param.DERBY),
    DERBY_EMBED(SQLDialect.DERBY, P6Param.DERBY_EMBED),

    FireBird(SQLDialect.FIREBIRD, P6Param.FireBirdSQL),
    FireBird_2_5(SQLDialect.FIREBIRD_2_5, P6Param.FireBirdSQL),
    FireBird_3_0(SQLDialect.FIREBIRD_3_0, P6Param.FireBirdSQL),

    H2(SQLDialect.H2, P6Param.H2),

    HSQLDB(SQLDialect.HSQLDB, P6Param.HSQLDB),

    SQLite(SQLDialect.SQLITE, P6Param.SQLite),

    MySQL(SQLDialect.MYSQL, P6Param.MySQL),
    MySQL_5_7(SQLDialect.MYSQL_5_7, P6Param.MySQL),
    MySQL_8_0(SQLDialect.MYSQL_8_0, P6Param.MySQL),

    MariaDB(SQLDialect.MARIADB, P6Param.MariaDB),

    Postgres(SQLDialect.POSTGRES, P6Param.PostgreSQL),
    Postgres_9_3(SQLDialect.POSTGRES_9_3, P6Param.PostgreSQL),
    Postgres_9_4(SQLDialect.POSTGRES_9_4, P6Param.PostgreSQL),
    Postgres_9_5(SQLDialect.POSTGRES_9_5, P6Param.PostgreSQL),
    Postgres_10(SQLDialect.POSTGRES_10, P6Param.PostgreSQL),
;

    public final SQLDialect jooqSqlDialect;
    public final P6Param p6spyParams;
}
/*
@Deprecated SQL99(),
DEFAULT(),
CUBRID()

Percona
SqlServer
Oracle
DB2
*/
