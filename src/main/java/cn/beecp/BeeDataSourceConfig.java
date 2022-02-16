/*
 * Copyright(C) Chris2018998
 * Contact:Chris2018998@tom.com
 *
 * Licensed under GNU Lesser General Public License v2.1
 */
package cn.beecp;

import cn.beecp.pool.DataSourceConnectionFactory;
import cn.beecp.pool.DriverConnectionFactory;
import cn.beecp.xa.RawXaConnectionFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.beecp.TransactionIsolationLevel.TRANS_LEVEL_CODE_LIST;
import static cn.beecp.TransactionIsolationLevel.isValidTransactionIsolationCode;
import static cn.beecp.pool.PoolStaticCenter.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Connection pool configuration under datasource
 *
 * @author Chris.Liao
 * @version 1.0
 */
public class BeeDataSourceConfig implements BeeDataSourceConfigJmxBean {
    //The number of CPUs
    private static final int NCPUS = Runtime.getRuntime().availableProcessors();
    //poolName index
    private static final AtomicInteger PoolNameIndex = new AtomicInteger(1);
    //jdbc user name
    private String username;
    //jdbc password
    private String password;
    //jdbc url
    private String jdbcUrl;
    //jdbc driver class name
    private String driverClassName;

    //pool name,if not set,auto generated by<code>BeeDataSourceConfig.PoolNameIndex</code>
    private String poolName;
    //boolean indicator,true:pool will use fair semaphore and fair transfer policy;default value:false
    private boolean fairMode;
    //size of connections on pool starting
    private int initialSize;
    //max reachable size of connections in pool
    private int maxActive = Math.min(Math.max(10, NCPUS), 50);
    //max permit size of pool semaphore
    private int borrowSemaphoreSize = Math.min(maxActive / 2, NCPUS);
    //milliseconds:max wait time to get one connection from pool<code>ConnectionPool.getConnection()</code>
    private long maxWait = SECONDS.toMillis(8);
    //milliseconds:max idle time of connections in pool,when reach,then close them and remove from pool
    private long idleTimeout = MINUTES.toMillis(3);
    //milliseconds:max no-use time of borrowed connections,when reach,then return them to pool by forced close
    private long holdTimeout = MINUTES.toMillis(3);

    //connection valid test sql on borrowed
    private String validTestSql = "SELECT 1";
    //seconds:max time to get valid test result
    private int validTestTimeout = 3;
    //milliseconds:connections valid assume time after last activity,if borrowed,not need test during the duration
    private long validAssumeTime = 500L;
    //milliseconds:interval time to run timer check task
    private long timerCheckInterval = MINUTES.toMillis(3);
    //using connections forced close indicator on pool clean
    private boolean forceCloseUsingOnClear;
    //milliseconds:delay time for next loop to clear,when<code>forceCloseUsingOnClear</code> is false and exists using connections
    private long delayTimeForNextClear = 3000L;

    //connection default value:catalog <code>Connection.setAutoCommit(String)</code>
    private String defaultCatalog;
    //connection default value:schema <code>Connection.setSchema(String)</code>
    private String defaultSchema;
    //connection default value:readOnly <code>Connection.setReadOnly(boolean)</code>
    private boolean defaultReadOnly;
    //connection default value:autoCommit <code>Connection.setAutoCommit(boolean)</code>
    private boolean defaultAutoCommit = true;
    //connection default value:transactionIsolation <code>Connection.setTransactionIsolation(int)</code>
    private int defaultTransactionIsolationCode = -999;
    //connection default value:description of transactionIsolation <code>defaultTransactionIsolationCode</code>
    private String defaultTransactionIsolationName;

    //raw JDBC connection factory
    private RawConnectionFactory connectionFactory;
    //raw JDBC connection factory class name
    private String connectionFactoryClassName;
    //xaConnection factory
    private RawXaConnectionFactory xaConnectionFactory;
    //xaConnection factory class name
    private String xaConnectionFactoryClassName;
    //extra properties for jdbc driver to connect db
    private Properties connectProperties = new Properties();
    //password decoder
    private Class passwordDecoderClass;
    //password decoder class name
    private String passwordDecoderClassName;

    //pool implementation class name
    private String poolImplementClassName;
    //boolean indicator,true:register dataSource to jmx
    private boolean enableJmx;
    //boolean indicator,true:print config item info on pool starting
    private boolean printConfigLog;
    //boolean indicator,true:print runtime log
    private boolean printRuntimeLog;

    //*********************************************** 0 **************************************************************//
    public BeeDataSourceConfig() {
    }

    //read configuration from properties file
    public BeeDataSourceConfig(File propertiesFile) {
        this.loadFromPropertiesFile(propertiesFile);
    }

    //read configuration from properties file
    public BeeDataSourceConfig(String propertiesFileName) {
        this.loadFromPropertiesFile(propertiesFileName);
    }

    //read configuration from properties
    public BeeDataSourceConfig(Properties configProperties) {
        this.loadFromProperties(configProperties);
    }

    public BeeDataSourceConfig(String driver, String url, String user, String password) {
        this.jdbcUrl = trimString(url);
        this.username = trimString(user);
        this.password = trimString(password);
        this.driverClassName = trimString(driver);
    }


    //*********************************************** 1 **************************************************************//
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = trimString(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = trimString(password);
    }

    public String getUrl() {
        return jdbcUrl;
    }

    public void setUrl(String jdbcUrl) {
        this.jdbcUrl = trimString(jdbcUrl);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = trimString(jdbcUrl);
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = trimString(driverClassName);
    }


    //*********************************************** 2 ***************************************************************//
    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = trimString(poolName);
    }

    public boolean isFairMode() {
        return fairMode;
    }

    public void setFairMode(boolean fairMode) {
        this.fairMode = fairMode;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        if (initialSize >= 0) this.initialSize = initialSize;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        if (maxActive > 0) {
            this.maxActive = maxActive;
            //fix issue:#19 Chris-2020-08-16 begin
            this.borrowSemaphoreSize = (maxActive > 1) ? Math.min(maxActive / 2, NCPUS) : 1;
            //fix issue:#19 Chris-2020-08-16 end
        }
    }

    public int getBorrowSemaphoreSize() {
        return borrowSemaphoreSize;
    }

    public void setBorrowSemaphoreSize(int borrowSemaphoreSize) {
        if (borrowSemaphoreSize > 0) this.borrowSemaphoreSize = borrowSemaphoreSize;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        if (maxWait > 0) this.maxWait = maxWait;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        if (idleTimeout > 0) this.idleTimeout = idleTimeout;
    }

    public long getHoldTimeout() {
        return holdTimeout;
    }

    public void setHoldTimeout(long holdTimeout) {
        if (holdTimeout > 0) this.holdTimeout = holdTimeout;
    }


    //*********************************************** 3 **************************************************************//
    public String getValidTestSql() {
        return validTestSql;
    }

    public void setValidTestSql(String validTestSql) {
        if (!isBlank(validTestSql)) this.validTestSql = trimString(validTestSql);
    }

    public int getValidTestTimeout() {
        return validTestTimeout;
    }

    public void setValidTestTimeout(int validTestTimeout) {
        if (validTestTimeout >= 0) this.validTestTimeout = validTestTimeout;
    }

    public long getValidAssumeTime() {
        return validAssumeTime;
    }

    public void setValidAssumeTime(long validAssumeTime) {
        if (validAssumeTime >= 0) this.validAssumeTime = validAssumeTime;
    }

    public long getTimerCheckInterval() {
        return timerCheckInterval;
    }

    public void setTimerCheckInterval(long timerCheckInterval) {
        if (timerCheckInterval > 0) this.timerCheckInterval = timerCheckInterval;
    }

    public boolean isForceCloseUsingOnClear() {
        return forceCloseUsingOnClear;
    }

    public void setForceCloseUsingOnClear(boolean forceCloseUsingOnClear) {
        this.forceCloseUsingOnClear = forceCloseUsingOnClear;
    }

    public long getDelayTimeForNextClear() {
        return delayTimeForNextClear;
    }

    public void setDelayTimeForNextClear(long delayTimeForNextClear) {
        if (delayTimeForNextClear > 0) this.delayTimeForNextClear = delayTimeForNextClear;
    }


    //*********************************************** 4 **************************************************************//
    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = trimString(defaultCatalog);
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = trimString(defaultSchema);
    }

    public boolean isDefaultReadOnly() {
        return defaultReadOnly;
    }

    public void setDefaultReadOnly(boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public boolean isDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public int getDefaultTransactionIsolationCode() {
        return defaultTransactionIsolationCode;
    }

    public void setDefaultTransactionIsolationCode(int defaultTransactionIsolationCode) {
        this.defaultTransactionIsolationCode = defaultTransactionIsolationCode;
    }

    public String getDefaultTransactionIsolationName() {
        return defaultTransactionIsolationName;
    }

    public void setDefaultTransactionIsolationName(String defaultTransactionIsolationName) {
        this.defaultTransactionIsolationName = trimString(defaultTransactionIsolationName);
    }


    //*********************************************** 5 **************************************************************//
    public RawConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(RawConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getConnectionFactoryClassName() {
        return connectionFactoryClassName;
    }

    public void setConnectionFactoryClassName(String connectionFactoryClassName) {
        this.connectionFactoryClassName = trimString(connectionFactoryClassName);
    }

    public RawXaConnectionFactory getXaConnectionFactory() {
        return xaConnectionFactory;
    }

    public void setXaConnectionFactory(RawXaConnectionFactory xaConnectionFactory) {
        this.xaConnectionFactory = xaConnectionFactory;
    }

    public String getXaConnectionFactoryClassName() {
        return xaConnectionFactoryClassName;
    }

    public void setXaConnectionFactoryClassName(String xaConnectionFactoryClassName) {
        this.xaConnectionFactoryClassName = trimString(xaConnectionFactoryClassName);
    }

    public void removeConnectProperty(String key) {
        if (!isBlank(key)) connectProperties.remove(key);
    }

    public void addConnectProperty(String key, Object value) {
        if (!isBlank(key) && value != null) connectProperties.put(key, value);
    }

    public void addConnectProperty(String connectPropertyText) {
        if (!isBlank(connectPropertyText)) {
            String[] attributeArray = connectPropertyText.split("&");
            for (String attribute : attributeArray) {
                String[] pair = attribute.split("=");
                if (pair.length == 2) {
                    addConnectProperty(pair[0].trim(), pair[1].trim());
                } else {
                    pair = attribute.split(":");
                    if (pair.length == 2) {
                        addConnectProperty(pair[0].trim(), pair[1].trim());
                    }
                }
            }
        }
    }

    public Class getPasswordDecoderClass() {
        return passwordDecoderClass;
    }

    public void setPasswordDecoderClass(Class passwordDecoderClass) {
        this.passwordDecoderClass = passwordDecoderClass;
    }

    public String getPasswordDecoderClassName() {
        return passwordDecoderClassName;
    }

    public void setPasswordDecoderClassName(String passwordDecoderClassName) {
        this.passwordDecoderClassName = passwordDecoderClassName;
    }

    //*********************************************** 6 **************************************************************//
    public String getPoolImplementClassName() {
        return poolImplementClassName;
    }

    public void setPoolImplementClassName(String poolImplementClassName) {
        this.poolImplementClassName = trimString(poolImplementClassName);
    }

    public boolean isEnableJmx() {
        return enableJmx;
    }

    public void setEnableJmx(boolean enableJmx) {
        this.enableJmx = enableJmx;
    }

    public void setPrintConfigLog(boolean printConfigLog) {
        this.printConfigLog = printConfigLog;
    }

    public boolean isPrintRuntimeLog() {
        return printRuntimeLog;
    }

    public void setPrintRuntimeLog(boolean printRuntimeLog) {
        this.printRuntimeLog = printRuntimeLog;
    }


    //*********************************************** 7 **************************************************************//
    void copyTo(BeeDataSourceConfig config) {
        //1:primitive type copy
        String fieldName = "";
        try {
            for (Field field : BeeDataSourceConfig.class.getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) || "connectProperties".equals(field.getName()))
                    continue;
                Object fieldValue = field.get(this);
                fieldName = field.getName();

                if (printConfigLog) CommonLog.info("{}.{}={}", poolName, fieldName, fieldValue);
                field.set(config, fieldValue);
            }
        } catch (Exception e) {
            throw new BeeDataSourceConfigException("Failed to copy field[" + fieldName + "]", e);
        }

        //2:copy 'connectProperties'
        Iterator<Map.Entry<Object, Object>> iterator = connectProperties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> entry = iterator.next();
            if (printConfigLog)
                CommonLog.info("{}.connectProperties.{}={}", poolName, entry.getKey(), entry.getValue());
            config.addConnectProperty((String) entry.getKey(), entry.getValue());
        }
    }

    //create PasswordDecoder instance
    private PasswordDecoder createPasswordDecoder() throws BeeDataSourceConfigException {
        PasswordDecoder passwordDecoder = null;
        Class<?> passwordDecoderClass = this.passwordDecoderClass;
        if (passwordDecoderClass == null && !isBlank(passwordDecoderClassName)) {
            try {
                passwordDecoderClass = Class.forName(passwordDecoderClassName);
            } catch (Throwable e) {
                throw new BeeDataSourceConfigException("Failed to load password decoder class:" + passwordDecoderClassName, e);
            }
        }

        if (passwordDecoderClass != null) {
            try {
                passwordDecoder = (PasswordDecoder) passwordDecoderClass.newInstance();
            } catch (Throwable e) {
                throw new BeeDataSourceConfigException("Failed to instantiate password decoder class:" + passwordDecoderClass.getName(), e);
            }
        }
        return passwordDecoder;
    }

    //check pool configuration
    public BeeDataSourceConfig check() throws SQLException {
        if (this.maxActive <= 0)
            throw new BeeDataSourceConfigException("maxActive must be greater than zero");
        if (this.initialSize < 0)
            throw new BeeDataSourceConfigException("initialSize must not be less than zero");
        if (this.initialSize > maxActive)
            throw new BeeDataSourceConfigException("initialSize must not be greater than maxActive");
        if (this.borrowSemaphoreSize <= 0)
            throw new BeeDataSourceConfigException("borrowSemaphoreSize must be greater than zero");
        //fix issue:#19 Chris-2020-08-16 begin
        //if (this.borrowConcurrentSize > maxActive)
        //throw new BeeDataSourceConfigException("Pool 'borrowConcurrentSize' must not be greater than pool max size");
        //fix issue:#19 Chris-2020-08-16 end
        if (this.idleTimeout <= 0)
            throw new BeeDataSourceConfigException("idleTimeout must be greater than zero");
        if (this.holdTimeout <= 0)
            throw new BeeDataSourceConfigException("holdTimeout must be greater than zero");
        if (this.maxWait <= 0)
            throw new BeeDataSourceConfigException("maxWait must be greater than zero");
        //fix issue:#1 The check of validationQuerySQL has logic problem. Chris-2019-05-01 begin
        //if (this.validationQuerySQL != null && validationQuerySQL.trim().length() == 0) {
        if (isBlank(this.validTestSql))
            throw new BeeDataSourceConfigException("validTestSql cant be null or empty");
        if (!this.validTestSql.toUpperCase(Locale.US).startsWith("SELECT ")) {
            //fix issue:#1 The check of validationQuerySQL has logic problem. Chris-2019-05-01 end
            throw new BeeDataSourceConfigException("validTestSql must be start with 'select '");
        }

        if (isBlank(this.poolName)) this.poolName = "FastPool-" + PoolNameIndex.getAndIncrement();

        //get transaction Isolation Code
        int transactionIsolationCode = getTransactionIsolationCode();
        this.setDefaultTransactionIsolationCode(transactionIsolationCode);

        //try to create connection factory
        RawConnectionFactory connectionFactory = tryCreateConnectionFactory();

        BeeDataSourceConfig configCopy = new BeeDataSourceConfig();
        this.copyTo(configCopy);

        configCopy.setConnectionFactory(connectionFactory);
        configCopy.setDefaultTransactionIsolationCode(transactionIsolationCode);
        return configCopy;
    }

    public void loadFromPropertiesFile(String filename) {
        if (isBlank(filename)) throw new IllegalArgumentException("Properties file can't be null");
        loadFromPropertiesFile(new File(filename));
    }

    public void loadFromPropertiesFile(File file) {
        if (file == null) throw new IllegalArgumentException("Properties file can't be null");
        if (!file.exists()) throw new IllegalArgumentException("File not found:" + file.getAbsolutePath());
        if (!file.isFile()) throw new IllegalArgumentException("Target object is not a valid file");
        if (!file.getAbsolutePath().toLowerCase(Locale.US).endsWith(".properties"))
            throw new IllegalArgumentException("Target file is not a properties file");

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            Properties configProperties = new Properties();
            configProperties.load(stream);
            loadFromProperties(configProperties);
        } catch (BeeDataSourceConfigException e) {
            throw (BeeDataSourceConfigException) e;
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to load properties file:", e);
        } finally {
            if (stream != null) try {
                stream.close();
            } catch (Throwable e) {
            }
        }
    }

    public void loadFromProperties(Properties configProperties) {
        if (configProperties == null || configProperties.isEmpty())
            throw new IllegalArgumentException("Properties can't be null or empty");

        //1:get all properties set methods
        Map<String, Method> setMethodMap = getSetMethodMap(BeeDataSourceConfig.class);
        //2:create properties to collect config value
        Map<String, Object> setValueMap = new HashMap<String, Object>(setMethodMap.size());
        //3:loop to find out properties config value by set methods
        Iterator<String> iterator = setMethodMap.keySet().iterator();
        while (iterator.hasNext()) {
            String propertyName = iterator.next();
            String configVal = getConfigValue(configProperties, propertyName);
            if (isBlank(configVal)) continue;
            setValueMap.put(propertyName, configVal);
        }
        //4:inject found config value to ds config object
        setPropertiesValue(this, setMethodMap, setValueMap);

        //5:try to find 'connectProperties' config value and put to ds config object
        addConnectProperty(getConfigValue(configProperties, "connectProperties"));
        String connectPropertiesCount = getConfigValue(configProperties, "connectProperties.count");
        if (!isBlank(connectPropertiesCount)) {
            int count = 0;
            try {
                count = Integer.parseInt(connectPropertiesCount.trim());
            } catch (Throwable e) {
            }
            for (int i = 1; i <= count; i++)
                addConnectProperty(getConfigValue(configProperties, "connectProperties." + i));
        }
    }

    private final int getTransactionIsolationCode() throws BeeDataSourceConfigException {
        if (!isBlank(defaultTransactionIsolationName)) {
            return TransactionIsolationLevel.getTransactionIsolationCode(defaultTransactionIsolationName);
        } else {
            if (defaultTransactionIsolationCode != -999 && !isValidTransactionIsolationCode(defaultTransactionIsolationCode))
                throw new BeeDataSourceConfigException("defaultTransactionIsolationCode error,valid value is one of[" + TRANS_LEVEL_CODE_LIST + "]");

            return defaultTransactionIsolationCode;
        }
    }

    private final RawConnectionFactory tryCreateConnectionFactory() throws BeeDataSourceConfigException, SQLException {
        PasswordDecoder passwordDecoder = createPasswordDecoder();
        if (connectionFactory != null) return connectionFactory;

        if (isBlank(connectionFactoryClassName)) {
            if (isBlank(jdbcUrl))
                throw new BeeDataSourceConfigException("jdbcUrl can't be null");

            Driver connectDriver = null;
            if (!isBlank(driverClassName))
                connectDriver = loadJdbcDriver(driverClassName);
            else if (!isBlank(jdbcUrl))
                connectDriver = DriverManager.getDriver(jdbcUrl);
            if (connectDriver == null)
                throw new BeeDataSourceConfigException("Failed to load jdbc Driver:" + driverClassName);
            if (!connectDriver.acceptsURL(jdbcUrl))
                throw new BeeDataSourceConfigException("jdbcUrl(" + jdbcUrl + ")can not match driver:" + connectDriver.getClass().getName());

            Properties connectProperties = new Properties();
            connectProperties.putAll(this.connectProperties);
            if (!isBlank(username))
                connectProperties.put("user", username);
            if (!isBlank(password)) {
                String tempPassword = password;
                if (passwordDecoder != null) tempPassword = passwordDecoder.decode(tempPassword);
                connectProperties.setProperty("password", tempPassword);
            }
            return new DriverConnectionFactory(jdbcUrl, connectDriver, connectProperties);
        } else {
            try {
                Class<?> conFactClass = Class.forName(connectionFactoryClassName, true, BeeDataSourceConfig.class.getClassLoader());
                if (RawConnectionFactory.class.isAssignableFrom(conFactClass)) {
                    return (RawConnectionFactory) conFactClass.newInstance();
                } else if (DataSource.class.isAssignableFrom(conFactClass)) {
                    DataSource dataSource = (DataSource) conFactClass.newInstance();
                    Properties connectProperties = this.connectProperties;
                    Map<String, Object> setValueMap = new HashMap<String, Object>(connectProperties.size());
                    Iterator<Map.Entry<Object, Object>> iterator = connectProperties.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) iterator.next();
                        if (entry.getKey() instanceof String) {
                            setValueMap.put((String) entry.getKey(), entry.getValue());
                        }
                    }

                    try {
                        setPropertiesValue(dataSource, setValueMap);
                    } catch (Exception e) {
                        throw new BeeDataSourceConfigException("Failed to set config value to connection dataSource", e);
                    }

                    return new DataSourceConnectionFactory(dataSource, username, password);
                } else {
                    throw new BeeDataSourceConfigException("Error connection factory class,must implement '" + RawConnectionFactory.class.getName() + "' interface");
                }
            } catch (ClassNotFoundException e) {
                throw new BeeDataSourceConfigException("Not found connection factory class:" + connectionFactoryClassName);
            } catch (InstantiationException e) {
                throw new BeeDataSourceConfigException("Failed to instantiate connection factory class:" + connectionFactoryClassName, e);
            } catch (IllegalAccessException e) {
                throw new BeeDataSourceConfigException("Failed to instantiate connection factory class:" + connectionFactoryClassName, e);
            }
        }
    }
}

