package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class Locations {
    public final DataBlock data = InitialData.block("location_tbl_6251");
    public final DataBlock room_size_tbl_6248 = InitialData.block("room_size_tbl_6248");

    public final DataBlock[] locations;

    public Locations() {
        int hl = data.start;
        int roomCnt = 0;
        while(hl < data.endExcl()-1) {
            roomCnt++;
            int sizeBytes = data.getU(hl+1);
            hl += 1 + sizeBytes;
        }
        locations = new DataBlock[roomCnt];
    }
}
