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
package cn.beecp.test.config;

import cn.beecp.BeeDataSourceConfig;
import cn.beecp.BeeDataSourceConfigException;
import cn.beecp.test.JdbcConfig;
import cn.beecp.test.TestCase;

/**
 * @author Chris.Liao
 * @version 1.0
 */

public class ValidSqlErrorSetTest extends TestCase {
    public void test() throws Exception {
        BeeDataSourceConfig testConfig = new BeeDataSourceConfig();
        String url = JdbcConfig.JDBC_URL;
        testConfig.setJdbcUrl(url);
        testConfig.setDriverClassName(JdbcConfig.JDBC_DRIVER);
        testConfig.setUsername(JdbcConfig.JDBC_USER);
        testConfig.setPassword(JdbcConfig.JDBC_PASSWORD);
        testConfig.setValidTestSql("?={call test(}");
        try {
            testConfig.check();
        } catch (BeeDataSourceConfigException e) {
            if (!e.getMessage().equals("validTestSql must be start with 'select '"))
                throw e;
        }
    }
}