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
import cn.beecp.pool.FastConnectionPool;
import cn.beecp.test.JdbcConfig;
import cn.beecp.test.TestCase;
import cn.beecp.test.TestUtil;

public class PoolRestTest extends TestCase {
    private BeeDataSource ds;
    private int initSize = 5;

    public void setUp() throws Throwable {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        config.setJdbcUrl(JdbcConfig.JDBC_URL);
        config.setDriverClassName(JdbcConfig.JDBC_DRIVER);
        config.setUsername(JdbcConfig.JDBC_USER);
        config.setPassword(JdbcConfig.JDBC_PASSWORD);
        config.setInitialSize(initSize);
        ds = new BeeDataSource(config);
    }

    public void tearDown() throws Throwable {
        ds.close();
    }

    public void test() throws InterruptedException, Exception {
        FastConnectionPool pool = (FastConnectionPool) TestUtil.getFieldValue(ds, "pool");
        if (pool.getConnTotalSize() != initSize)
            TestUtil.assertError("Total connections expected:%s,current is:%s", initSize, pool.getConnTotalSize());
        if (pool.getConnIdleSize() != initSize)
            TestUtil.assertError("connections expected:%s,current is:%s", initSize, pool.getConnIdleSize());

        pool.clearAllConnections();

        if (pool.getConnTotalSize() != 0)
            TestUtil.assertError("Total connections not as expected 0,but current is:%s", pool.getConnTotalSize(), "");
        if (pool.getConnIdleSize() != 0)
            TestUtil.assertError("Idle connections not as expected 0,but current is:%s", pool.getConnIdleSize(), "");
    }
}
