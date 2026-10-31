package org.kr.game.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

/*
; #TABLE(default,centre,:w)
        ; { =h Byte(n) | =h Description }
        ; { +0 | object graphic no (dynamic $60-$67) }
        ; { +1 | start X position ($46-0xba) }
        ; { +2 | start Y position ($40-$70 +/-0x0c) }
        ; { +3 | start Z position ($46-0xba) }
        ; { +4 | start screen }
        ; { +5 | current X }
        ; { +6 | current Y }
        ; { +7 | current Z }
        ; { +8 | current screen }
*/

public class SpecialObjects {
    private final DataBlock special_objs_tbl_6FF2;
    public int start() { return special_objs_tbl_6FF2.start; }
    public int[] getCopy() { return special_objs_tbl_6FF2.getCopy(); }

    public final int size;

    private final SpecialObject[] objects;

    public SpecialObjects() {
        special_objs_tbl_6FF2 = InitialData.block("special_objs_tbl_6FF2");
        size = special_objs_tbl_6FF2.size / 9;
        objects = new SpecialObject[size];

        // Initialize objects from initial memory contents
        for(int i = 0; i < size; i++) {
            int address = getAddress(i);
            int grNo = special_objs_tbl_6FF2.getU(address);
            SpecialObjectLocation start = new SpecialObjectLocation(
                    special_objs_tbl_6FF2.getU(address + 1),
                    special_objs_tbl_6FF2.getU(address + 2),
                    special_objs_tbl_6FF2.getU(address + 3),
                    special_objs_tbl_6FF2.getU(address + 4));
            SpecialObjectLocation current = new SpecialObjectLocation(
                    special_objs_tbl_6FF2.getU(address + 5),
                    special_objs_tbl_6FF2.getU(address + 6),
                    special_objs_tbl_6FF2.getU(address + 7),
                    special_objs_tbl_6FF2.getU(address + 8));
            objects[i] = new SpecialObject(grNo, start, current);
        }
    }

    public int getAddress(int index) { return special_objs_tbl_6FF2.start + index * 9; }
    public int getIndex(int address) { return (address - special_objs_tbl_6FF2.start) / 9; }

    //private int getInternal(int index, int offset) { return special_objs_tbl_6FF2.getU(getAddress(index) + offset); }
    //private void setInternal(int index, int offset, int value) { special_objs_tbl_6FF2.set(getAddress(index) + offset, value); }

    public int getGraphicNo(int index) { return objects[index].graphicNo; }
    public int getStartX(int index) { return objects[index].start.x; }
    public int getStartY(int index) { return objects[index].start.y; }
    public int getStartZ(int index) { return objects[index].start.z; }
    public int getStartScreen(int index) { return objects[index].start.screen; }
    public int getCurrentX(int index) { return objects[index].current.x; }
    public int getCurrentY(int index) { return objects[index].current.y; }
    public int getCurrentZ(int index) { return objects[index].current.z; }
    public int getScreen(int index) { return objects[index].current.screen; }

    public void clearGraphicNo(int index) { setGraphicNo(index, 0); }
    public void setGraphicNo(int index, int value) { objects[index].graphicNo = value; }
    public void setStartX(int index, int value) { objects[index].start.x = value; }
    public void setStartY(int index, int value) { objects[index].start.y = value; }
    public void setStartZ(int index, int value) { objects[index].start.z = value; }
    public void setStartScreen(int index, int value) { objects[index].start.screen = value; }
    public void setCurrentX(int index, int value) { objects[index].current.x = value; }
    public void setCurrentY(int index, int value) { objects[index].current.y = value; }
    public void setCurrentZ(int index, int value) { objects[index].current.z = value; }
    public void setScreen(int index, int value) { objects[index].current.screen = value; }

    public void copyStartToCurrent(int index) {
        setCurrentX(index, getStartX(index));
        setCurrentY(index, getStartY(index));
        setCurrentZ(index, getStartZ(index));
        setScreen(index, getStartScreen(index));
    }
}

class SpecialObjectLocation {
    int x, y, z, screen;
    SpecialObjectLocation(int x, int y, int z, int screen) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.screen = screen;
    }
}

class SpecialObject {
    int graphicNo;
    SpecialObjectLocation start;
    SpecialObjectLocation current;

    SpecialObject(int graphicNo, SpecialObjectLocation start, SpecialObjectLocation current) {
        this.graphicNo = graphicNo;
        this.start = start;
        this.current = current;
    }
}
