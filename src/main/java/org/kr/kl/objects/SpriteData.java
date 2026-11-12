package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

import java.util.Arrays;
import java.util.Optional;

public class SpriteData {
    private int size;
    private final int[] pointers;
    public final DataBlock sprite_graphics_data_728A = InitialData.block("sprite_graphics_data_728A");
    private final LookupTable lookupTable = new LookupTable();

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

        //int de = getPointer(grNo);
        Sprite sprite = sprites[grNo];
        int de = 0;

        boolean flipVScratch = metadata.isVFlip(ix);
        boolean flipHScratch = metadata.isHFlip(ix);
        int flagsSpriteData = sprite.data[de] & 0xC0;
        boolean flipVData = (flagsSpriteData & 0x80) > 0;
        boolean flipHData = (flagsSpriteData & 0x40) > 0;

        // Flip vertically (in-place) and set flag in data
        if(flipVScratch != flipVData) {
            for(int line = 0; line<(sprite.lines >> 1); line++) {
                for(int b = 0; b < sprite.bytesInLine; b++) {
                    int topByteAddress = de + line * sprite.bytesInLine + b;
                    int bottomByteAddress = de + (sprite.lines - line - 1) * sprite.bytesInLine + b;
                    sprite.replaceBytes(topByteAddress, bottomByteAddress);
                }
            }
            sprite.data[de] = sprite.data[de] ^ 0x80;
        }
        // Flip horizontally (in-place) and set flag in data
        if(flipHScratch != flipHData) {
            for(int line = 0; line<sprite.lines; line++) {
                int firstByteAddress = de + 2 + line * sprite.bytesInLine;
                int lastByteAddress = firstByteAddress + sprite.bytesInLine - 1;
                int bytePairs = sprite.bytesInLine >> 1;
                for(int b = 0; b < bytePairs; b++) {
                    boolean isMask = (b & 1) == 0;
                    // Middle pair means that we have off number of data bytes so we must only flip bits and leave bytes in place
                    boolean isMiddlePair = ((bytePairs & 1) == 1) && (b == bytePairs - 1);
                    int shift = isMiddlePair ? 0 : (isMask ? -1 : 1);
                    // NOTE: the data bytes and mask bytes must not be mixed
                    int leftByteAddress = firstByteAddress + b;
                    int rightByteAddress = lastByteAddress - b + shift;
                    int leftBuffer = sprite.getByteRaw(leftByteAddress - 2);
                    leftBuffer = lookupTable.get(0xF100 + leftBuffer); // F1xx - byte flip table
                    int rightBuffer = sprite.getByteRaw(rightByteAddress - 2);
                    rightBuffer = lookupTable.get(0xF100 + rightBuffer);
                    sprite.setByteRaw(leftByteAddress - 2, isMiddlePair ? leftBuffer : rightBuffer);
                    sprite.setByteRaw(rightByteAddress - 2, isMiddlePair ? rightBuffer : leftBuffer);
                }
            }
            sprite.data[de] = sprite.data[de] ^ 0x40;
        }
        return Optional.of(sprite);
    }
}
