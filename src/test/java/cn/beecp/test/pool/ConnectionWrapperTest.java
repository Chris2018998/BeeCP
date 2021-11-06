/*
 * Copyright Chris2018998
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.beecp.test.pool;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;
import cn.beecp.test.JdbcConfig;
import cn.beecp.test.TestCase;
import cn.beecp.test.TestUtil;

import java.sql.*;

public class ConnectionWrapperTest extends TestCase {
    private BeeDataSource ds;

    public void setUp() throws Throwable {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        config.setJdbcUrl(JdbcConfig.JDBC_URL);
        config.setDriverClassName(JdbcConfig.JDBC_DRIVER);
        config.setUsername(JdbcConfig.JDBC_USER);
        config.setPassword(JdbcConfig.JDBC_PASSWORD);
        config.setInitialSize(5);
        config.setValidTestSql("SELECT 1 from dual");
        config.setIdleTimeout(3000);
        ds = new BeeDataSource(config);
    }

    public void tearDown() throws Throwable {
        ds.close();
    }

    public void test() throws InterruptedException, Exception {
        Connection con = null;
        Statement st = null;
        CallableStatement cs = null;
        PreparedStatement ps = null;

        try {
            con = ds.getConnection();
            st = con.createStatement();
            cs = con.prepareCall("?={call test(}");
            ps = con.prepareStatement("select 1 from dual");
            DatabaseMetaData dbs = con.getMetaData();

            if (con.unwrap(Connection.class) != con)
                TestUtil.assertError("Raw conneciton exposed(unwrap)");
            if (st.getConnection() != con)
                TestUtil.assertError("Raw conneciton exposed(st)");
            if (cs.getConnection() != con)
                TestUtil.assertError("Raw conneciton exposed(cs)");
            if (ps.getConnection() != con)
                TestUtil.assertError("Raw conneciton exposed(ps)");
            if (dbs.getConnection() != con)
                TestUtil.assertError("Raw conneciton exposed(dbs)");

            ResultSet re1 = st.executeQuery("select 1 from dual");
            if (re1 != null && re1.getStatement() != st)
                TestUtil.assertError("Raw Statement exposed(st)");
            ResultSet re2 = ps.executeQuery();
            if (re2 != null && re2.getStatement() != ps)
                TestUtil.assertError("Raw Statement exposed(ps)");
            ResultSet re3 = cs.getResultSet();
            if (re3 != null && re3.getStatement() != cs)
                TestUtil.assertError("Raw Statement exposed(cs)");
            ResultSet re4 = dbs.getTableTypes();
            if (re4 != null && re4.getStatement() != null)
                TestUtil.assertError("Raw Statement exposed(dbs)");

            if (re1 != null && re1.getStatement() != st)
                TestUtil.assertError("Raw Result exposed(st)");
            if (re2 != null && re2.getStatement() != ps)
                TestUtil.assertError("Raw Result exposed(ps)");
            if (re3 != null && re3.getStatement() != cs)
                TestUtil.assertError("Raw Result exposed(cs)");
        } finally {
            if (st != null)
                TestUtil.oclose(st);
            if (cs != null)
                TestUtil.oclose(cs);
            if (ps != null)
                TestUtil.oclose(ps);
            if (con != null)
                TestUtil.oclose(con);
        }
    }
}
