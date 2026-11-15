package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class BackgroundObject {
    private final BackgroundSprite[] sprites;

    public BackgroundObject(String resource) {
        DataBlock block = InitialData.block(resource);
        // each object consists of 8-byte sprite info terminated by 0
        sprites = new BackgroundSprite[(block.size - 1) / 8];
        for(int i=0; i<sprites.length; i++) {
            int base = block.start + i*8;
            sprites[i] = new BackgroundSprite( block.getU(base), block.getU(base+1),
                    block.getU(base+2), block.getU(base+3), block.getU(base+4),
                    block.getU(base+5), block.getU(base+6), block.getU(base+7));
        }
    }

    public int size() { return sprites.length; }

    public BackgroundSprite s(int index) { return sprites[index]; }
}
