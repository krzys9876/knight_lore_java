package org.kr.kl.objects;

import org.kr.DataBlock;

public class Variables {
    public DataBlock data = new DataBlock(0x5BA0, 0x5BE8 - 0x5BA0 + 1);

    public void reset() { data.reset(); }

    public void set_5BA4(int value) { data.set(0x5BA4, value); }
    public void set_5BB8(int value) { data.set(0x5BA4, value); }
    public void set_5BA6(int value) { data.set(0x5BA4, value); }
    public void set_5BA7(int value) { data.set(0x5BA4, value); }
}
