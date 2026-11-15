package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class Locations {
    public final DataBlock data = InitialData.block("location_tbl_6251");

    public final Location[] locations;

    public Locations() {
        int hl = data.start;
        int roomCnt = 0;
        while(hl < data.endExcl()-1) {
            roomCnt++;
            int sizeBytes = data.getU(hl+1);
            hl += 1 + sizeBytes;
        }
        locations = new Location[roomCnt];
        hl = data.start;
        for(int i=0; i < roomCnt; i++) {
            int size = data.getU(hl + 1) + 1;
            locations[i] = new Location(data.slice(hl, size));
            hl+=size;
        }
    }
}
