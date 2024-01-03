/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Copyright(C) Chris2018998,All rights reserved.
 *
 * Project owner contact:Chris2018998@tom.com.
 *
 * Project Licensed under GNU Lesser General Public License v2.1.
 */
package cn.beecp.pool;

import cn.beecp.RawConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static cn.beecp.pool.ConnectionPoolStatics.isBlank;

/**
 * Connection factory Implementation with a JDBC DataSource
 *
 * @author Chris liao
 * @version 1.0
 */
public final class ConnectionFactoryByDriverDs implements RawConnectionFactory {
    //username
    private final String username;
    //password
    private final String password;
    //usernameIsNotNull
    private final boolean useUsername;
    //driverDataSource
    private final DataSource driverDataSource;

    //Constructor
    public ConnectionFactoryByDriverDs(DataSource driverDataSource, String username, String password) {
        this.driverDataSource = driverDataSource;
        this.username = username;
        this.password = password;
        this.useUsername = !isBlank(username);
    }

    //return a connection when creates successful,otherwise,throws a failure exception
    public final Connection create() throws SQLException {
        return this.useUsername ? this.driverDataSource.getConnection(this.username, this.password) : this.driverDataSource.getConnection();
    }
}
