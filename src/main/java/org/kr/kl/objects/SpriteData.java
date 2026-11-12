package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class SpriteData {
    private final DataBlock sprite_tbl_7112 = InitialData.block("sprite_tbl_7112");
    public final DataBlock sprite_graphics_data_728A = InitialData.block("sprite_graphics_data_728A");

    public int getPointer(int grNo) {
        int hl = grNo * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
        return sprite_tbl_7112.getU(hl) + sprite_tbl_7112.getU(hl+1)*256; // sprite actual address
    }
}
