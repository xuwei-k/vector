package com.github.xuwei_k;

/*                     __                                               *\
 **     ________ ___   / /  ___     Scala API                            **
 **    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
 **  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
 ** /____/\___/_/ |_/____/_/ | |                                         **
 **                          |/                                          **
 \*                                                                      */

public class VectorPointer<T> {
    protected int depth;
    protected Object[] display0;
    protected Object[] display1;
    protected Object[] display2;
    protected Object[] display3;
    protected Object[] display4;
    protected Object[] display5;

    public VectorPointer() {
    }

    protected <U> void initFrom(final VectorPointer<U> that) {
        initFrom(that, that.depth);
    }

    private final <U> void initFrom(final VectorPointer<U> that, final int depth) {
        this.depth = depth;
        switch (depth - 1) {
        case -1:
            break;
        case 0:
            display0 = that.display0;
            break;
        case 1:
            display1 = that.display1;
            display0 = that.display0;
            break;
        case 2:
            display2 = that.display2;
            display1 = that.display1;
            display0 = that.display0;
            break;
        case 3:
            display3 = that.display3;
            display2 = that.display2;
            display1 = that.display1;
            display0 = that.display0;
            break;
        case 4:
            display4 = that.display4;
            display3 = that.display3;
            display2 = that.display2;
            display1 = that.display1;
            display0 = that.display0;
            break;
        case 5:
            display5 = that.display5;
            display4 = that.display4;
            display3 = that.display3;
            display2 = that.display2;
            display1 = that.display1;
            display0 = that.display0;
            break;
        }
    }

    protected final T getElem(final int index, final int xor) {
        if (xor < (1 << 5)) { // level = 0
            return (T)display0[index & 31];
        } else if (xor < (1 << 10)) { // level = 1
            return (T)(((Object[])display1[(index >> 5) & 31])[index & 31]);
        } else if (xor < (1 << 15)) { // level = 2
            return (T)(((Object[])(((Object[])display2[(index >> 10) & 31])[(index >> 5) & 31]))[index & 31]);
        } else if (xor < (1 << 20)) { // level = 3
            return (T)(((Object[])((Object[])((Object[])display3[(index >> 15) & 31])[(index >> 10) & 31])[(index >> 5) & 31])[index & 31]);
        } else if (xor < (1 << 25)) { // level = 4
            return (T)(((Object[])((Object[])((Object[])((Object[])display4[(index >> 20) & 31])[(index >> 15) & 31])[(index >> 10) & 31])[(index >> 5) & 31])[index & 31]);
        } else if (xor < (1 << 30)) { // level = 5
            return (T)(((Object[])((Object[])((Object[])((Object[])((Object[])display5[(index >> 25) & 31])[(index >> 20) & 31])[(index >> 15) & 31])[(index >> 10) & 31])[(index >> 5) & 31])[index & 31]);
        } else { // level = 6
            throw new IllegalArgumentException();
        }
    }

    protected final void gotoPos(final int index, final int xor) {
        if (xor < (1 << 5)) { // level = 0 (could maybe removed)
        } else if (xor < (1 << 10)) { // level = 1
            display0 = (Object[])display1[(index >> 5) & 31];
        } else if (xor < (1 << 15)) { // level = 2
            display1 = (Object[])display2[(index >> 10) & 31];
            display0 = (Object[])display1[(index >> 5) & 31];
        } else if (xor < (1 << 20)) { // level = 3
            display2 = (Object[])display3[(index >> 15) & 31];
            display1 = (Object[])display2[(index >> 10) & 31];
            display0 = (Object[])display1[(index >> 5) & 31];
        } else if (xor < (1 << 25)) { // level = 4
            display3 = (Object[])display4[(index >> 20) & 31];
            display2 = (Object[])display3[(index >> 15) & 31];
            display1 = (Object[])display2[(index >> 10) & 31];
            display0 = (Object[])display1[(index >> 5) & 31];
        } else if (xor < (1 << 30)) { // level = 5
            display4 = (Object[])display5[(index >> 25) & 31];
            display3 = (Object[])display4[(index >> 20) & 31];
            display2 = (Object[])display3[(index >> 15) & 31];
            display1 = (Object[])display2[(index >> 10) & 31];
            display0 = (Object[])display1[(index >> 5) & 31];
        } else { // level = 6
            throw new IllegalArgumentException();
        }
    }

    protected final void gotoNextBlockStart(final int index, final int xor) {
        if (xor < (1 << 10)) { // level = 1
            display0 = (Object[])display1[(index >> 5) & 31];
        } else if (xor < (1 << 15)) { // level = 2
            display1 = (Object[])display2[(index >> 10) & 31];
            display0 = (Object[])display1[0];
        } else if (xor < (1 << 20)) { // level = 3
            display2 = (Object[])display3[(index >> 15) & 31];
            display1 = (Object[])display2[0];
            display0 = (Object[])display1[0];
        } else if (xor < (1 << 25)) { // level = 4
            display3 = (Object[])display4[(index >> 20) & 31];
            display2 = (Object[])display3[0];
            display1 = (Object[])display2[0];
            display0 = (Object[])display1[0];
        } else if (xor < (1 << 30)) { // level = 5
            display4 = (Object[])display5[(index >> 25) & 31];
            display3 = (Object[])display4[0];
            display2 = (Object[])display3[0];
            display1 = (Object[])display2[0];
            display0 = (Object[])display1[0];
        } else { // level = 6
            throw new IllegalArgumentException();
        }
    }

    /*
     // xor: oldIndex ^ index
    private[immutable] final def gotoNextBlockStartWritable(index: Int, xor: Int): Unit = { // goto block start pos
      if (xor < (1 << 10)) { // level = 1
        if (depth == 1) { display1 = new Array(32); display1(0) = display0; depth+=1}
        display0 = new Array(32)
        display1((index >>  5) & 31) = display0
      } else
      if (xor < (1 << 15)) { // level = 2
        if (depth == 2) { display2 = new Array(32); display2(0) = display1; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
      } else
      if (xor < (1 << 20)) { // level = 3
        if (depth == 3) { display3 = new Array(32); display3(0) = display2; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display2 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
        display3((index >> 15) & 31) = display2
      } else
      if (xor < (1 << 25)) { // level = 4
        if (depth == 4) { display4 = new Array(32); display4(0) = display3; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display2 = new Array(32)
        display3 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
        display3((index >> 15) & 31) = display2
        display4((index >> 20) & 31) = display3
      } else
      if (xor < (1 << 30)) { // level = 5
        if (depth == 5) { display5 = new Array(32); display5(0) = display4; depth+=1}
        display0 = new Array(32)
        display1 = new Array(32)
        display2 = new Array(32)
        display3 = new Array(32)
        display4 = new Array(32)
        display1((index >>  5) & 31) = display0
        display2((index >> 10) & 31) = display1
        display3((index >> 15) & 31) = display2
        display4((index >> 20) & 31) = display3
        display5((index >> 25) & 31) = display4
      } else { // level = 6
        throw new IllegalArgumentException()
      }
    }

    */

    // STUFF BELOW USED BY APPEND / UPDATE
    private final <X> X copyOf(final Object[] a) {
        if (a == null){
            System.out.println("NULL");
        }
        final Object[] b = new Object[a.length];
        System.arraycopy(a, 0, b, 0, a.length);
        return (X) b;
    }

    private final <X> X nullSlotAndCopy(final Object[] array, final int index) {
        final Object x = array[index];
        array[index] = null;
        return (X) copyOf((Object[]) x);
    }

    protected final void stabilize(final int index) {
        switch (depth - 1) {
        case 5:
            display5 = copyOf(display5);
            display4 = copyOf(display4);
            display3 = copyOf(display3);
            display2 = copyOf(display2);
            display1 = copyOf(display1);
            display5[(index >> 25) & 31] = display4;
            display4[(index >> 20) & 31] = display3;
            display3[(index >> 15) & 31] = display2;
            display2[(index >> 10) & 31] = display1;
            display1[(index >> 5) & 31] = display0;
            break;
        case 4:
            display4 = copyOf(display4);
            display3 = copyOf(display3);
            display2 = copyOf(display2);
            display1 = copyOf(display1);
            display4[(index >> 20) & 31] = display3;
            display3[(index >> 15) & 31] = display2;
            display2[(index >> 10) & 31] = display1;
            display1[(index >> 5) & 31] = display0;
            break;
        case 3:
            display3 = copyOf(display3);
            display2 = copyOf(display2);
            display1 = copyOf(display1);
            display3[(index >> 15) & 31] = display2;
            display2[(index >> 10) & 31] = display1;
            display1[(index >> 5) & 31] = display0;
            break;
        case 2:
            display2 = copyOf(display2);
            display1 = copyOf(display1);
            display2[(index >> 10) & 31] = display1;
            display1[(index >> 5) & 31] = display0;
            break;
        case 1:
            display1 = copyOf(display1);
            display1[(index >> 5) & 31] = display0;
            break;
        case 0:
        }
    }

    protected final void gotoPosWritable0(final int newIndex, final int xor) {
        switch (depth - 1) {
        case 5:
            display5 = copyOf(display5);
            display4 = nullSlotAndCopy(display5, (newIndex >> 25) & 31);
            display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31);
            display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31);
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
            break;
        case 4:
            display4 = copyOf(display4);
            display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31);
            display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31);
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
            break;
        case 3:
            display3 = copyOf(display3);
            display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31);
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
            break;
        case 2:
            display2 = copyOf(display2);
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
            break;
        case 1:
            display1 = copyOf(display1);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
            break;
        case 0:
            display0 = copyOf(display0);
            break;
        }
    }

    protected final void gotoPosWritable1(final int oldIndex, final int newIndex, final int xor) {
        if (xor < (1 << 5)) { // level = 0
            display0 = copyOf(display0);
        } else if (xor < (1 << 10)) { // level = 1
            display1 = copyOf(display1);
            display1[(oldIndex >> 5) & 31] = display0;
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
        } else if (xor < (1 << 15)) { // level = 2
            display1 = copyOf(display1);
            display2 = copyOf(display2);
            display1[(oldIndex >> 5) & 31] = display0;
            display2[(oldIndex >> 10) & 31] = display1;
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
        } else if (xor < (1 << 20)) { // level = 3
            display1 = copyOf(display1);
            display2 = copyOf(display2);
            display3 = copyOf(display3);
            display1[(oldIndex >> 5) & 31] = display0;
            display2[(oldIndex >> 10) & 31] = display1;
            display3[(oldIndex >> 15) & 31] = display2;
            display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31);
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
        } else if (xor < (1 << 25)) { // level = 4
            display1 = copyOf(display1);
            display2 = copyOf(display2);
            display3 = copyOf(display3);
            display4 = copyOf(display4);
            display1[(oldIndex >> 5) & 31] = display0;
            display2[(oldIndex >> 10) & 31] = display1;
            display3[(oldIndex >> 15) & 31] = display2;
            display4[(oldIndex >> 20) & 31] = display3;
            display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31);
            display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31);
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
        } else if (xor < (1 << 30)) { // level = 5
            display1 = copyOf(display1);
            display2 = copyOf(display2);
            display3 = copyOf(display3);
            display4 = copyOf(display4);
            display5 = copyOf(display5);
            display1[(oldIndex >> 5) & 31] = display0;
            display2[(oldIndex >> 10) & 31] = display1;
            display3[(oldIndex >> 15) & 31] = display2;
            display4[(oldIndex >> 20) & 31] = display3;
            display5[(oldIndex >> 25) & 31] = display4;
            display4 = nullSlotAndCopy(display5, (newIndex >> 25) & 31);
            display3 = nullSlotAndCopy(display4, (newIndex >> 20) & 31);
            display2 = nullSlotAndCopy(display3, (newIndex >> 15) & 31);
            display1 = nullSlotAndCopy(display2, (newIndex >> 10) & 31);
            display0 = nullSlotAndCopy(display1, (newIndex >> 5) & 31);
        } else { // level = 6
            throw new IllegalArgumentException();
        }
    }

    protected final Object[] copyRange(final Object[] array, final int oldLeft, final int newLeft) {
        final Object[] elems = new Object[32];
        System.arraycopy(array, oldLeft, elems, newLeft, 32 - Math.max(newLeft, oldLeft));
        return elems;
    }

    protected final void gotoFreshPosWritable0(final int oldIndex, final int newIndex, final int xor) {
        if (xor < (1 << 5)) { // level = 0
            // println("XXX clean with low xor")
        } else if (xor < (1 << 10)) { // level = 1
            if (depth == 1) {
                display1 = new Object[32];
                display1[(oldIndex >> 5) & 31] = display0;
                depth++;
            }
            display0 = new Object[32];
        } else if (xor < (1 << 15)) { // level = 2
            if (depth == 2) {
                display2 = new Object[32];
                display2[(oldIndex >> 10) & 31] = display1;
                depth++;
            }
            display1 = (Object[])display2[(newIndex >> 10) & 31];
            if (display1 == null)
                display1 = new Object[32];
            display0 = new Object[32];
        } else if (xor < (1 << 20)) { // level = 3
            if (depth == 3) {
                display3 = new Object[32];
                display3[(oldIndex >> 15) & 31] = display2;
                display2 = new Object[32];
                display1 = new Object[32];
                depth++;
            }
            display2 = (Object[])display3[(newIndex >> 15) & 31];
            if (display2 == null)
                display2 = new Object[32];
            display1 = (Object[])display2[(newIndex >> 10) & 31];
            if (display1 == null)
                display1 = new Object[32];
            display0 = new Object[32];
        } else if (xor < (1 << 25)) { // level = 4
            if (depth == 4) {
                display4 = new Object[32];
                display4[(oldIndex >> 20) & 31] = display3;
                display3 = new Object[32];
                display2 = new Object[32];
                display1 = new Object[32];
                depth++;
            }
            display3 = (Object[])display4[(newIndex >> 20) & 31];
            if (display3 == null)
                display3 = new Object[32];
            display2 = (Object[])display3[(newIndex >> 15) & 31];
            if (display2 == null)
                display2 = new Object[32];
            display1 = (Object[])display2[(newIndex >> 10) & 31];
            if (display1 == null)
                display1 = new Object[32];
            display0 = new Object[32];
        } else if (xor < (1 << 30)) { // level = 5
            if (depth == 5) {
                display5 = new Object[32];
                display5[(oldIndex >> 25) & 31] = display4;
                display4 = new Object[32];
                display3 = new Object[32];
                display2 = new Object[32];
                display1 = new Object[32];
                depth += 1;
            }
            display4 = (Object[])display5[(newIndex >> 20) & 31];
            if (display4 == null)
                display4 = new Object[32];
            display3 = (Object[])display4[(newIndex >> 20) & 31];
            if (display3 == null)
                display3 = new Object[32];
            display2 = (Object[])display3[(newIndex >> 15) & 31];
            if (display2 == null)
                display2 = new Object[32];
            display1 = (Object[])display2[(newIndex >> 10) & 31];
            if (display1 == null)
                display1 = new Object[32];
            display0 = new Object[32];
        } else { // level = 6
            throw new IllegalArgumentException();
        }
    }

    protected final void gotoFreshPosWritable1(final int oldIndex, final int newIndex, final int xor) {
        stabilize(oldIndex);
        gotoFreshPosWritable0(oldIndex, newIndex, xor);
    }

}
