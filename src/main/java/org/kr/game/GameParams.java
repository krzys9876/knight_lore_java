package org.kr.game;

import org.kr.game.utils.SeedGenerator;

public class GameParams {
    private final SeedGenerator rnd;

    private final boolean skipMenu;
    private final boolean disableDeath;
    private final boolean disableLoseLife;
    private final boolean disableIncDays;

    private final int maxDays; // 98 is max (wraps at 99)
    private final int initialLives;
    private int cauldronRequiredObjects; // 0x0E

    private final long delayMs; // 90ms
    private final long delayMsReplay;


    public GameParams(SeedGenerator rnd, boolean skipMenu, boolean disableDeath, boolean disableLoseLife, boolean disableIncDays,
                      int maxDays, int initialLives, int cauldronRequiredObjects, long delayMs, int delayMsReplay) {
        this.rnd = rnd;
        this.skipMenu = skipMenu;
        this.disableDeath = disableDeath;
        this.disableLoseLife = disableLoseLife;
        this.disableIncDays = disableIncDays;

        this.maxDays = maxDays;
        this.initialLives = initialLives;
        this.cauldronRequiredObjects = cauldronRequiredObjects;

        this.delayMs = delayMs;
        this.delayMsReplay = delayMsReplay;
    }

    public SeedGenerator getRnd() { return rnd; }

    public boolean isSkipMenu() { return skipMenu; }
    public boolean isDisableDeath() { return disableDeath; }
    public boolean isDisableLoseLife() { return disableLoseLife; }
    public boolean isDisableIncDays() { return disableIncDays; }

    public int getMaxDays() { return maxDays; }
    public int getInitialLives() { return initialLives; }
    public int getCauldronRequiredObjects() { return cauldronRequiredObjects; }

    public long getDelayMs() { return delayMs; }
    public long getDelayMsReplay() { return delayMsReplay; }
}
