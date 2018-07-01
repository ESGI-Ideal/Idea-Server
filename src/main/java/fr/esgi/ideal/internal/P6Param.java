package fr.esgi.ideal.internal;

import lombok.NonNull;

import java.util.Optional;

/*
 * from https://github.com/p6spy/p6spy/blob/master/src/test/java/com/p6spy/engine/test/P6TestFramework.java
 * and https://github.com/p6spy/p6spy/tree/master/src/test/resources/com/p6spy/engine/spy/*
 */
//-Djdbc.drivers=
public enum P6Param {
    PostgreSQL("postgresql", "org.postgresql.Driver",
               null, "org.postgresql.xa.PGXADataSource", "org.postgresql.ds.PGConnectionPoolDataSource"),

    MySQL("mysql", "com.mysql.jdbc.Driver",
          "com.mysql.jdbc.jdbc2.optional.MysqlDataSource", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource",
           "yyyy-MM-dd"),
    MariaDB("mysql", "com.mysql.jdbc.Driver",
            "com.mysql.jdbc.jdbc2.optional.MysqlDataSource", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource",
            "yyyy-MM-dd"),
    Percona("mysql", "com.mysql.jdbc.Driver",
            "com.mysql.jdbc.jdbc2.optional.MysqlDataSource", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource",
            "yyyy-MM-dd"),

    //Oracle("oracle", null, "oracle.jdbc.pool.OracleDataSource", "oracle.jdbc.xa.client.OracleXADataSource", "oracle.jdbc.pool.OracleConnectionPoolDataSource"),
    SqlServer("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "com.microsoft.sqlserver.jdbc.SQLServerDataSource", "com.microsoft.sqlserver.jdbc.SQLServerXADataSource", "com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource"),

    SQLite("sqlite", "org.sqlite.JDBC"),
    /*
     * HSQLDB doesn't like default one "dd-MMM-yy", throws:
     * org.hsqldb.HsqlException: data exception: invalid datetime format
     *
     * relevant sources available on: http://hsqldb.svn.sourceforge.net/viewvc/hsqldb/base/trun/src/org/hsqldb/types/DateTimeType.java?revision=5307&view=markup
     *
     * please note, this is not used in tests (see LoggedSQLValidTest.isDateTimeSupported())
     * as I didn't figure out the correct pattern
     *databaseDialectDateFormat=???
     */
    HSQLDB("hsqldb", "org.hsqldb.jdbcDriver",
           "org.hsqldb.jdbc.JDBCDataSource", "org.hsqldb.jdbc.pool.JDBCXADataSource", "org.hsqldb.jdbc.pool.JDBCPooledDataSource"),
    H2("h2", "org.h2.Driver",
       "org.h2.jdbcx.JdbcDataSource", "org.h2.jdbcx.JdbcDataSource", "org.h2.jdbcx.JdbcDataSource",
       "yyyy-MM-dd"),
    /*
     * DB2 doesn't like default one "dd-MMM-yy", throws:
     * DB2 SQL Error: SQLCODE=-180, SQLSTATE=22007, SQLERRMC=null, DRIVER=4.13.80 due to error: com.ibm.db2.jcc.am.SqlDataException: DB2 SQL Error: SQLCODE=-180, SQLSTATE=22007, SQLERRMC=null, DRIVER=4.13.80
     */
    DB2("db2", "com.ibm.db2.jcc.DB2Driver",
        "com.ibm.db2.jcc.DB2DataSource", "com.ibm.db2.jcc.DB2XADataSource", null,
        "yyyy-MM-dd"),
    /*
     * DB2 doesn't like default one "dd-MMM-yy", throws:
     * ERROR 22007: The syntax of the string representation of a date/time value is incorrect.
     *
     * as the derby sources (https://svn.apache.org/repos/asf/db/derby/code/trunk/java/engine/org/apache/derby/iapi/types/SQLTimestamp.java) say:
     * Parse a timestamp or a date. DB2 allows timestamps to be used as dates or times. So
     * date('2004-04-15-16.15.32') is valid, as is date('2004-04-15').
     */
    DERBY("derby", "org.apache.derby.jdbc.ClientDriver",
            "org.apache.derby.jdbc.EmbeddedSimpleDataSource", "org.apache.derby.jdbc.EmbeddedXADataSource", "org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource",
            "yyyy-MM-dd-HH.mm.ss"),
    DERBY_EMBED("derby", "org.apache.derby.jdbc.EmbeddedDriver",
                "org.apache.derby.jdbc.EmbeddedSimpleDataSource", "org.apache.derby.jdbc.EmbeddedXADataSource", "org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource",
                "yyyy-MM-dd-HH.mm.ss"),
    FireBirdSQL("firebirdsql", "org.firebirdsql.jdbc.FBDriver",
                "org.firebirdsql.pool.FBConnectionPoolDataSource", "org.firebirdsql.ds.FBXADataSource","org.firebirdsql.pool.FBConnectionPoolDataSource")
    ;


    public final static String P6SpyEnvPrefix = "p6spy.config.";


    P6Param(@NonNull final String base, @NonNull final String driver,
                    final String ds_basic, final String ds_xa, final String ds_poll,
                    final String dateFormat) {
        this.URI_base = base;
        this.driverJDBC = driver;
        this.basicDS = Optional.ofNullable(ds_basic);
        this.xaDS = Optional.ofNullable(ds_xa);
        this.pollingDS = Optional.ofNullable(ds_poll);
        this.dateFormat = Optional.ofNullable(dateFormat);
    }

    P6Param(@NonNull final String base, @NonNull final String driver,
                    final String ds_basic, final String ds_xa, final String ds_poll) {
        this(base, driver, ds_basic, ds_xa, ds_poll, null);
    }

    P6Param(@NonNull final String base, @NonNull final String driver) {
        this(base, driver, null, null, null);
    }

    /**
     * Constant part in <code>jdbc:#cnst_id#:_url_</code>
     */
    public final String URI_base;

    /**
     * Driver JDBC class
     */
    public final String driverJDBC;

    /**
     * Driver DataSource class
     */
    public final Optional<String> basicDS;

    /**
     * xaDataSource
     */
    public final Optional<String> xaDS;

    /**
     * poolingDataSource
     */
    public final Optional<String> pollingDS;

    /**
     * Database Dialect Date Format
     */
    public final Optional<String> dateFormat;
}
