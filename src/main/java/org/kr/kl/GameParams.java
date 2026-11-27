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

    private boolean isGameActive = false;


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
    public void enableSkipMenu() { skipMenu = true; }
    public void disableSkipMenu() { skipMenu = false; }

    public boolean isDisableDeath() { return disableDeath; }
    public void enableDeath() { disableDeath = false; }
    public void disableDeath() { disableDeath = true; }

    public boolean isDisableLoseLife() { return disableLoseLife; }
    public void enableLoseLife() { disableLoseLife = false; }
    public void disableLoseLife() { disableLoseLife = true; }

    public boolean isDisableIncDays() { return disableIncDays; }
    public void enableIncDays() { disableIncDays = false; }
    public void disableIncDays() { disableIncDays = true; }

    public int getMaxDays() { return maxDays; }
    public void setMaxDays(int days) { if(days >=0 && days <= 99) maxDays = days; }
    public int getInitialLives() { return initialLives; }
    public void setInitialLives(int iLives) {
        if(iLives >=0 && iLives <= 99) {
            initialLives = iLives;
            if(!isGameActive) lives = initialLives;
        }
    }
    public int getCauldronRequiredObjects() { return cauldronRequiredObjects; }
    public void setCauldronRequiredObjects(int obj) {
        if(obj >=0 && obj <=14)
         cauldronRequiredObjects = obj;
    }

    public int getLives() { return lives; }
    public void setLivesInit() {
        if(!isGameActive) {
            this.lives = getInitialLives()+1;
        }
    }
    public void setLives(int lives) {
        if(lives>=0 && lives<=99 && isGameActive) {
            this.lives = lives;
        }
    }

    public long getDelayMs() { return delayMs; }
    public void setDelayMs(long ms) {
        if(ms>=0L && ms<=250L) {
            this.delayMs = ms;
        }
    }


    public long getDelayMsReplay() { return delayMsReplay; }

    public long getTickLimit() { return tickLimit; }
    public void setTickLimit(long tickLinit) { this.tickLimit = tickLinit; }

    public void freezeSeed() { rnd.freeze(); }
    public void unfreezeSeed() { rnd.unfreeze(); }

    public boolean isGameActive() { return isGameActive; }

    public void setActive() { isGameActive = true; }
    public void setInactive() { isGameActive = false; }
}
