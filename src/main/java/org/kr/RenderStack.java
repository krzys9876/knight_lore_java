package org.kr;

public class RenderStack {
    private static final int SIZE = 40;
    private DataBlock list = new DataBlock(0, SIZE);

    public RenderStack() { reset(); }

    public void reset() { list.set(0, 0xFF); }
    public boolean isEmpty() { return list.get(0)==0xFF;}

    public boolean contains(int element) {
        int i=0;
        while(i<SIZE && list.get(i)!=0xFF) {
            if(list.get(i)==element) return true;
            i++;
        }
        return false;
    }

    public boolean isLast(int element) {
        int i=0;
        while(i<40 && list.get(i)!=0xFF) i++;
        return i>0 && list.get(i-1)==element;
    }

    public void add(int element) {
        int i=0;
        while(i<40 && list.get(i)!=0xFF) {
            if(list.get(i)==element) return;
            i++;
        }
        list.set(i, element);
        list.set(i+1, 0xFF);
    }
}
