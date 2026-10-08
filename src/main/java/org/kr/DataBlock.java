package org.kr;

import java.util.Arrays;

public class DataBlock {
    public final int start;
    public final int size;
    private final int[] data;

    public DataBlock(int start, int size) {
        this.start = start;
        this.size = size;
        data = new int[size];
    }

    public DataBlock(int start, int[] data) {
        this.start = start;
        this.size = data.length;
        this.data = Arrays.copyOf(data, data.length);
    }

    public DataBlock copy() {
        return new DataBlock(start, Arrays.copyOf(data, data.length));
    }

    public int last() { return start + size - 1; }
    public int endExcl() { return start + size; }
    public void set(int address, int value) {
        if(value < -256 || value > 255)
            throw new IllegalArgumentException("value must be between 0 and 255 ("+value+")");
        data[address - start]=value;
    }
    // signed byte value
    public int getS(int address) {
        int value = data[address - start];
        if(value >= 128) value = 127 - value;
        return value;
    }
    // unsigned byte value
    public int getU(int address) {
        int value = data[address - start];
        if(value < 0) value = 127 - value;
        return value;
    }
    public int[] getCopy() { return Arrays.copyOf(data, data.length); }

    public void reset() {
        for (int i = start; i < start+size-1; i++) { set(i, 0);}
    }

    public boolean isSet(int bit, int address) { return isSetS(bit, getU(address)); }
    public void setBit(int bit, int address) { set(address, getWithBitSet(bit, address)); }
    public void resetBit(int bit, int address) { set(address, getWithBitReset(bit, address)); }
    public int getWithBitSet(int bit, int address) { return getU(address) | (1 << bit);}
    public int getWithBitReset(int bit, int address) { return getU(address) & ((1 << bit) ^ 0b11111111);}

    public static boolean isSetS(int bit, int value) { return (value & (1 << bit))>0; }

    public String objectInfo(int address) {
        // Assuming the complete object info starts at address
        String txt = String.format("%02x %s xyz:(%d,%d,%d) d:(%d,%d,%d)", getU(address), GRAPHIC_NAMES[getU(address)],
                getU(address+1), getU(address+2), getU(address+3),
                getS(address+0x09), getS(address+0x0A), getS(address+0x0B));
        return txt;
    }

    private static final String[] GRAPHIC_NAMES = {
            "none",                                             // 0x00 (0)
            "unused",                                           // 0x01 (1)
            "stone arch (near side)",                           // 0x02 (2)
            "stone arch (far side)",                            // 0x03 (3)
            "tree arch (near side)",                            // 0x04 (4)
            "tree arch (far side)",                             // 0x05 (5)
            "rock",                                             // 0x06 (6)
            "block",                                            // 0x07 (7)
            "portcullis (stationary)",                          // 0x08 (8)
            "portcullis (moving)",                              // 0x09 (9)
            "bricks",                                           // 0x0A (10)
            "more bricks",                                      // 0x0B (11)
            "even more bricks",                                 // 0x0C (12)
            "even more bricks",                                 // 0x0D (13)
            "even more bricks",                                 // 0x0E (14)
            "even more bricks",                                 // 0x0F (15)
            "player legs (human)",                              // 0x10 (16)
            "player legs (human)",                              // 0x11 (17)
            "player legs (human)",                              // 0x12 (18)
            "player legs (human)",                              // 0x13 (19)
            "player legs (human)",                              // 0x14 (20)
            "player legs (human)",                              // 0x15 (21)
            "gargoyle",                                         // 0x16 (22)
            "spikes",                                           // 0x17 (23)
            "player legs (human)",                              // 0x18 (24)
            "player legs (human)",                              // 0x19 (25)
            "player legs (human)",                              // 0x1A (26)
            "player legs (human)",                              // 0x1B (27)
            "player legs (human)",                              // 0x1C (28)
            "player legs (human)",                              // 0x1D (29)
            "guard (moving NSEW) (top half)",                   // 0x1E (30)
            "guard (moving NSEW) (top half)",                   // 0x1F (31)
            "player top (human)",                               // 0x20 (32)
            "player top (human)",                               // 0x21 (33)
            "player top (human)",                               // 0x22 (34)
            "player top (human)",                               // 0x23 (35)
            "player top (human)",                               // 0x24 (36)
            "player top (human)",                               // 0x25 (37)
            "player top (human)",                               // 0x26 (38)
            "player top (human)",                               // 0x27 (39)
            "player top (human)",                               // 0x28 (40)
            "player top (human)",                               // 0x29 (41)
            "player top (human)",                               // 0x2A (42)
            "player top (human)",                               // 0x2B (43)
            "player top (human)",                               // 0x2C (44)
            "player top (human)",                               // 0x2D (45)
            "player top (human)",                               // 0x2E (46)
            "player top (human)",                               // 0x2F (47)
            "player legs (wulf)",                               // 0x30 (48)
            "player legs (wulf)",                               // 0x31 (49)
            "player legs (wulf)",                               // 0x32 (50)
            "player legs (wulf)",                               // 0x33 (51)
            "player legs (wulf)",                               // 0x34 (52)
            "player legs (wulf)",                               // 0x35 (53)
            "block (moving EW)",                                // 0x36 (54)
            "block (moving NS)",                                // 0x37 (55)
            "player legs (wulf)",                               // 0x38 (56)
            "player legs (wulf)",                               // 0x39 (57)
            "player legs (wulf)",                               // 0x3A (58)
            "player legs (wulf)",                               // 0x3B (59)
            "player legs (wulf)",                               // 0x3C (60)
            "player legs (wulf)",                               // 0x3D (61)
            "another block",                                    // 0x3E (62)
            "spiked ball",                                      // 0x3F (63)
            "player top (wulf)",                                // 0x40 (64)
            "player top (wulf)",                                // 0x41 (65)
            "player top (wulf)",                                // 0x42 (66)
            "player top (wulf)",                                // 0x43 (67)
            "player top (wulf)",                                // 0x44 (68)
            "player top (wulf)",                                // 0x45 (69)
            "player top (wulf)",                                // 0x46 (70)
            "player top (wulf)",                                // 0x47 (71)
            "player top (wulf)",                                // 0x48 (72)
            "player top (wulf)",                                // 0x49 (73)
            "player top (wulf)",                                // 0x4A (74)
            "player top (wulf)",                                // 0x4B (75)
            "player top (wulf)",                                // 0x4C (76)
            "player top (wulf)",                                // 0x4D (77)
            "player top (wulf)",                                // 0x4E (78)
            "player top (wulf)",                                // 0x4F (79)
            "ghost",                                            // 0x50 (80)
            "ghost",                                            // 0x51 (81)
            "ghost",                                            // 0x52 (82)
            "ghost",                                            // 0x53 (83)
            "table",                                            // 0x54 (84)
            "chest",                                            // 0x55 (85)
            "fire (EW)",                                        // 0x56 (86)
            "fire (EW)",                                        // 0x57 (87)
            "sun",                                              // 0x58 (88)
            "moon",                                             // 0x59 (89)
            "frame (left)",                                     // 0x5A (90)
            "block (dropping)",                                 // 0x5B (91)
            "human/wulf transform",                             // 0x5C (92)
            "human/wulf transform",                             // 0x5D (93)
            "human/wulf transform",                             // 0x5E (94)
            "human/wulf transform",                             // 0x5F (95)
            "diamond",                                          // 0x60 (96)
            "poison",                                           // 0x61 (97)
            "boot",                                             // 0x62 (98)
            "chalice",                                          // 0x63 (99)
            "cup",                                              // 0x64 (100)
            "bottle",                                           // 0x65 (101)
            "crystal ball",                                     // 0x66 (102)
            "extra life",                                       // 0x67 (103)
            "into cauldron: diamond",                           // 0x68 (104)
            "into cauldron: poison",                            // 0x69 (105)
            "into cauldron: boot",                              // 0x6A (106)
            "into cauldron: chalice",                           // 0x6B (107)
            "into cauldron: cup",                               // 0x6C (108)
            "into cauldron: bottle",                            // 0x6D (109)
            "into cauldron: crystal ball",                      // 0x6E (110)
            "final sparkles in cauldron",                       // 0x6F (111)
            "death sparkles",                                   // 0x70 (112)
            "death sparkles",                                   // 0x71 (113)
            "death sparkles",                                   // 0x72 (114)
            "death sparkles",                                   // 0x73 (115)
            "death sparkles",                                   // 0x74 (116)
            "death sparkles",                                   // 0x75 (117)
            "death sparkles",                                   // 0x76 (118)
            "last death sparkle",                               // 0x77 (119)
            "player appears sparkles",                          // 0x78 (120)
            "player appears sparkles",                          // 0x79 (121)
            "player appears sparkles",                          // 0x7A (122)
            "player appears sparkles",                          // 0x7B (123)
            "player appears sparkles",                          // 0x7C (124)
            "player appears sparkles",                          // 0x7D (125)
            "player appears sparkles",                          // 0x7E (126)
            "last player appears sparkle",                      // 0x7F (127)
            "tree wall",                                        // 0x80 (128)
            "tree wall",                                        // 0x81 (129)
            "tree wall",                                        // 0x82 (130)
            "sparkles in the cauldron room at end of game",     // 0x83 (131)
            "sparkles in the cauldron room at end of game",     // 0x84 (132)
            "sparkles in the cauldron room at end of game",     // 0x85 (133)
            "unused",                                           // 0x86 (134)
            "unused",                                           // 0x87 (135)
            "unused",                                           // 0x88 (136)
            "unused",                                           // 0x89 (137)
            "unused",                                           // 0x8A (138)
            "unused",                                           // 0x8B (139)
            "unused",                                           // 0x8C (140)
            "cauldron (bottom)",                                // 0x8D (141)
            "cauldron (top)",                                   // 0x8E (142)
            "block (collapsing)",                               // 0x8F (143)
            "guard & wizard (bottom half)",                     // 0x90 (144)
            "guard & wizard (bottom half)",                     // 0x91 (145)
            "guard & wizard (bottom half)",                     // 0x92 (146)
            "guard & wizard (bottom half)",                     // 0x93 (147)
            "guard & wizard (bottom half)",                     // 0x94 (148)
            "guard & wizard (bottom half)",                     // 0x95 (149)
            "guard (EW) (top half)",                            // 0x96 (150)
            "guard (EW) (top half)",                            // 0x97 (151)
            "guard & wizard (bottom half)",                     // 0x98 (152)
            "guard & wizard (bottom half)",                     // 0x99 (153)
            "guard & wizard (bottom half)",                     // 0x9A (154)
            "guard & wizard (bottom half)",                     // 0x9B (155)
            "guard & wizard (bottom half)",                     // 0x9C (156)
            "guard & wizard (bottom half)",                     // 0x9D (157)
            "wizard (top half)",                                // 0x9E (158)
            "wizard (top half)",                                // 0x9F (159)
            "cauldron bubbles",                                 // 0xA0 (160)
            "cauldron bubbles",                                 // 0xA1 (161)
            "cauldron bubbles",                                 // 0xA2 (162)
            "cauldron bubbles",                                 // 0xA3 (163)
            "repel spell",                                      // 0xA4 (164)
            "repel spell",                                      // 0xA5 (165)
            "repel spell",                                      // 0xA6 (166)
            "repel spell",                                      // 0xA7 (167)
            "in cauldron (1st): diamond",                       // 0xA8 (168)
            "in cauldron (1st): poison",                        // 0xA9 (169)
            "in cauldron (1st): boot",                          // 0xAA (170)
            "in cauldron (1st): chalice",                       // 0xAB (171)
            "in cauldron (1st): cup",                           // 0xAC (172)
            "in cauldron (1st): bottle",                        // 0xAD (173)
            "in cauldron (1st): crystal ball",                  // 0xAE (174)
            "in cauldron (1st): extra life",                    // 0xAF (175)
            "fire (stationary) (not used)",                     // 0xB0 (176)
            "fire (stationary) (not used)",                     // 0xB1 (177)
            "ball up/down",                                     // 0xB2 (178)
            "ball up/down",                                     // 0xB3 (179)
            "fire (NS)",                                        // 0xB4 (180)
            "fire (NS)",                                        // 0xB5 (181)
            "ball (bouncing around)",                           // 0xB6 (182)
            "ball (bouncing around)",                           // 0xB7 (183)
            "death sparkles",                                   // 0xB8 (184)
            "last obj in cauldron sparkle",                     // 0xB9 (185)
            "unused",                                           // 0xBA (186)
            "last obj in cauldron sparkle",                     // 0xBB (187)
    };
}
