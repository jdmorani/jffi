/*
 * Copyright (C) 2009 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jffi;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallContextCache {

    private final Map<Signature, CallContextRef> contextCache = new ConcurrentHashMap<Signature, CallContextRef>();
    private final ReferenceQueue<CallContext> contextReferenceQueue = new ReferenceQueue<CallContext>();

    /** Holder class to do lazy allocation of the ClosureManager instance */
    private static final class SingletonHolder {
        static final CallContextCache INSTANCE = new CallContextCache();
    }

    /**
     * Gets the global instance of the <tt>CallContextCache</tt>
     *
     * @return An instance of a <tt>CallContextCache</tt>
     */
    public static final CallContextCache getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /** Constructs a ClosureManager */
    private CallContextCache() { }

    public final CallContext getCallContext(Type returnType, Type[] parameterTypes, CallingConvention convention) {
        Signature signature = new Signature(returnType, parameterTypes, convention);
        CallContextRef ref = contextCache.get(signature);
        CallContext ctx;

        if (ref != null && (ctx = ref.get()) != null) {
            return ctx;
        }

        // Cull any dead references
        while ((ref = (CallContextRef) contextReferenceQueue.poll()) != null) {
            contextCache.remove(ref.signature);
        }

        ctx = new CallContext(returnType, (Type[]) parameterTypes.clone(), convention);
        contextCache.put(signature, new CallContextRef(signature, ctx, contextReferenceQueue));

        return ctx;
    }

    private static final class CallContextRef extends SoftReference<CallContext> {

        final Signature signature;

        public CallContextRef(Signature signature, CallContext ctx, ReferenceQueue<CallContext> queue) {
            super(ctx, queue);
            this.signature = signature;
        }
    }

    private static final class Signature {

        /**
         * Keep references to the return and parameter types so they do not get
         * garbage collected until the closure does.
         */
        private final Type returnType;
        private final Type[] parameterTypes;
        private final CallingConvention convention;
        private int hashCode = 0;

        public Signature(Type returnType, Type[] parameterTypes, CallingConvention convention) {
            if (returnType == null || parameterTypes == null) {
                throw new NullPointerException("null return type or parameter types array");
            }
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
            this.convention = convention;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            final Signature other = (Signature) obj;

            if (this.convention != other.convention) {
                return false;
            }

            if (this.returnType != other.returnType && !this.returnType.equals(other.returnType)) {
                return false;
            }

            if (this.parameterTypes.length == other.parameterTypes.length) {
                for (int i = 0; i < this.parameterTypes.length; ++i) {
                    if (this.parameterTypes[i] != other.parameterTypes[i] && (this.parameterTypes[i] == null || !this.parameterTypes[i].equals(other.parameterTypes[i]))) {
                        return false;
                    }
                }
                // All param types are same, return type is same, convention is same, so this is the same signature
                return true;
            }

            return false;
        }

        private final int calculateHashCode() {
            int hash = 7;
            hash = 53 * hash + (this.returnType != null ? this.returnType.hashCode() : 0);
            int paramHash = 1;
            for (int i = 0; i < parameterTypes.length; ++i) {
                paramHash = 31 * paramHash + parameterTypes[i].hashCode();
            }
            hash = 53 * hash + paramHash;
            hash = 53 * hash + this.convention.hashCode();
            return hash;
        }

        @Override
        public int hashCode() {
            return hashCode != 0 ? hashCode : (hashCode = calculateHashCode());
        }
    }
}
