/*
 * Copyright(C) Chris2018998
 * Contact:Chris2018998@tom.com
 *
 * Licensed under GNU Lesser General Public License v2.1
 */
package cn.beecp.test.mock;

import cn.beecp.pool.xa.MockXaResource;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * XaConnection
 *
 * @author Chris.Liao
 * @version 1.0
 */
public final class MockXaConnection implements XAConnection {
    private final Connection con;
    private final MockXaResource res;

    public MockXaConnection(Connection con, MockXaResource res) {
        this.con = con;
        this.res = res;
    }

    public void close() throws SQLException {
        con.close();
    }

    public Connection getConnection() throws SQLException {
        return con;
    }

    public XAResource getXAResource() throws SQLException {
        return res;
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
    }

    public void addStatementEventListener(StatementEventListener listener) {
    }

    public void removeStatementEventListener(StatementEventListener listener) {
    }
}