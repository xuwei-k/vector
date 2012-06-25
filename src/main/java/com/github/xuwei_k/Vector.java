package com.github.xuwei_k;

import java.util.Iterator;

/*                     __                                               *\
 **     ________ ___   / /  ___     Scala API                            **
 **    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
 **  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
 ** /____/\___/_/ |_/____/_/ | |                                         **
 **                          |/                                          **
 \*                                                                      */

final public class Vector<A> extends VectorPointer<A> implements Iterable<A>{

    private final int startIndex, endIndex, focus;

    private static final Vector<?> NIL = new Vector<Object>(0, 0, 0);

    public static final <A> Vector<A> empty() {
        return (Vector<A>) NIL;
    }

    public static <X> Vector<X> apply(final X... xs){
        Vector<X> v = empty();
        for(final X x:xs){
            v = v.prepend(x);
        }
        return v;
    }

    Vector(final int startIndex, final int endIndex, final int focus) {
        super();
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.focus = focus;
        this.length = endIndex - startIndex;
    }

    private boolean dirty = false;

    public final int length;

    public int lengthCompare(final int len) {
        return length - len;
    }

    private void initIterator(final VectorIterator<A> s) {
        s.initFrom(this);
        if (dirty)
            s.stabilize(focus);
        if (s.depth > 1)
            s.gotoPos(startIndex, startIndex ^ focus);
    }

    public VectorIterator<A> iterator() {
        final VectorIterator<A> s = new VectorIterator<A>(startIndex, endIndex);
        initIterator(s);
        return s;
    }

    @Override
    public String toString(){
        final StringBuilder buf = new StringBuilder();
        for(final A elem:this){
            buf.append(elem + ",");
        }
        return buf.toString();
    }

    public Iterator<A> reverseIterator() {
        return new Iterator<A>() {
            private int i = Vector.this.length;

            public boolean hasNext() {
                return 0 < i;
            }

            @Override
            public A next() {
                if (0 < i) {
                    i--;
                    return apply(i);
                } else {
                    throw new Error();
                }
            }

            @Override
            public void remove() {
                throw new NoSuchMethodError();
            }
        };
    }

    public A apply(final int index) {
        final int idx = checkRangeConvert(index);
        System.out.println("get elem: " + index + "/" + idx + "(focus:" + focus + " xor:" + (idx ^ focus) + " depth:" + depth + ")");
        return getElem(idx, idx ^ focus);
    }

    private int checkRangeConvert(final int index) {
        final int idx = index + startIndex;
        if (0 <= index && idx < endIndex) {
            return idx;
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(idx));
        }
    }

    public Vector<A> updated(final int index, final A elem) {
        return updateAt(index, elem);
    }

    public Vector<A> append(final A elem) {
        return appendFront(elem);
    }

    public Vector<A> prepend(final A elem) {
        return appendBack(elem);
    }

    public Vector<A> take(final int n) {
        if (n <= 0)
            return empty();
        else if (startIndex + n < endIndex)
            return dropBack0(startIndex + n);
        else
            return this;
    }

    public Vector<A> drop(final int n) {
        if (n <= 0)
            return this;
        else if (startIndex + n < endIndex)
            return dropFront0(startIndex + n);
        else
            return Vector.empty();
    }

    public Vector<A> takeRight(final int n) {
        if (n <= 0)
            return empty();
        else if (endIndex - n > startIndex)
            return dropFront0(endIndex - n);
        else
            return this;
    }

    public Vector<A> dropRight(final int n) {
        if (n <= 0)
            return this;
        else if (endIndex - n > startIndex)
            return dropBack0(endIndex - n);
        else
            return empty();
    }

    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    public A head() {
        if (isEmpty())
            throw new UnsupportedOperationException("empty.head");
        return apply(0);
    }

    public Vector<A> tail() {
        if (isEmpty())
            throw new UnsupportedOperationException("empty.tail");
        return drop(1);
    }

    public A last() {
        if (isEmpty())
            throw new UnsupportedOperationException("empty.last");
        return apply(length - 1);
    }

    public Vector<A> init() {
        if (isEmpty())
            throw new UnsupportedOperationException("empty.init");
        return dropRight(1);
    }

    public Vector<A> slice(final int from, final int until) {
        return take(until).drop(from);
    }

    // TODO Tuple
    // pubic splitAt(n: Int): (Vector[A], Vector[A]) = (take(n), drop(n))

    /*
     * TODO public Vector<A> concat(final Vector<A> that){ super.++(that.seq); }
     */

    private Vector<A> updateAt(final int index, final A elem) {
        final int idx = checkRangeConvert(index);
        final Vector<A> s = new Vector<A>(startIndex, endIndex, idx);
        s.initFrom(this);
        s.dirty = dirty;
        s.gotoPosWritable(focus, idx, focus ^ idx);
        s.display0[idx & 0x1f] = (Object) elem;
        return s;
    }

    private void gotoPosWritable(final int oldIndex, final int newIndex, final int xor) {
        if (dirty) {
            gotoPosWritable1(oldIndex, newIndex, xor);
        } else {
            gotoPosWritable0(newIndex, xor);
            dirty = true;
        }
    }

    private void gotoFreshPosWritable(final int oldIndex, final int newIndex, final int xor) {
        if (dirty) {
            gotoFreshPosWritable1(oldIndex, newIndex, xor);
        } else {
            gotoFreshPosWritable0(oldIndex, newIndex, xor);
            dirty = true;
        }
    }

    private Vector<A> appendFront(final A value) {
        if (endIndex != startIndex) {
            int blockIndex = (startIndex - 1) & ~31;
            int lo = (startIndex - 1) & 31;

            if (startIndex != blockIndex + 32) {
                final Vector<A> s = new Vector<A>(startIndex - 1, endIndex, blockIndex);
                s.initFrom(this);
                s.dirty = dirty;
                s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex);
                s.display0[lo] = (Object) value;
                return s;
            } else {

                final int freeSpace = ((1 << 5 * (depth)) - endIndex);
                final int shift = freeSpace & ~((1 << 5 * (depth - 1)) - 1);
                final int shiftBlocks = freeSpace >>> 5 * (depth - 1);
                if (shift != 0) {
                    if (depth > 1) {
                        final int newBlockIndex = blockIndex + shift;
                        final int newFocus = focus + shift;
                        final Vector<A> s = new Vector<A>(startIndex - 1 + shift, endIndex + shift, newBlockIndex);
                        s.initFrom(this);
                        s.dirty = dirty;
                        s.shiftTopLevel(0, shiftBlocks);
                        s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex);
                        s.display0[lo] = value;
                        return s;
                    } else {
                        final int newBlockIndex = blockIndex + 32;
                        final int newFocus = focus;
                        final Vector<A> s = new Vector<A>(startIndex - 1 + shift, endIndex + shift, newBlockIndex);
                        s.initFrom(this);
                        s.dirty = dirty;
                        s.shiftTopLevel(0, shiftBlocks);
                        s.gotoPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex);
                        s.display0[shift - 1] = value;
                        return s;
                    }
                } else if (blockIndex < 0) {
                    final int move = (1 << 5 * (depth + 1)) - (1 << 5 * (depth));
                    final int newBlockIndex = blockIndex + move;
                    final int newFocus = focus + move;

                    final Vector<A> s = new Vector<A>(startIndex - 1 + move, endIndex + move, newBlockIndex);
                    s.initFrom(this);
                    s.dirty = dirty;
                    s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex);
                    s.display0[lo] = value;
                    return s;
                } else {
                    final int newBlockIndex = blockIndex;
                    final int newFocus = focus;
                    final Vector<A> s = new Vector<A>(startIndex - 1, endIndex, newBlockIndex);
                    s.initFrom(this);
                    s.dirty = dirty;
                    s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex);
                    s.display0[lo] = value;
                    return s;
                }
            }
        } else {
            final Object[] elems = new Object[32];
            elems[31] = value;
            final Vector<A> s = new Vector<A>(31, 32, 0);
            s.depth = 1;
            s.display0 = elems;
            return s;
        }
    }

    private Vector<A> appendBack(final A value) {
        if (endIndex != startIndex) {
            int blockIndex = endIndex & ~31;
            int lo = endIndex & 31;

            if (endIndex != blockIndex) {
                final Vector<A> s = new Vector<A>(startIndex, endIndex + 1, blockIndex);
                s.initFrom(this);
                s.dirty = dirty;
                s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex);
                s.display0[lo] = value;
                return s;
            } else {
                final int shift = startIndex & ~((1 << 5 * (depth - 1)) - 1);
                final int shiftBlocks = startIndex >>> 5 * (depth - 1);

                if (shift != 0) {
                    if (depth > 1) {
                        final int newBlockIndex = blockIndex - shift;
                        final int newFocus = focus - shift;
                        final Vector<A> s = new Vector<A>(startIndex - shift, endIndex + 1 - shift, newBlockIndex);
                        s.initFrom(this);
                        s.dirty = dirty;
                        s.shiftTopLevel(shiftBlocks, 0);
                        s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex);
                        s.display0[lo] = value;

                        return s;
                    } else {
                        final int newBlockIndex = blockIndex - 32;
                        final int newFocus = focus;

                        final Vector<A> s = new Vector<A>(startIndex - shift, endIndex + 1 - shift, newBlockIndex);
                        s.initFrom(this);
                        s.dirty = dirty;
                        s.shiftTopLevel(shiftBlocks, 0);
                        s.gotoPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex);
                        s.display0[32 - shift] = value;
                        return s;
                    }
                } else {
                    final int newBlockIndex = blockIndex;
                    final int newFocus = focus;

                    final Vector<A> s = new Vector<A>(startIndex, endIndex + 1, newBlockIndex);
                    s.initFrom(this);
                    s.dirty = dirty;
                    s.gotoFreshPosWritable(newFocus, newBlockIndex, newFocus ^ newBlockIndex);
                    s.display0[lo] = value;
                    if (s.depth == depth + 1) {
                        // println("creating new level " + s.depth +
                        // " (had "+0+" free space)")
                    }
                    return s;
                }
            }
        } else {
            final Object[] elems = new Object[32];
            elems[0] = (Object) value;
            final Vector<A> s = new Vector<A>(0, 1, 0);
            s.depth = 1;
            s.display0 = elems;
            return s;
        }
    }

    // low-level implementation (needs cleanup, maybe move to util class)
    private void shiftTopLevel(final int oldLeft, final int newLeft) {
        switch (depth - 1) {
        case 0:
            display0 = copyRange(display0, oldLeft, newLeft);
            break;
        case 1:
            display1 = (Object[][]) copyRange(display1, oldLeft, newLeft);
            break;
        case 2:
            display2 = (Object[][][]) copyRange(display2, oldLeft, newLeft);
            break;
        case 3:
            display3 = (Object[][][][]) copyRange(display3, oldLeft, newLeft);
            break;
        case 4:
            display4 = (Object[][][][][]) copyRange(display4, oldLeft, newLeft);
            break;
        case 5:
            display5 = (Object[][][][][][]) copyRange(display5, oldLeft, newLeft);
            break;
        }
    }

    private void zeroLeft(final Object[] array, final int index) {
        for (int i = 0; i < index; i++) {
            array[i] = null;
        }
    }

    private void zeroRight(final Object[] array, final int index) {
        for (int i = index; i < array.length; i++) {
            array[i] = null;
        }
    }

    private <X> X copyLeft(final Object[] array, final int right) {
        final Object[] a2 = new Object[array.length];
        System.arraycopy(array, 0, a2, 0, right);
        return (X) a2;
    }

    private <X> X copyRight(final Object[] array, final int left) {
        final Object[] a2 = new Object[array.length];
        System.arraycopy(array, left, a2, left, a2.length - left);
        return (X) a2;
    }

    private void preClean(final int depth) {
        this.depth = depth;
        switch (depth - 1) {
        case 0:
            display1 = null;
            display2 = null;
            display3 = null;
            display4 = null;
            display5 = null;
            break;
        case 1:
            display2 = null;
            display3 = null;
            display4 = null;
            display5 = null;
            break;
        case 2:
            display3 = null;
            display4 = null;
            display5 = null;
            break;
        case 3:
            display4 = null;
            display5 = null;
            break;
        case 4:
            display5 = null;
            break;
        case 5:
        }
    }

    private void cleanLeftEdge(final int cutIndex) {
        if (cutIndex < (1 << 5)) {
            zeroLeft(display0, cutIndex);
        } else if (cutIndex < (1 << 10)) {
            zeroLeft(display0, cutIndex & 0x1f);
            display1 = copyRight(display1, (cutIndex >>> 5));
        } else if (cutIndex < (1 << 15)) {
            zeroLeft(display0, cutIndex & 0x1f);
            display1 = copyRight(display1, (cutIndex >>> 5) & 0x1f);
            display2 = copyRight(display2, (cutIndex >>> 10));
        } else if (cutIndex < (1 << 20)) {
            zeroLeft(display0, cutIndex & 0x1f);
            display1 = copyRight(display1, (cutIndex >>> 5) & 0x1f);
            display2 = copyRight(display2, (cutIndex >>> 10) & 0x1f);
            display3 = copyRight(display3, (cutIndex >>> 15));
        } else if (cutIndex < (1 << 25)) {
            zeroLeft(display0, cutIndex & 0x1f);
            display1 = copyRight(display1, (cutIndex >>> 5) & 0x1f);
            display2 = copyRight(display2, (cutIndex >>> 10) & 0x1f);
            display3 = copyRight(display3, (cutIndex >>> 15) & 0x1f);
            display4 = copyRight(display4, (cutIndex >>> 20));
        } else if (cutIndex < (1 << 30)) {
            zeroLeft(display0, cutIndex & 0x1f);
            display1 = copyRight(display1, (cutIndex >>> 5) & 0x1f);
            display2 = copyRight(display2, (cutIndex >>> 10) & 0x1f);
            display3 = copyRight(display3, (cutIndex >>> 15) & 0x1f);
            display4 = copyRight(display4, (cutIndex >>> 20) & 0x1f);
            display5 = copyRight(display5, (cutIndex >>> 25));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void cleanRightEdge(final int cutIndex) {
        if (cutIndex <= (1 << 5)) {
            zeroRight(display0, cutIndex);
        } else if (cutIndex <= (1 << 10)) {
            zeroRight(display0, ((cutIndex - 1) & 0x1f) + 1);
            display1 = copyLeft(display1, (cutIndex >>> 5));
        } else if (cutIndex <= (1 << 15)) {
            zeroRight(display0, ((cutIndex - 1) & 0x1f) + 1);
            display1 = copyLeft(display1, (((cutIndex - 1) >>> 5) & 0x1f) + 1);
            display2 = copyLeft(display2, (cutIndex >>> 10));
        } else if (cutIndex <= (1 << 20)) {
            zeroRight(display0, ((cutIndex - 1) & 0x1f) + 1);
            display1 = copyLeft(display1, (((cutIndex - 1) >>> 5) & 0x1f) + 1);
            display2 = copyLeft(display2, (((cutIndex - 1) >>> 10) & 0x1f) + 1);
            display3 = copyLeft(display3, (cutIndex >>> 15));
        } else if (cutIndex <= (1 << 25)) {
            zeroRight(display0, ((cutIndex - 1) & 0x1f) + 1);
            display1 = copyLeft(display1, (((cutIndex - 1) >>> 5) & 0x1f) + 1);
            display2 = copyLeft(display2, (((cutIndex - 1) >>> 10) & 0x1f) + 1);
            display3 = copyLeft(display3, (((cutIndex - 1) >>> 15) & 0x1f) + 1);
            display4 = copyLeft(display4, (cutIndex >>> 20));
        } else if (cutIndex <= (1 << 30)) {
            zeroRight(display0, ((cutIndex - 1) & 0x1f) + 1);
            display1 = copyLeft(display1, (((cutIndex - 1) >>> 5) & 0x1f) + 1);
            display2 = copyLeft(display2, (((cutIndex - 1) >>> 10) & 0x1f) + 1);
            display3 = copyLeft(display3, (((cutIndex - 1) >>> 15) & 0x1f) + 1);
            display4 = copyLeft(display4, (((cutIndex - 1) >>> 20) & 0x1f) + 1);
            display5 = copyLeft(display5, (cutIndex >>> 25));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private int requiredDepth(final int xor) {
        if (xor < (1 << 5))
            return 1;
        else if (xor < (1 << 10))
            return 2;
        else if (xor < (1 << 15))
            return 3;
        else if (xor < (1 << 20))
            return 4;
        else if (xor < (1 << 25))
            return 5;
        else if (xor < (1 << 30))
            return 6;
        else
            throw new IllegalArgumentException();
    }

    private Vector<A> dropFront0(final int cutIndex) {
        int blockIndex = cutIndex & ~31;
        int lo = cutIndex & 31;
        final int xor = cutIndex ^ (endIndex - 1);
        final int d = requiredDepth(xor);
        final int shift = (cutIndex & ~((1 << (5 * d)) - 1));
        final Vector<A> s = new Vector<A>(cutIndex - shift, endIndex - shift, blockIndex - shift);
        s.initFrom(this);
        s.dirty = dirty;
        s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex);
        s.preClean(d);
        s.cleanLeftEdge(cutIndex - shift);
        return s;
    }

    private Vector<A> dropBack0(final int cutIndex) {
        int blockIndex = (cutIndex - 1) & ~31;
        int lo = ((cutIndex - 1) & 31) + 1;

        final int xor = startIndex ^ (cutIndex - 1);
        final int d = requiredDepth(xor);
        final int shift = (startIndex & ~((1 << (5 * d)) - 1));

        final Vector<A> s = new Vector<A>(startIndex - shift, cutIndex - shift, blockIndex - shift);
        s.initFrom(this);
        s.dirty = dirty;
        s.gotoPosWritable(focus, blockIndex, focus ^ blockIndex);
        s.preClean(d);
        s.cleanRightEdge(cutIndex - shift);
        return s;
    }

}

