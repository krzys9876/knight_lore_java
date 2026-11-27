package org.kr.kl;

import org.kr.kl.utils.SeedGenerator;

public class GameParams {
    private final SeedGenerator rnd;

    private boolean skipMenu;
    private boolean disableDeath;
    private boolean disableLoseLife;
    private boolean disableIncDays;

    private int maxDays; // 98 is max (wraps at 99)
    private int initialLives;
    private int cauldronRequiredObjects; // 0x0E

    private long delayMs; // 90ms
    private final long delayMsReplay;

    private int lives;

    private long tickLimit = -1;


    public GameParams(SeedGenerator rnd, boolean skipMenu, boolean disableDeath, boolean disableLoseLife, boolean disableIncDays,
                      int maxDays, int initialLives, int cauldronRequiredObjects, long delayMs, int delayMsReplay) {
        this.rnd = rnd;
        this.skipMenu = skipMenu;
        this.disableDeath = disableDeath;
        this.disableLoseLife = disableLoseLife;
        this.disableIncDays = disableIncDays;

        this.maxDays = maxDays;
        this.initialLives = initialLives;
        this.lives = initialLives;
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

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public long getDelayMs() { return delayMs; }
    public long getDelayMsReplay() { return delayMsReplay; }

    public long getTickLimit() { return tickLimit; }
    public void setTickLimit(long tickLinit) { this.tickLimit = tickLinit; }

    public void freezeSeed() { rnd.freeze(); }
    public void unfreezeSeed() { rnd.unfreeze(); }
}
