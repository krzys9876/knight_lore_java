package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

import java.util.Arrays;
import java.util.Optional;

public class SpriteData {
    private int size;
    private final int[] pointers;
    public final DataBlock sprite_graphics_data_728A = InitialData.block("sprite_graphics_data_728A");

    private final Sprite[] sprites;

    public SpriteData() {
        DataBlock sprite_tbl_7112 = InitialData.block("sprite_tbl_7112");
        size = sprite_tbl_7112.size / 2; // 2 bytes per object
        pointers = new int[size];
        sprites = new Sprite[size];
        for(int grNo=0; grNo<size; grNo++) {
            int hl = grNo * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
            pointers[grNo] = sprite_tbl_7112.getU(hl) + sprite_tbl_7112.getU(hl+1)*256; // sprite actual address
        }
        for(int grNo=0; grNo<size; grNo++) {
            int startAddress = pointers[grNo];
            int lines = sprite_graphics_data_728A.getU(startAddress+1);
            int bytesInLine = (sprite_graphics_data_728A.getU(startAddress) & 0x07) * 2;
            int endAddressExcl = startAddress + 2 + lines * bytesInLine;
            sprites[grNo] = new Sprite(Arrays.copyOfRange(sprite_graphics_data_728A.getCopy(), startAddress - sprite_graphics_data_728A.start, endAddressExcl - sprite_graphics_data_728A.start));
        }
    }

    // Returns address of sprite graphics data or 0 if null sprite
    public Optional<Sprite> flip_sprite_D6EF(GraphicObjects metadata, int ix) {
        int grNo = metadata.getGrNo(ix);
        if(grNo == 0) return Optional.empty();

        Sprite sprite = sprites[grNo];
        sprite.flip(metadata.isHFlip(ix), metadata.isVFlip(ix));
        return Optional.of(sprite);
    }
}
