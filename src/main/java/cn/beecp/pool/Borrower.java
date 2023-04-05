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

/**
 * Pool Connection borrower
 *
 * @author Chris Liao
 * @version 1.0
 */
final class Borrower {
    final Thread thread = Thread.currentThread();
    volatile Object state;
    PooledConnection lastUsed;
}