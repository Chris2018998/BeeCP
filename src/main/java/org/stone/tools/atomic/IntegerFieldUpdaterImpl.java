/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Copyright(C) Chris2018998,All rights reserved.
 *
 * Project owner contact:Chris2018998@tom.com.
 *
 * Project Licensed under Apache License v2.0.
 */
package org.stone.tools.atomic;

import org.stone.tools.unsafe.UnsafeAdaptor;
import org.stone.tools.unsafe.UnsafeAdaptorHolder;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Atomic Integer Field Updater Implementation(Don't use in other place)
 *
 * @author Chris Liao
 * @version 1.0
 */
public final class IntegerFieldUpdaterImpl<T> extends AtomicIntegerFieldUpdater<T> {
    private final static UnsafeAdaptor UA = UnsafeAdaptorHolder.U;
    private final long offset;

    private IntegerFieldUpdaterImpl(long offset) {
        this.offset = offset;
    }

    public static <T> AtomicIntegerFieldUpdater<T> newUpdater(Class<T> beanClass, String fieldName) {
        try {
            return new IntegerFieldUpdaterImpl<T>(UA.objectFieldOffset(beanClass.getDeclaredField(fieldName)));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw e;
        } catch (Throwable e) {
            return AtomicIntegerFieldUpdater.newUpdater(beanClass, fieldName);
        }
    }

    public final boolean compareAndSet(T bean, int expect, int update) {
        return UA.compareAndSwapInt(bean, this.offset, expect, update);
    }

    public final boolean weakCompareAndSet(T bean, int expect, int update) {
        return UA.compareAndSwapInt(bean, this.offset, expect, update);
    }

    public final void set(T bean, int newValue) {
        UA.putIntVolatile(bean, this.offset, newValue);
    }

    public final void lazySet(T bean, int newValue) {
        UA.putOrderedInt(bean, this.offset, newValue);
    }

    public final int get(T bean) {
        return UA.getIntVolatile(bean, this.offset);
    }

}