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
            int endAddressExcl = grNo==size-1 ? sprite_graphics_data_728A.endExcl() : pointers[grNo]+1;
            sprites[grNo] = new Sprite(Arrays.copyOfRange(sprite_graphics_data_728A.getCopy(), startAddress - sprite_graphics_data_728A.start, endAddressExcl - sprite_graphics_data_728A.start));
        }
    }

    public int getPointer(int grNo) {
        //int hl = grNo * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
        //return sprite_tbl_7112.getU(hl) + sprite_tbl_7112.getU(hl+1)*256; // sprite actual address
        return pointers[grNo];
    }

    // Returns address of sprite graphics data or 0 if null sprite
    public Optional<Sprite> flip_sprite_D6EF(GraphicObjects metadata, int ix) {
        int grNo = metadata.getGrNo(ix);
        if(grNo == 0) return Optional.empty();

        int de = getPointer(grNo);

        boolean flipVScratch = metadata.isVFlip(ix);
        boolean flipHScratch = metadata.isHFlip(ix);
        int flagsSpriteData = sprite_graphics_data_728A.getU(de) & 0xC0;
        boolean flipVData = (flagsSpriteData & 0x80) > 0;
        boolean flipHData = (flagsSpriteData & 0x40) > 0;

        // Flip vertically (in-place) and set flag in data
        if(flipVScratch != flipVData) {
            int lines = sprite_graphics_data_728A.getU(de+1);
            int bytesInLine = (sprite_graphics_data_728A.getU(de) & 0x07) * 2;
            for(int line = 0; line<(lines >> 1); line++) {
                for(int b = 0; b < bytesInLine; b++) {
                    int topByteAddress = de + 2 + line * bytesInLine + b;
                    int bottomByteAddress = de + 2 + (lines - line - 1) * bytesInLine + b;
                    int buffer = sprite_graphics_data_728A.getU(topByteAddress);
                    sprite_graphics_data_728A.set(topByteAddress, sprite_graphics_data_728A.getU(bottomByteAddress));
                    sprite_graphics_data_728A.set(bottomByteAddress, buffer);
                }
            }
            sprite_graphics_data_728A.set(de, sprite_graphics_data_728A.getU(de) ^ 0x80);
        }
        // Flip horizontally (in-place) and set flag in data
        if(flipHScratch != flipHData) {
            int lines = sprite_graphics_data_728A.getU(de+1);
            int bytesInLine = (sprite_graphics_data_728A.getU(de) & 0x07) * 2;
            for(int line = 0; line<lines; line++) {
                int firstByteAddress = de + 2 + line * bytesInLine;
                int lastByteAddress = firstByteAddress + bytesInLine - 1;
                int bytePairs = bytesInLine >> 1;
                for(int b = 0; b < bytePairs; b++) {
                    boolean isMask = (b & 1) == 0;
                    // Middle pair means that we have off number of data bytes so we must only flip bits and leave bytes in place
                    boolean isMiddlePair = ((bytePairs & 1) == 1) && (b == bytePairs - 1);
                    int shift = isMiddlePair ? 0 : (isMask ? -1 : 1);
                    // NOTE: the data bytes and mask bytes must not be mixed
                    int leftByteAddress = firstByteAddress + b;
                    int rightByteAddress = lastByteAddress - b + shift;
                    int leftBuffer = sprite_graphics_data_728A.getU(leftByteAddress);
                    leftBuffer = lookupTable.get(0xF100 + leftBuffer); // F1xx - byte flip table
                    int rightBuffer = sprite_graphics_data_728A.getU(rightByteAddress);
                    rightBuffer = lookupTable.get(0xF100 + rightBuffer);
                    sprite_graphics_data_728A.set(leftByteAddress, isMiddlePair ? leftBuffer : rightBuffer);
                    sprite_graphics_data_728A.set(rightByteAddress, isMiddlePair ? rightBuffer : leftBuffer);
                }
            }
            sprite_graphics_data_728A.set(de, sprite_graphics_data_728A.getU(de) ^ 0x40);
        }
        int startAddress = de;
        int lines = sprite_graphics_data_728A.getU(de+1);
        int bytesInLine = (sprite_graphics_data_728A.getU(de) & 0x07) * 2;
        int endAddress = de + 2 + lines * bytesInLine;
        assert(endAddress > startAddress);
        return Optional.of(new Sprite(Arrays.copyOfRange(sprite_graphics_data_728A.getCopy(), startAddress - sprite_graphics_data_728A.start, endAddress - sprite_graphics_data_728A.start)));
    }
}
