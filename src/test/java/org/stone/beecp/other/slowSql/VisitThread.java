/*
 * Copyright(C) Chris2018998
 * Contact:Chris2018998@tom.com
 *
 * Licensed under GNU Lesser General Public License v2.1
 */
package org.stone.beecp.other.slowSql;

import org.stone.beecp.BeeDataSource;

import java.sql.Connection;
import java.util.concurrent.CyclicBarrier;

public class VisitThread extends Thread {
    private BeeDataSource ds;
    private CyclicBarrier barrier;

    VisitThread(BeeDataSource ds, CyclicBarrier barrier) {
        this.ds = ds;
        this.barrier = barrier;
    }

    public void run() {
        Connection con = null;
        try {
            con = ds.getConnection();
            barrier.await();
        } catch (Exception e) {
        } finally {
            if (con != null) try {
                con.close();
            } catch (Exception e) {
            }
        }
    }
}
