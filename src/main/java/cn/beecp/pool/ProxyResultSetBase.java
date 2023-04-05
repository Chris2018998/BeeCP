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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static cn.beecp.pool.PoolStaticCenter.CLOSED_RSLT;

/**
 * ResultSet statement base class
 *
 * @author Chris Liao
 * @version 1.0
 */
abstract class ProxyResultSetBase extends ProxyBaseWrapper implements ResultSet {
    protected ResultSet raw;
    private ProxyStatementBase owner;//called by subclass to check close state

    public ProxyResultSetBase(ResultSet raw, PooledConnection p) {
        super(p);
        this.raw = raw;
    }

    public ProxyResultSetBase(ResultSet raw, ProxyStatementBase o, PooledConnection p) {
        super(p);
        o.setOpenResultSet(this);
        this.raw = raw;
        owner = o;
    }

    //***************************************************************************************************************//
    //                                             Below are self-define methods                                     //
    //***************************************************************************************************************//
    boolean containsRaw(ResultSet raw) {
        return this.raw == raw;
    }

    //***************************************************************************************************************//
    //                                              Below are override methods                                       //
    //***************************************************************************************************************//
    public Statement getStatement() throws SQLException {
        if (this.isClosed) throw new SQLException("No operations allowed after resultSet closed");
        return this.owner;
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    public final void close() throws SQLException {
        if (this.isClosed) return;
        try {
            this.isClosed = true;
            this.raw.close();
        } finally {
            this.raw = CLOSED_RSLT;
            if (this.owner != null) this.owner.removeOpenResultSet(this);
        }
    }
}
