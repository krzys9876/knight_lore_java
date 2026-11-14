package org.kr.kl.objects;

import org.kr.DataBlock;

public class Variables {
    public DataBlock data = new DataBlock(0x5BA0, 0x5BE8 - 0x5BA0 + 1);

    public void reset() { data.reset(); }

    public void set_5BA4(int value) { data.set(0x5BA4, value); }
    public void set_5BB8(int value) { data.set(0x5BB8, value); }
    public void set_5BA6(int value) { data.set(0x5BA6, value); }
    public void set_5BA7(int value) { data.set(0x5BA7, value); }
    public void set_5BB1(int value) { data.set(0x5BB1, value); }
    public void set_5BC3(int value) { data.set(0x5BC3, value); }
    public void set_5BB9(int value) { data.set(0x5BB9, value); }
    public void set_5BB5(int value) { data.set(0x5BB5, value); }
    public void set_5BC4(int value) { data.set(0x5BC4, value); }
    public void set_5BC1(int value) { data.set(0x5BC1, value); }
    public void set_5BC2(int value) { data.set(0x5BC2, value); }
    public void set_5BBB(int value) { data.set(0x5BBB, value); }
    public void set_5BC5(int value) { data.set(0x5BC5, value); }
    public void set_5BC6(int value) { data.set(0x5BC6, value); }
    public void set_5BB3(int value) { data.set(0x5BB3, value); }
    public void set_5BB4(int value) { data.set(0x5BB4, value); }
    public void set_5BB2(int value) { data.set(0x5BB2, value); }
    public void set_5BBA(int value) { data.set(0x5BBA, value); }
    public void set_5BB6(int value) { data.set(0x5BB6, value); }
    public void set_5BB7(int value) { data.set(0x5BB7, value); }
    public void set_5BBE(int value) { data.set(0x5BBE, value); }
    public void set_5BB0(int value) { data.set(0x5BB0, value); }
    public void set_5BAF(int value) { data.set(0x5BAF, value); }
    public void set_5BC0(int value) { data.set(0x5BC0, value); }
    public void set_5BBF(int value) { data.set(0x5BBF, value); }
    public void set_5BBD(int value) { data.set(0x5BBD, value); }
    public void set_5BAD(int value) { data.set(0x5BAD, value); }
    public void set_5BAB(int value) { data.set(0x5BAB, value); }
    public void set_5BAC(int value) { data.set(0x5BAC, value); }
    public void set_5BAE(int value) { data.set(0x5BAE, value); }



    public void init() {
        reset();

        //; Data block at 5BC7
        // @label=gfxbase_8x8
        // b$5BC7 DEFB $08,$61
        //variables.set(0x5BC7, 0x08); // variables NOT USED (passing DataBlock with font data instead)
        //variables.set(0x5BC8, 0x61);
        // @label=user_input_method
        // b$5BA4 DEFS $01
        set_5BA4(0);
        //; Data block at 5BB8
        //@label=suppress_border
        //b$5BB8 DEFB $01
        set_5BB8(1);
        //@label=old_input_method
        //b$5BA6 DEFS $01
        //$5BA7 DEFS $01
        set_5BA6(0);
        set_5BA7(0);
        // @label=transform_flag_graphic
        // b$5BB1 DEFS $01
        set_5BB1(0);
        //@label=all_objs_in_cauldron
        //b$5BC3 DEFS $01
        set_5BC3(0);
        //@label=days
        //b$5BB9 DEFS $01
        set_5BB9(0);
        //; #TABLE(default,centre,:w)
        //; { =h Bit(n) | =h Description }
        //; { b5 | ??? }
        //; { b4 | pickup/drop }
        //; { b3 | jump }
        //; { b2 | forward }
        //; { b1 | right }
        //; { b0 | left }
        //; TABLE#
        //@label=user_input
        //b$5BB5 DEFS $01
        set_5BB5(0);
        //@label=obj_dropping_into_cauldron
        //b$5BC4 DEFS $01
        set_5BC4(0);

        //; Data block at 5BC1
        //@label=tmp_dZ
        //b$5BC1 DEFS $01
        set_5BC1(0);

        //; Data block at 5BC2
        //@label=tmp_bouncing_ball_dZ
        //b$5BC2 DEFS $01
        set_5BC2(0);

        //; Data block at 5BBB
        //@label=objects_put_in_cauldron
        //b$5BBB DEFS $01
        set_5BBB(0);

        //; Data block at 5BC5
        //@label=rising_blocks_z
        //b$5BC5 DEFS $01
        set_5BC5(0);

        //; Data block at 5BC6
        //@label=num_scrns_visited
        //b$5BC6 DEFS $01
        set_5BC6(0);

        //; Data block at 5BB3
        //@label=pickup_drop_pressed
        //b$5BB3 DEFS $01
        set_5BB3(0);

        //; Data block at 5BB4
        //@label=objects_carried_changed
        //b$5BB4 DEFS $01
        set_5BB4(0);
    }
}
