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

import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceConnectionCloseTest extends TestCase {
    private BeeDataSource ds;

    public void setUp() {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        config.setJdbcUrl(JdbcConfig.JDBC_URL);
        config.setDriverClassName(JdbcConfig.JDBC_DRIVER);
        config.setUsername(JdbcConfig.JDBC_USER);
        config.setPassword(JdbcConfig.JDBC_PASSWORD);
        ds = new BeeDataSource(config);
    }

    public void tearDown() {
        //do nothing
    }

    public void test() throws Exception {
        ds.close();
        Connection con = null;
        try {
            con = ds.getConnection();
            if (con != null) TestUtil.assertError("DataSourceConnectionFactoryTest failed");
            System.out.println(con);
        } catch (SQLException e) {
        } finally {
            if (con != null)
                TestUtil.oclose(con);
        }
    }
}
