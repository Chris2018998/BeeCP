/*
 * Copyright(C) Chris2018998
 * Contact:Chris2018998@tom.com
 *
 * Licensed under GNU Lesser General Public License v2.1
 */
package org.stone.beecp.pool;

import junit.framework.TestCase;
import org.stone.base.TestUtil;
import org.stone.beecp.BeeDataSource;
import org.stone.beecp.BeeDataSourceConfig;
import org.stone.beecp.JdbcConfig;

public class PoolRestartNewConfigTest extends TestCase {
    private final int initSize = 5;
    private BeeDataSource ds;

    public void setUp() {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        config.setJdbcUrl(JdbcConfig.JDBC_URL);
        config.setDriverClassName(JdbcConfig.JDBC_DRIVER);
        config.setUsername(JdbcConfig.JDBC_USER);
        config.setPassword(JdbcConfig.JDBC_PASSWORD);
        config.setInitialSize(initSize);
        ds = new BeeDataSource(config);
    }

    public void tearDown() {
        ds.close();
    }

    public void test() throws Exception {
        FastConnectionPool pool = (FastConnectionPool) TestUtil.getFieldValue(ds, "pool");
        TestUtil.assertError("Total connections expected:%s,current is:%s", initSize, pool.getTotalSize());
        TestUtil.assertError("connections expected:%s,current is:%s", initSize, pool.getIdleSize());
        pool.clear(false);

        TestUtil.assertError("Total connections not as expected:%s,but current is:%s", 0, pool.getTotalSize());
        TestUtil.assertError("Idle connections not as expected:%s,but current is:%s", 0, pool.getIdleSize());
    }
}
