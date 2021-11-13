/*
 * Copyright(C) Chris2018998
 * Contact:Chris2018998@tom.com
 *
 * Licensed under GNU Lesser General Public License v2.1
 */
package cn.beecp.pool;

import cn.beecp.RawConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static cn.beecp.pool.PoolStaticCenter.isBlank;

/**
 * Datasource ConnectionFactory
 *
 * @author Chris.liao
 * @version 1.0
 */
public class DataSourceConnectionFactory implements RawConnectionFactory {
    //username
    private String username;
    //password
    private String password;
    //usernameIsNotNull
    private boolean usernameIsNotNull;
    //driverDataSource
    private DataSource driverDataSource;

    //Constructor
    public DataSourceConnectionFactory(DataSource driverDataSource, String username, String password) {
        this.driverDataSource = driverDataSource;
        this.username = username;
        this.password = password;
        if (!isBlank(username))
            usernameIsNotNull = true;
    }

    //create one connection
    public final Connection create() throws SQLException {
        if (usernameIsNotNull) {
            return driverDataSource.getConnection(username, password);
        } else {
            return driverDataSource.getConnection();
        }
    }
}
