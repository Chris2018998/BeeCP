/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Copyright(C) Chris2018998,All rights reserved.
 *
 * Project owner contact:Chris2018998@tom.com.
 *
 * Project Licensed under GNU Lesser General Public License v2.1.
 */
package cn.beecp.pool;

import javassist.*;

import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;

import static cn.beecp.pool.ConnectionPoolStatics.isBlank;

/**
 * An independent execution toolkit class to generate JDBC statement classes with javassist,
 * then write to class folder.
 *
 * @author Chris Liao
 * @version 1.0
 */
@SuppressWarnings("unchecked")
final class ProxyClassGenerator {
    private static final String DefaultFolder = "BeeCP/target/classes";

    /**
     * @param args take the first argument as classes generated output folder,otherwise take default folder
     * @throws Exception throw exception in generating process
     */
    public static void main(String[] args) throws Exception {
        String classesFolder = "";
        if (args != null && args.length > 0) classesFolder = args[0];
        if (isBlank(classesFolder)) classesFolder = DefaultFolder;
        ProxyClassGenerator.writeProxyFile(classesFolder);
    }

    private static void resolveInterfaceMethods(CtClass interfaceClass, LinkedList linkedList, HashSet exitSignatureSet) throws Exception {
        for (CtMethod ctMethod : interfaceClass.getDeclaredMethods()) {
            int modifiers = ctMethod.getModifiers();
            String signature = ctMethod.getName() + ctMethod.getSignature();
            if (Modifier.isAbstract(modifiers)
                    && (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))
                    && !Modifier.isStatic(modifiers)
                    && !Modifier.isFinal(modifiers)
                    && !exitSignatureSet.contains(signature)) {

                linkedList.add(ctMethod);
                exitSignatureSet.add(signature);
            }
        }

        for (CtClass superInterface : interfaceClass.getInterfaces())
            ProxyClassGenerator.resolveInterfaceMethods(superInterface, linkedList, exitSignatureSet);
    }

    /**
     * write to disk folder
     *
     * @param folder classes generated will write to it
     * @throws Exception if failed to write file to disk
     */
    private static void writeProxyFile(String folder) throws Exception {
        CtClass[] ctClasses = ProxyClassGenerator.createProxyClasses();
        for (CtClass ctClass : ctClasses) {
            ctClass.writeFile(folder);
        }
    }

    /**
     * create all wrapper classes based on JDBC some interfaces
     *
     * @return a class array generated by javassist
     * <p>
     * new Class:
     * cn.beecp.pool.ProxyConnection
     * cn.beecp.pool.ProxyStatement
     * cn.beecp.pool.ProxyPsStatement
     * cn.beecp.pool.ProxyCsStatement
     * cn.beecp.pool.ProxyResultSet
     * @throws Exception if failed to generate class
     */
    private static CtClass[] createProxyClasses() throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.importPackage("java.sql");
        classPool.importPackage("cn.beecp.pool");
        classPool.appendClassPath(new LoaderClassPath(ProxyClassGenerator.class.getClassLoader()));

        //............Connection Begin.........
        CtClass ctConnectionClass = classPool.get(Connection.class.getName());
        CtClass ctProxyConnectionBaseClass = classPool.get(ProxyConnectionBase.class.getName());
        CtClass ctProxyConnectionClass = classPool.makeClass("cn.beecp.pool.ProxyConnection", ctProxyConnectionBaseClass);
        ctProxyConnectionClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{classPool.get("cn.beecp.pool.PooledConnection")}, ctProxyConnectionClass);
        ctConstructor.setBody("{super($$);}");
        ctProxyConnectionClass.addConstructor(ctConstructor);
        //...............Connection End................

        //.............statement Begin.............
        CtClass ctStatementClass = classPool.get(Statement.class.getName());
        CtClass ctProxyStatementBaseClass = classPool.get(ProxyStatementBase.class.getName());
        CtClass ctProxyStatementClass = classPool.makeClass("cn.beecp.pool.ProxyStatement", ctProxyStatementBaseClass);
        ctProxyStatementClass.setModifiers(Modifier.PUBLIC);
        CtClass[] statementCreateParamTypes = {
                classPool.get("java.sql.Statement"),
                classPool.get("cn.beecp.pool.ProxyConnectionBase"),
                classPool.get("cn.beecp.pool.PooledConnection")
        };
        ctConstructor = new CtConstructor(statementCreateParamTypes, ctProxyStatementClass);
        ctConstructor.setBody("{super($$);}");
        ctProxyStatementClass.addConstructor(ctConstructor);
        //.............Statement Begin...............

        //............PreparedStatement Begin...............
        CtClass ctPreparedStatementClass = classPool.get(PreparedStatement.class.getName());
        CtClass ctProxyPsStatementClass = classPool.makeClass("cn.beecp.pool.ProxyPsStatement", ctProxyStatementClass);
        ctProxyPsStatementClass.setInterfaces(new CtClass[]{ctPreparedStatementClass});
        ctProxyPsStatementClass.setModifiers(Modifier.PUBLIC);
        CtClass[] statementPsCreateParamTypes = {
                classPool.get("java.sql.PreparedStatement"),
                classPool.get("cn.beecp.pool.ProxyConnectionBase"),
                classPool.get("cn.beecp.pool.PooledConnection")};
        ctConstructor = new CtConstructor(statementPsCreateParamTypes, ctProxyPsStatementClass);
        ctConstructor.setBody("{super($$);}");
        ctProxyPsStatementClass.addConstructor(ctConstructor);
        //........PreparedStatement End..............

        //..............CallableStatement Begin.............
        CtClass ctCallableStatementClass = classPool.get(CallableStatement.class.getName());
        CtClass ctProxyCsStatementClass = classPool.makeClass("cn.beecp.pool.ProxyCsStatement", ctProxyPsStatementClass);
        ctProxyCsStatementClass.setInterfaces(new CtClass[]{ctCallableStatementClass});
        ctProxyCsStatementClass.setModifiers(Modifier.PUBLIC);
        CtClass[] statementCsCreateParamTypes = {
                classPool.get("java.sql.CallableStatement"),
                classPool.get("cn.beecp.pool.ProxyConnectionBase"),
                classPool.get("cn.beecp.pool.PooledConnection")};
        ctConstructor = new CtConstructor(statementCsCreateParamTypes, ctProxyCsStatementClass);
        ctConstructor.setBody("{super($$);}");
        ctProxyCsStatementClass.addConstructor(ctConstructor);
        //...........CallableStatement End...............

        //..............DatabaseMetaData Begin.............
        CtClass ctDatabaseMetaDataClass = classPool.get(DatabaseMetaData.class.getName());
        CtClass ctProxyDatabaseMetaDataBaseClass = classPool.get(ProxyDatabaseMetaDataBase.class.getName());
        CtClass ctProxyDatabaseMetaDataClass = classPool.makeClass("cn.beecp.pool.ProxyDatabaseMetaData", ctProxyDatabaseMetaDataBaseClass);
        ctProxyDatabaseMetaDataClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        CtClass[] databaseMetaDataTypes = {
                classPool.get("java.sql.DatabaseMetaData"),
                classPool.get("cn.beecp.pool.PooledConnection")};
        ctConstructor = new CtConstructor(databaseMetaDataTypes, ctProxyDatabaseMetaDataClass);
        ctConstructor.setBody("{super($$);}");
        ctProxyDatabaseMetaDataClass.addConstructor(ctConstructor);
        //...........DatabaseMetaData End...............

        //............... Result Begin..................
        CtClass ctResultSetClass = classPool.get(ResultSet.class.getName());
        CtClass ctProxyResultSetBaseClass = classPool.get(ProxyResultSetBase.class.getName());
        CtClass ctProxyResultSetClass = classPool.makeClass("cn.beecp.pool.ProxyResultSet", ctProxyResultSetBaseClass);
        ctProxyResultSetClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        CtClass[] resultSetCreateParamTypes1 = {
                classPool.get("java.sql.ResultSet"),
                classPool.get("cn.beecp.pool.PooledConnection")};
        CtConstructor ctConstructor1 = new CtConstructor(resultSetCreateParamTypes1, ctProxyResultSetClass);
        ctConstructor1.setBody("{super($$);}");
        ctProxyResultSetClass.addConstructor(ctConstructor1);

        CtClass[] resultSetCreateParamTypes2 = {
                classPool.get("java.sql.ResultSet"),
                classPool.get("cn.beecp.pool.ProxyStatementBase"),
                classPool.get("cn.beecp.pool.PooledConnection")};
        ctConstructor = new CtConstructor(resultSetCreateParamTypes2, ctProxyResultSetClass);
        ctConstructor.setBody("{super($$);}");
        ctProxyResultSetClass.addConstructor(ctConstructor);
        //............Result End...............
        ProxyClassGenerator.createProxyConnectionClass(classPool, ctProxyConnectionClass, ctConnectionClass, ctProxyConnectionBaseClass);
        ProxyClassGenerator.createProxyStatementClass(classPool, ctProxyStatementClass, ctStatementClass, ctProxyStatementBaseClass);
        ProxyClassGenerator.createProxyStatementClass(classPool, ctProxyPsStatementClass, ctPreparedStatementClass, ctProxyStatementClass);
        ProxyClassGenerator.createProxyStatementClass(classPool, ctProxyCsStatementClass, ctCallableStatementClass, ctProxyPsStatementClass);
        ProxyClassGenerator.createProxyDatabaseMetaDataClass(classPool, ctProxyDatabaseMetaDataClass, ctDatabaseMetaDataClass, ctProxyDatabaseMetaDataBaseClass);
        ProxyClassGenerator.createProxyResultSetClass(ctProxyResultSetClass, ctResultSetClass, ctProxyResultSetBaseClass);

        //............... ProxyObjectFactory Begin..................
        CtClass ctProxyObjectFactoryClass = classPool.get(ConnectionPoolStatics.class.getName());
        for (CtMethod method : ctProxyObjectFactoryClass.getDeclaredMethods()) {
            if ("createProxyConnection".equals(method.getName())) {
                method.setBody("{return new ProxyConnection($$);}");
            } else if ("createProxyResultSet".equals(method.getName())) {
                method.setBody("{return new ProxyResultSet($$);}");
            }
        }
        //............... ProxyObjectFactory end..................
        return new CtClass[]{
                ctProxyConnectionClass,
                ctProxyStatementClass,
                ctProxyPsStatementClass,
                ctProxyCsStatementClass,
                ctProxyDatabaseMetaDataClass,
                ctProxyResultSetClass,
                ctProxyObjectFactoryClass};
    }


    //find out methods,which not need add proxy
    private static HashSet findMethodsNotNeedProxy(CtClass baseClass) {
        HashSet notNeedAddProxyMethods = new HashSet(16);
        for (CtMethod ctSuperClassMethod : baseClass.getMethods()) {
            int modifiers = ctSuperClassMethod.getModifiers();
            if ((!Modifier.isAbstract(modifiers) && (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)))
                    || Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isNative(modifiers)) {
                notNeedAddProxyMethods.add(ctSuperClassMethod.getName() + ctSuperClassMethod.getSignature());
            }
        }
        return notNeedAddProxyMethods;
    }

    /**
     * create connection statement class
     * *
     *
     * @param classPool                   javassist class pool
     * @param ctConnectionClassProxyClass connection implemented sub class will be generated
     * @param ctConnectionClass           connection interface in javassist class pool
     * @param ctConBaseClass              super class extend by 'ctctConnectionClassProxyClass'
     * @throws Exception some error occurred
     */
    private static void createProxyConnectionClass(ClassPool classPool, CtClass ctConnectionClassProxyClass, CtClass ctConnectionClass, CtClass ctConBaseClass) throws Exception {
        LinkedList<CtMethod> linkedList = new LinkedList<CtMethod>();
        HashSet notNeedAddProxyMethods = findMethodsNotNeedProxy(ctConBaseClass);
        ProxyClassGenerator.resolveInterfaceMethods(ctConnectionClass, linkedList, notNeedAddProxyMethods);

        CtClass ctStatementClass = classPool.get(Statement.class.getName());
        CtClass ctPreparedStatementClass = classPool.get(PreparedStatement.class.getName());
        CtClass ctCallableStatementClass = classPool.get(CallableStatement.class.getName());
        CtClass ctDatabaseMetaDataIntf = classPool.get(DatabaseMetaData.class.getName());

        StringBuilder methodBuffer = new StringBuilder(50);
        for (CtMethod ctMethod : linkedList) {
            String methodName = ctMethod.getName();
            CtMethod newCtMethod = CtNewMethod.copy(ctMethod, ctConnectionClassProxyClass, null);
            newCtMethod.setModifiers(Modifier.PUBLIC);

            methodBuffer.delete(0, methodBuffer.length());
            methodBuffer.append("{");
            if (ctMethod.getReturnType() == ctStatementClass) {
                newCtMethod.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
                methodBuffer.append("return new ProxyStatement(raw." + methodName + "($$),this,p);");
            } else if (ctMethod.getReturnType() == ctPreparedStatementClass) {
                newCtMethod.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
                methodBuffer.append("return new ProxyPsStatement(raw." + methodName + "($$),this,p);");
            } else if (ctMethod.getReturnType() == ctCallableStatementClass) {
                newCtMethod.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
                methodBuffer.append("return new ProxyCsStatement(raw." + methodName + "($$),this,p);");
            } else if (ctMethod.getReturnType() == ctDatabaseMetaDataIntf) {
                methodBuffer.append("return new ProxyDatabaseMetaData(raw." + methodName + "($$),p);");
            } else if (methodName.equals("close")) {
                continue;
            } else if (ctMethod.getReturnType() == CtClass.voidType) {
                methodBuffer.append("raw." + methodName + "($$);");
            } else {
                methodBuffer.append("return raw." + methodName + "($$);");
            }

            methodBuffer.append("}");
            newCtMethod.setBody(methodBuffer.toString());
            ctConnectionClassProxyClass.addMethod(newCtMethod);
        }
    }

    private static void createProxyStatementClass(ClassPool classPool, CtClass statementProxyClass, CtClass ctStatementClass, CtClass ctStatementSuperClass) throws Exception {
        LinkedList<CtMethod> linkedList = new LinkedList<CtMethod>();
        HashSet notNeedAddProxyMethods = findMethodsNotNeedProxy(ctStatementSuperClass);
        ProxyClassGenerator.resolveInterfaceMethods(ctStatementClass, linkedList, notNeedAddProxyMethods);

        CtClass ctResultSetClass = classPool.get(ResultSet.class.getName());
        StringBuilder methodBuffer = new StringBuilder(50);

        String rawName = "raw.";
        if ("java.sql.PreparedStatement".equals(ctStatementClass.getName())) {
            rawName = "((PreparedStatement)raw).";
        } else if ("java.sql.CallableStatement".equals(ctStatementClass.getName())) {
            rawName = "((CallableStatement)raw).";
        }

        for (CtMethod ctMethod : linkedList) {
            String methodName = ctMethod.getName();
            CtMethod newCtMethod = CtNewMethod.copy(ctMethod, statementProxyClass, null);
            newCtMethod.setModifiers(methodName.startsWith("execute") ? Modifier.PUBLIC | Modifier.FINAL : Modifier.PUBLIC);

            methodBuffer.delete(0, methodBuffer.length());
            methodBuffer.append("{");

            boolean existsSQLException = exitsSQLException(ctMethod.getExceptionTypes());
            if (existsSQLException) methodBuffer.append("  try{");
            if (ctMethod.getReturnType() == CtClass.voidType) {
                if (methodName.startsWith("execute")) methodBuffer.append("p.commitDirtyInd=!p.curAutoCommit;");
                methodBuffer.append(rawName + methodName + "($$);");
                if (methodName.startsWith("execute"))
                    methodBuffer.append("p.lastAccessTime=System.currentTimeMillis();");
            } else {
                if (methodName.startsWith("execute")) {
                    methodBuffer.append("p.commitDirtyInd=!p.curAutoCommit;");
                    methodBuffer.append(ctMethod.getReturnType().getName() + " r=" + rawName + methodName + "($$);");
                    methodBuffer.append("p.lastAccessTime=System.currentTimeMillis();");
                    if (ctMethod.getReturnType() == ctResultSetClass) {
                        methodBuffer.append("return new ProxyResultSet(r,this,p);");
                    } else {
                        methodBuffer.append("return r;");
                    }
                } else {
                    if (ctMethod.getReturnType() == ctResultSetClass) {
                        methodBuffer.append("return new ProxyResultSet(raw." + methodName + "($$),this,p);");
                    } else
                        methodBuffer.append("return " + rawName + methodName + "($$);");
                }
            }

            if (existsSQLException)
                methodBuffer.append("  }catch(SQLException e){ p.checkSQLException(e);throw e;}");
            methodBuffer.append("}");
            newCtMethod.setBody(methodBuffer.toString());
            statementProxyClass.addMethod(newCtMethod);
        }
    }

    //ctProxyDatabaseMetaDataClass,ctDatabaseMetaDataIntf,ctDatabaseMetaDataSuperClass
    private static void createProxyDatabaseMetaDataClass(ClassPool classPool, CtClass ctProxyDatabaseMetaDataClass, CtClass ctDatabaseMetaDataIntf, CtClass ctDatabaseMetaDataSuperClass) throws Exception {
        LinkedList<CtMethod> linkedList = new LinkedList<CtMethod>();
        HashSet notNeedAddProxyMethods = findMethodsNotNeedProxy(ctDatabaseMetaDataSuperClass);
        ProxyClassGenerator.resolveInterfaceMethods(ctDatabaseMetaDataIntf, linkedList, notNeedAddProxyMethods);
        CtClass ctResultSetClass = classPool.get(ResultSet.class.getName());

        StringBuilder methodBuffer = new StringBuilder(40);
        for (CtMethod ctMethod : linkedList) {
            String methodName = ctMethod.getName();
            CtMethod newCtMethod = CtNewMethod.copy(ctMethod, ctProxyDatabaseMetaDataClass, null);
            newCtMethod.setModifiers(Modifier.PUBLIC);

            methodBuffer.delete(0, methodBuffer.length());
            methodBuffer.append("{")
                    .append("  checkClosed();");

            boolean existsSQLException = exitsSQLException(ctMethod.getExceptionTypes());
            if (existsSQLException) methodBuffer.append("  try{");
            if (ctMethod.getReturnType() == ctResultSetClass) {
                methodBuffer.append("return new ProxyResultSet(raw." + methodName + "($$),p);");
            } else if (ctMethod.getReturnType() == CtClass.voidType) {
                methodBuffer.append("raw." + methodName + "($$);");
            } else {
                methodBuffer.append("return raw." + methodName + "($$);");
            }

            if (existsSQLException)
                methodBuffer.append("  }catch(SQLException e){ p.checkSQLException(e);throw e;}");
            methodBuffer.append("}");
            newCtMethod.setBody(methodBuffer.toString());
            ctProxyDatabaseMetaDataClass.addMethod(newCtMethod);
        }
    }

    private static void createProxyResultSetClass(CtClass ctResultSetClassProxyClass, CtClass ctResultSetClass, CtClass ctResultSetClassSuperClass) throws Exception {
        LinkedList<CtMethod> linkedList = new LinkedList<CtMethod>();
        HashSet notNeedAddProxyMethods = findMethodsNotNeedProxy(ctResultSetClassSuperClass);
        ProxyClassGenerator.resolveInterfaceMethods(ctResultSetClass, linkedList, notNeedAddProxyMethods);
        StringBuilder methodBuffer = new StringBuilder(25);

        for (CtMethod ctMethod : linkedList) {
            String methodName = ctMethod.getName();
            CtMethod newCtMethodm = CtNewMethod.copy(ctMethod, ctResultSetClassProxyClass, null);
            newCtMethodm.setModifiers(Modifier.PUBLIC);

            methodBuffer.delete(0, methodBuffer.length());
            methodBuffer.append("{");
            if (methodName.equals("close"))
                continue;

            boolean existsSQLException = exitsSQLException(ctMethod.getExceptionTypes());
            if (existsSQLException) methodBuffer.append("  try{");
            if (methodName.startsWith("insert") || methodName.startsWith("update") || methodName.startsWith("delete")) {
                if (ctMethod.getReturnType() == CtClass.voidType) {
                    methodBuffer.append("raw." + methodName + "($$);").append(" p.updateAccessTime();");
                } else {
                    methodBuffer.append(ctMethod.getReturnType().getName() + " r=raw." + methodName + "($$);")
                            .append(" p.updateAccessTime();").append(" return r;");
                }
            } else {
                if (ctMethod.getReturnType() == CtClass.voidType) {
                    methodBuffer.append("raw." + methodName + "($$);");
                } else {
                    methodBuffer.append("return raw." + methodName + "($$);");
                }
            }

            if (existsSQLException)
                methodBuffer.append("  }catch(SQLException e){ p.checkSQLException(e);throw e;}");
            methodBuffer.append("}");
            newCtMethodm.setBody(methodBuffer.toString());
            ctResultSetClassProxyClass.addMethod(newCtMethodm);
        }
    }

    private static boolean exitsSQLException(CtClass[] exceptionTypes) throws Exception {
        if (exceptionTypes == null || exceptionTypes.length == 0) return false;
        for (CtClass exceptionClass : exceptionTypes) {
            if ("java.sql.SQLException".equals(exceptionClass.getName()))
                return true;
        }
        return false;
    }
}

