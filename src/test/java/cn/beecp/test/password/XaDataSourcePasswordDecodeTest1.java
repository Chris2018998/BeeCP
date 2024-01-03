/*
 * Copyright(C) Chris2018998
 * Contact:Chris2018998@tom.com
 *
 * Licensed under GNU Lesser General Public License v2.1
 */
package cn.beecp.test.password;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;
import cn.beecp.RawXaConnectionFactory;
import cn.beecp.pool.FastConnectionPool;
import cn.beecp.test.TestCase;
import cn.beecp.test.TestUtil;
import cn.beecp.test.mock.MockXaDataSource;

public class XaDataSourcePasswordDecodeTest1 extends TestCase {
    private final String url = "jdbc:mock:test";
    private final String user = "mock";
    private final String password = "root";
    private BeeDataSource ds;

    public void setUp() throws Throwable {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setConnectionFactoryClassName("cn.beecp.test.mock.MockXaDataSource");
        config.setJdbcLinkInfDecoderClassName("cn.beecp.test.password.DatabasePasswordDecoder");
        ds = new BeeDataSource(config);
    }

    public void tearDown() throws Throwable {
        ds.close();
    }

    public void test() throws Exception {
        FastConnectionPool pool = (FastConnectionPool) TestUtil.getFieldValue(ds, "pool");
        RawXaConnectionFactory rawXaConnFactory = (RawXaConnectionFactory) TestUtil.getFieldValue(pool, "rawXaConnFactory");
        MockXaDataSource xaDs = (MockXaDataSource) TestUtil.getFieldValue(rawXaConnFactory, "dataSource");

        if (!user.equals(xaDs.getUser()))
            TestUtil.assertError("user expect value:%s,actual value:%s", user, xaDs.getUser());
        if (!url.equals(xaDs.getURL()))
            TestUtil.assertError("url expect value:%s,actual value:%s", url, xaDs.getURL());
        if (!DatabasePasswordDecoder.password().equals(xaDs.getPassword()))
            TestUtil.assertError("password expect value:%s,actual value:%s", DatabasePasswordDecoder.password(), xaDs.getPassword());
    }
}