package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class Location {
    public static final DataBlock room_size_tbl_6248 = InitialData.block("room_size_tbl_6248");

    public final int id;
    public final int sizeBytes;
    private final int attrOrig;

    public final DataBlock data;

    public int[] backgroundObjectId;

    public Location(DataBlock data) {
        this.data = data;

        id = this.data.getU(this.data.start);
        sizeBytes = this.data.getU(this.data.start+1);
        attrOrig = this.data.getU(this.data.start+2);
        int bkObjStart = this.data.start + 3;
        int bkObjPos = bkObjStart;
        while(bkObjPos < data.endExcl() && data.getU(bkObjPos) != 0xFF) bkObjPos++;
        backgroundObjectId = new int[bkObjPos-bkObjStart];
        for(int i=0; i<backgroundObjectId.length; i++) backgroundObjectId[i] = data.getU(bkObjStart + i);
    }

    public int getAttr() { return (attrOrig & 0x07) | 0x40; }
    public int getRoomSize() { return (attrOrig >> 3) & 0x1F; }
    private int getRoomSizeBase() { return Location.room_size_tbl_6248.start + getRoomSize()*3; }
    public int getRoomSizeX() { return Location.room_size_tbl_6248.getU(getRoomSizeBase()); }
    public int getRoomSizeY() { return Location.room_size_tbl_6248.getU(getRoomSizeBase() + 1); }
    public int getRoomSizeZ() { return Location.room_size_tbl_6248.getU(getRoomSizeBase() + 2); }
}
