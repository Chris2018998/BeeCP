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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class TransactionNormalRollbackTest extends TestCase {
    private BeeDataSource ds;

    public void setUp() throws Throwable {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        config.setJdbcUrl(JdbcConfig.JDBC_URL);
        config.setDriverClassName(JdbcConfig.JDBC_DRIVER);
        config.setUsername(JdbcConfig.JDBC_USER);
        config.setPassword(JdbcConfig.JDBC_PASSWORD);
        ds = new BeeDataSource(config);
    }

    public void tearDown() throws Throwable {
        ds.close();
    }

    public void test() throws InterruptedException, Exception {
        Connection con1 = null;
        PreparedStatement ps1 = null;
        ResultSet re1 = null;
        PreparedStatement ps2 = null;

        PreparedStatement ps3 = null;
        ResultSet re3 = null;
        try {
            con1 = ds.getConnection();
            con1.setAutoCommit(false);

            String userId = String.valueOf(new Random(Long.MAX_VALUE).nextLong());
            ps1 = con1
                    .prepareStatement("select count(*) from " + JdbcConfig.TEST_TABLE + " where TEST_ID='" + userId + "'");
            re1 = ps1.executeQuery();
            if (re1.next()) {
                int size = re1.getInt(1);
                if (size != 0)
                    TestUtil.assertError("record size error");
            }

            ps2 = con1.prepareStatement("insert into " + JdbcConfig.TEST_TABLE + "(TEST_ID,TEST_NAME)values(?,?)");
            ps2.setString(1, userId);
            ps2.setString(2, userId);
            int rows = ps2.executeUpdate();
            if (rows != 1)
                TestUtil.assertError("Failed to insert");

            con1.rollback();

            ps3 = con1
                    .prepareStatement("select count(*) from " + JdbcConfig.TEST_TABLE + " where TEST_ID='" + userId + "'");
            re3 = ps3.executeQuery();
            if (re3.next()) {
                int size = re3.getInt(1);
                if (size != 0)
                    TestUtil.assertError("rollback failed");
            }
        } finally {
            if (re1 != null)
                TestUtil.oclose(re1);
            if (ps1 != null)
                TestUtil.oclose(ps1);
            if (ps2 != null)
                TestUtil.oclose(ps2);
            if (re3 != null)
                TestUtil.oclose(re3);
            if (ps3 != null)
                TestUtil.oclose(ps3);
            if (con1 != null)
                TestUtil.oclose(con1);
        }
    }
}
