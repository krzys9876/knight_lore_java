package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class ForegroundObject {
    public final ForegroundSprite[] sprites;
    public DataBlock block;

    public ForegroundObject(String resource) {
        block = InitialData.block(resource);
        // each object consists of 6-byte sprite(s) info terminated by 0
        sprites = new ForegroundSprite[(block.size - 1) / 6];
        for(int i=0; i<sprites.length; i++) {
            int base = block.start + i*6;
            sprites[i] = new ForegroundSprite( block.getU(base), block.getU(base+1),
                    block.getU(base+2), block.getU(base+3), block.getU(base+4),
                    block.getU(base+5));
        }
    }
}
