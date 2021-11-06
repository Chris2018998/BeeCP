/*
 * Copyright(C) Chris2018998
 * Contact:Chris2018998@tom.com
 *
 * Licensed under GNU General Public License version 3.0.
 */
package cn.beecp.pool;

import cn.beecp.BeeDataSourceConfig;
import cn.beecp.BeeDataSourceConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

/**
 * Pool Static Center
 *
 * @author Chris.Liao
 * @version 1.0
 */
public class PoolStaticCenter {
    public static final Logger CommonLog = LoggerFactory.getLogger(PoolStaticCenter.class);
    public static final SQLTimeoutException RequestTimeoutException = new SQLTimeoutException("Request timeout");
    public static final SQLException RequestInterruptException = new SQLException("Request interrupted");
    public static final SQLException PoolCloseException = new SQLException("Pool has shut down or in clearing");
    public static final XAException XaConnectionClosedException = new XAException("No operations allowed after connection closed");
    public static final SQLException ConnectionClosedException = new SQLException("No operations allowed after connection closed");
    public static final SQLException StatementClosedException = new SQLException("No operations allowed after statement closed");
    public static final SQLException ResultSetClosedException = new SQLException("No operations allowed after resultSet closed");
    public static final SQLException AutoCommitChangeForbiddenException = new SQLException("Execute 'commit' or 'rollback' before this operation");
    public static final SQLException DriverNotSupportNetworkTimeoutException = new SQLException("Driver not support 'networkTimeout'");
    public static final String DS_Config_Prop_Separator_MiddleLine = "-";
    public static final String DS_Config_Prop_Separator_UnderLine = "_";
    static final Connection CLOSED_CON = (Connection) Proxy.newProxyInstance(
            PoolStaticCenter.class.getClassLoader(),
            new Class[]{Connection.class},
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("isClosed".equals(method.getName())) {
                        return Boolean.TRUE;
                    } else {
                        throw ConnectionClosedException;
                    }
                }
            }
    );
    static final CallableStatement CLOSED_CSTM = (CallableStatement) Proxy.newProxyInstance(
            PoolStaticCenter.class.getClassLoader(),
            new Class[]{CallableStatement.class},
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("isClosed".equals(method.getName())) {
                        return Boolean.TRUE;
                    } else {
                        throw StatementClosedException;
                    }
                }
            }
    );
    static final ResultSet CLOSED_RSLT = (ResultSet) Proxy.newProxyInstance(
            PoolStaticCenter.class.getClassLoader(),
            new Class[]{ResultSet.class},
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("isClosed".equals(method.getName())) {
                        return Boolean.TRUE;
                    } else {
                        throw ResultSetClosedException;
                    }
                }
            }
    );


    static final Connection createProxyConnection(final PooledConnection p, final Borrower b) throws SQLException {
        throw new SQLException("Proxy classes not be generated,please execute 'ProxyClassGenerator' after compile");
    }

    static final ResultSet createProxyResultSet(final ResultSet raw, final ProxyStatementBase owner, final PooledConnection p) throws SQLException {
        throw new SQLException("Proxy classes not be generated,please execute 'ProxyClassGenerator' after compile");
    }

    static final void oclose(final ResultSet r) {
        try {
            r.close();
        } catch (Throwable e) {
            CommonLog.debug("Warning:Error at closing resultSet:", e);
        }
    }

    static final void oclose(final Statement s) {
        try {
            s.close();
        } catch (Throwable e) {
            CommonLog.debug("Warning:Error at closing statement:", e);
        }
    }

    static final void oclose(final Connection c) {
        try {
            c.close();
        } catch (Throwable e) {
            CommonLog.debug("Warning:Error at closing connection:", e);
        }
    }

    static final void validateTestSql(Connection rawCon, Statement st, String testSql, boolean isDefaultAutoCommit) throws SQLException {
        boolean changed = false;
        try {
            if (isDefaultAutoCommit) {
                rawCon.setAutoCommit(false);
                changed = true;
            }
            st.execute(testSql);
        } finally {
            try {
                rawCon.rollback();//why? maybe store procedure in test sql
                if (changed) rawCon.setAutoCommit(isDefaultAutoCommit);//reset to default
            } catch (Throwable e) {
                throw e instanceof SQLException ? (SQLException) e : new SQLException(e);
            }
        }
    }

    public static final boolean isBlank(String str) {
        if (str == null) return true;
        int l = str.length();
        for (int i = 0; i < l; ++i) {
            if (!Character.isWhitespace((int) str.charAt(i)))
                return false;
        }
        return true;
    }

    public static final boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }


    /**
     * get config item value by property name,which support three format:
     * 1:hump,example:maxActive
     * 2:middle line,example: max-active
     * 3:middle line,example: max_active
     *
     * @param configProperties configuration list
     * @param propertyName     config item name
     * @return configuration item value
     */
    public final static String getConfigValue(Properties configProperties, String propertyName) {
        String value = readConfig(configProperties, propertyName);
        if (isBlank(value))
            value = readConfig(configProperties, propertyNameToFieldId(propertyName, DS_Config_Prop_Separator_MiddleLine));
        if (isBlank(value))
            value = readConfig(configProperties, propertyNameToFieldId(propertyName, DS_Config_Prop_Separator_UnderLine));
        return value;
    }

    public final static String readConfig(Properties configProperties, String propertyName) {
        String value = configProperties.getProperty(propertyName);
        if (!isBlank(value)) {
            CommonLog.info("beecp.{}={}", propertyName, value);
            return value.trim();
        } else {
            return null;
        }
    }

    public final static String trimString(String value) {
        return (value == null) ? null : value.trim();
    }

    public final static Driver loadJdbcDriver(String driverClassName) throws BeeDataSourceConfigException {
        try {
            Class<?> driverClass = Class.forName(driverClassName, true, BeeDataSourceConfig.class.getClassLoader());
            return (Driver) driverClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new BeeDataSourceConfigException("Not found driver class:" + driverClassName);
        } catch (InstantiationException e) {
            throw new BeeDataSourceConfigException("Failed to instantiate driver class:" + driverClassName, e);
        } catch (IllegalAccessException e) {
            throw new BeeDataSourceConfigException("Failed to instantiate driver class:" + driverClassName, e);
        }
    }

    public static final void setPropertiesValue(Object bean, Map<String, Object> setValueMap) throws Exception {
        if (bean == null) throw new BeeDataSourceConfigException("Bean can't be null");
        setPropertiesValue(bean, getSetMethodMap(bean.getClass()), setValueMap);
    }

    public static final void setPropertiesValue(Object bean, Map<String, Method> setMethodMap, Map<String, Object> setValueMap) {
        if (bean == null) throw new BeeDataSourceConfigException("Bean can't be null");
        if (setMethodMap == null) throw new BeeDataSourceConfigException("Set method map can't be null");
        if (setMethodMap.isEmpty()) throw new BeeDataSourceConfigException("Set method map can't be empty");
        if (setValueMap == null) throw new BeeDataSourceConfigException("Properties value map can't be null");
        if (setValueMap.isEmpty()) throw new BeeDataSourceConfigException("Properties value map can't be empty");

        Object value;
        Iterator<Map.Entry<String, Object>> iterator = setValueMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String propertyName = entry.getKey();
            Object setValue = entry.getValue();
            Method setMethod = setMethodMap.get(propertyName);
            if (setMethod != null && setValue != null) {
                Class type = setMethod.getParameterTypes()[0];
                try {//1:convert config value to match type of set method
                    value = convert(propertyName, setValue, type);
                } catch (BeeDataSourceConfigException e) {
                    throw e;
                } catch (Exception e) {
                    throw new BeeDataSourceConfigException("Failed to convert config value to property(" + propertyName + ")type:" + type.getName(), e);
                }

                try {//2:inject value by set method
                    setMethod.invoke(bean, value);
                } catch (IllegalAccessException e) {
                    throw new BeeDataSourceConfigException("Failed to inject config value to property:" + propertyName, e);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getTargetException();
                    if (cause != null) {
                        throw new BeeDataSourceConfigException("Failed to inject config value to property:" + propertyName, cause);
                    } else {
                        throw new BeeDataSourceConfigException("Failed to inject config value to property:" + propertyName, e);
                    }
                }
            }
        }
    }

    public static final Map<String, Method> getSetMethodMap(Class beanClass) {
        HashMap<String, Method> methodMap = new LinkedHashMap<String, Method>(32);
        Method[] methods = beanClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (method.getParameterTypes().length == 1 && methodName.startsWith("set") && methodName.length() > 3) {
                methodName = methodName.substring(3);
                methodName = methodName.substring(0, 1).toLowerCase(Locale.US) + methodName.substring(1);
                methodMap.put(methodName, method);
            }
        }
        return methodMap;
    }

    public static final String propertyNameToFieldId(String property, String separator) {
        char[] chars = property.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length);
        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                sb.append(separator).append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static final Object convert(String propName, Object setValue, Class type) throws BeeDataSourceConfigException {
        if (type.isInstance(setValue)) {
            return setValue;
        } else if (type == String.class) {
            return setValue.toString();
        }

        String text = setValue.toString();
        text = text.trim();
        if (text.length() == 0) return null;

        if (type == char.class || type == Character.class) {
            return text.toCharArray()[0];
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(text);
        } else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(text);
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(text);
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(text);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(text);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(text);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(text);
        } else if (type == BigInteger.class) {
            return new BigInteger(text);
        } else if (type == BigDecimal.class) {
            return new BigDecimal(text);
        } else {
            try {
                Object objInstance = Class.forName(text).newInstance();
                if (!type.isInstance(objInstance))
                    throw new BeeDataSourceConfigException("Config value can't mach property(" + propName + ")type:" + type.getName());

                return objInstance;
            } catch (ClassNotFoundException e) {
                throw new BeeDataSourceConfigException("Not found class:" + text);
            } catch (InstantiationException e) {
                throw new BeeDataSourceConfigException("Failed to instantiated class:" + text, e);
            } catch (IllegalAccessException e) {
                throw new BeeDataSourceConfigException("Failed to instantiated class:" + text, e);
            }
        }
    }

    static final class ProxyConnectionCloseTask implements Runnable {
        private ProxyConnectionBase proxyCon;

        public ProxyConnectionCloseTask(ProxyConnectionBase proxyCon) {
            this.proxyCon = proxyCon;
        }

        public void run() {
            try {
                proxyCon.close();
            } catch (Throwable e) {
            }
        }
    }
}