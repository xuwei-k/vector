package com.github.xuwei_k;

import java.util.NoSuchElementException;

/*                     __                                               *\
 **     ________ ___   / /  ___     Scala API                            **
 **    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
 **  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
 ** /____/\___/_/ |_/____/_/ | |                                         **
 **                          |/                                          **
 \*                                                                      */

public class VectorIterator<A> extends VectorPointer<A> implements java.util.Iterator<A> {

    public VectorIterator(final int start, final int end) {
        this._startIndex = start;
        this._endIndex = end;
        blockIndex = _startIndex & ~31;
        lo = _startIndex & 31;
        endIndex = _endIndex;
        endLo = Math.min(endIndex - blockIndex, 32);
        _hasNext = blockIndex + lo < endIndex;
    }

    private final int _startIndex, _endIndex;
    private int blockIndex, lo, endIndex;

    private int endLo;

    @Override
    public boolean hasNext() {
        return _hasNext;
    }

    private boolean _hasNext;

    @Override
    public A next() {
        if (!_hasNext){
            throw new NoSuchElementException("reached iterator end");
        }

        final A res = (A) display0[lo];
        lo ++;

        if (lo == endLo) {
            if (blockIndex + lo < endIndex) {
                final int newBlockIndex = blockIndex + 32;
                gotoNextBlockStart(newBlockIndex, blockIndex ^ newBlockIndex);

                blockIndex = newBlockIndex;
                endLo = Math.min(endIndex - blockIndex, 32);
                lo = 0;
            } else {
                _hasNext = false;
            }
        }

        return res;
    }

    private int remainingElementCount() {
        return Math.max((_endIndex - (blockIndex + lo)), 0);
    }

    private Vector<A> remainingVector() {
        final Vector<A> v = new Vector<A>(blockIndex + lo, _endIndex, blockIndex + lo);
        v.initFrom(this);
        return v;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

