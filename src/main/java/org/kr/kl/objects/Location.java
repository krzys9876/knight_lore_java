package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class Location {
    public static final DataBlock room_size_tbl_6248 = InitialData.block("room_size_tbl_6248");

    public final DataBlock data;

    public Location(DataBlock data) {
        this.data = data;
    }

    public int getLocId() { return data.getU(data.start); }
    public int sizeBytes() { return data.getU(data.start + 1); }
    private int getAttrOrig() { return data.getU(data.start + 2); }
    public int getAttr() { return (getAttrOrig() & 0x07) | 0x40; }
    public int getRoomSize() { return (getAttrOrig() >> 3) & 0x1F; }
    private int getRoomSizeBase() { return Location.room_size_tbl_6248.start + getRoomSize()*3; }
    public int getRoomSizeX() { return Location.room_size_tbl_6248.getU(getRoomSizeBase()); }
    public int getRoomSizeY() { return Location.room_size_tbl_6248.getU(getRoomSizeBase() + 1); }
    public int getRoomSizeZ() { return Location.room_size_tbl_6248.getU(getRoomSizeBase() + 2); }
}
