package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

import java.util.Arrays;
import java.util.Optional;

public class SpriteData {
    public final DataBlock sprite_graphics_data_728A = InitialData.block("sprite_graphics_data_728A");
    private final Sprite[] sprites;

    public SpriteData() {
        DataBlock sprite_tbl_7112 = InitialData.block("sprite_tbl_7112"); // pointers to sprites in sprite graphic data table
        int size = sprite_tbl_7112.size / 2; // 2 bytes per object
        sprites = new Sprite[size];
        int[] spriteDataRaw = sprite_graphics_data_728A.getCopy();
        for(int grNo = 0; grNo< size; grNo++) {
            int hl = grNo * 2 + sprite_tbl_7112.start; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
            int startAddress = sprite_tbl_7112.getU(hl) + sprite_tbl_7112.getU(hl+1)*256 - sprite_graphics_data_728A.start; // sprite actual address
            int lines = spriteDataRaw[startAddress+1];
            int bytesInLine = (spriteDataRaw[startAddress] & 0x07) * 2;
            int endAddressExcl = startAddress + 2 + lines * bytesInLine;
            sprites[grNo] = new Sprite(Arrays.copyOfRange(spriteDataRaw, startAddress, endAddressExcl));
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
