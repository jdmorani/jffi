
package com.kenai.jffi;

/**
 * Flags to use when adding an array as a pointer parameter
 */
public final class ArrayFlags {

    /* Stop ArrayFlags from being intantiated */
    private ArrayFlags() {}

    /** Copy the array contents to native memory before calling the function */
    public static final int IN = ObjectBuffer.IN;

    /** After calling the function, reload the array contents from native memory */
    public static final int OUT = ObjectBuffer.OUT;

    /** Pin the array memory and pass the JVM memory pointer directly to the function */
    public static final int PINNED = ObjectBuffer.PINNED;

    /** Append a NUL byte to the array contents after copying to native memory */
    public static final int NULTERMINATE = ObjectBuffer.ZERO_TERMINATE;

    /** For OUT arrays, clear the native memory area before passing to the native function */
    public static final int CLEAR = ObjectBuffer.CLEAR;

    /**
     * Tests if the flags indicate data should be copied from native memory.
     *
     * @param flags The array flags.  Any combination of IN | OUT | PINNED | NULTERMINATE.
     * @return <tt>true</tt> If array data should be copied from native memory.
     */
    public static final boolean isOut(int flags) {
        return (flags & (OUT | IN)) != IN;
    }

    /**
     * Tests if the flags indicate data should be copied to native memory.
     *
     * @param flags The array flags.  Any combination of IN | OUT | PINNED | NULTERMINATE.
     * @return <tt>true</tt> If array data should be copied to native memory.
     */
    public static final boolean isIn(int flags) {
        return (flags & (OUT | IN)) != OUT;
    }
}