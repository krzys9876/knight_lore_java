package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class SpriteData {
    private int size;
    private final int[] pointers;
    public final DataBlock sprite_graphics_data_728A = InitialData.block("sprite_graphics_data_728A");

    public SpriteData() {
        DataBlock sprite_tbl_7112 = InitialData.block("sprite_tbl_7112");
        size = sprite_tbl_7112.size / 2; // 2 bytes per object
        pointers = new int[size];
        for(int grNo=0; grNo<size; grNo++) {
            int hl = grNo * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
            pointers[grNo] = sprite_tbl_7112.getU(hl) + sprite_tbl_7112.getU(hl+1)*256; // sprite actual address
        }
    }

    public int getPointer(int grNo) {
        //int hl = grNo * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
        //return sprite_tbl_7112.getU(hl) + sprite_tbl_7112.getU(hl+1)*256; // sprite actual address
        return pointers[grNo];
    }
}
