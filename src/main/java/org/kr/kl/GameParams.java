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

    // migrated from game variables
    private int lives;
    private int objectsPutInCauldron;



    private long tickLimit = -1;

    private boolean isGameActive = false;

    private Runnable paramsCallback = () -> {};

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
        this.objectsPutInCauldron = 0;
        this.cauldronRequiredObjects = cauldronRequiredObjects;

        this.delayMs = delayMs;
        this.delayMsReplay = delayMsReplay;
    }

    public void setParamsCallback(Runnable r) { paramsCallback = r; }

    public SeedGenerator getRnd() { return rnd; }

    public boolean isSkipMenu() { return skipMenu; }
    public void enableSkipMenu() { skipMenu = true; paramsCallback.run(); }
    public void disableSkipMenu() { skipMenu = false; paramsCallback.run(); }

    public boolean isDisableDeath() { return disableDeath; }
    public void enableDeath() { disableDeath = false; paramsCallback.run(); }
    public void disableDeath() { disableDeath = true; paramsCallback.run(); }

    public boolean isDisableLoseLife() { return disableLoseLife; }
    public void enableLoseLife() { disableLoseLife = false; paramsCallback.run(); }
    public void disableLoseLife() { disableLoseLife = true; paramsCallback.run(); }

    public boolean isDisableIncDays() { return disableIncDays; }
    public void enableIncDays() { disableIncDays = false; paramsCallback.run(); }
    public void disableIncDays() { disableIncDays = true; paramsCallback.run();}

    public int getMaxDays() { return maxDays; }
    public void setMaxDays(int days) { if(days >=0 && days <= 99) maxDays = days; }
    public int getInitialLives() { return initialLives; }
    public void setInitialLives(int iLives) {
        if(iLives >=0 && iLives <= 99) {
            initialLives = iLives;
            if(!isGameActive) lives = initialLives;
            paramsCallback.run();
        }
    }
    public int getCauldronRequiredObjects() { return cauldronRequiredObjects; }
    public void setCauldronRequiredObjects(int obj) {
        if(obj >=objectsPutInCauldron && obj <=14) {
            cauldronRequiredObjects = obj;
            paramsCallback.run();
        }
    }
    public int getObjectsPutInCauldron() { return objectsPutInCauldron; }
    public void setObjectsPutInCauldron(int value) {
        if(value>=0 && value <=14) {
            objectsPutInCauldron = value;
            paramsCallback.run();
        }
    }

    public int getLives() { return lives; }
    public void setLivesInit() {
        if(!isGameActive) {
            this.lives = getInitialLives()+1;
            paramsCallback.run();
        }
    }
    public void setLives(int lives) {
        if(lives>=0 && lives<=99 && isGameActive) {
            this.lives = lives;
            paramsCallback.run();
        }
    }

    public long getDelayMs() { return delayMs; }
    public void setDelayMs(long ms) {
        if(ms>=0L && ms<=250L) {
            this.delayMs = ms;
            paramsCallback.run();
        }
    }

    public long getDelayMsReplay() { return delayMsReplay; }

    public long getTickLimit() { return tickLimit; }
    public void setTickLimit(long tickLinit) { this.tickLimit = tickLinit; }

    public void freezeSeed() { rnd.freeze(); }
    public void unfreezeSeed() { rnd.unfreeze(); }

    public boolean isGameActive() { return isGameActive; }

    public void setActive() { isGameActive = true; paramsCallback.run(); }
    public void setInactive() { isGameActive = false; paramsCallback.run(); }
}
