package org.kr.kl.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

import java.util.HashMap;
import java.util.Map;

public class Locations {
    public final DataBlock data = InitialData.block("location_tbl_6251");

    public final Location[] locations;

    private final Map<Integer, Location> locMap;

    public Locations() {
        int hl = data.start;
        int roomCnt = 0;
        while(hl < data.endExcl()-1) {
            roomCnt++;
            int sizeBytes = data.getU(hl+1);
            hl += 1 + sizeBytes;
        }
        locations = new Location[roomCnt];
        locMap = new HashMap<>();
        hl = data.start;
        for(int i=0; i < roomCnt; i++) {
            int size = data.getU(hl + 1) + 1;
            locations[i] = new Location(data.slice(hl, size));
            locMap.put(locations[i].id, locations[i]);
            hl+=size;
        }
    }

    public Location get(int id) { return locMap.get(id); }

    public int countVisited() {
        int visitedCnt = 0;
        for(Location l : locations) if(l.isVisited()) visitedCnt ++;
        return visitedCnt;
    }

    public void resetVisited() { for(Location l : locations) l.resetVisited(); }
}
