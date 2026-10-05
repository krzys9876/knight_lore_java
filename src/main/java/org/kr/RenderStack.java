package org.kr;

public class RenderStack {
    private static final int SIZE = 40;
    private DataBlock list = new DataBlock(0, SIZE);

    public RenderStack() { reset(); }

    public void reset() { list.set(0, 0xFF); }
    public boolean isEmpty() { return list.getA(0)==0xFF;}

    public boolean contains(int element) {
        int i=0;
        while(i<SIZE && list.getA(i)!=0xFF) {
            if(list.getA(i)==element) return true;
            i++;
        }
        return false;
    }

    public boolean isLast(int element) {
        int i=0;
        while(i<40 && list.getA(i)!=0xFF) i++;
        return i>0 && list.getA(i-1)==element;
    }

    public void add(int element) {
        int i=0;
        while(i<40 && list.getA(i)!=0xFF) {
            if(list.getA(i)==element) return;
            i++;
        }
        list.set(i, element);
        list.set(i+1, 0xFF);
    }
}
