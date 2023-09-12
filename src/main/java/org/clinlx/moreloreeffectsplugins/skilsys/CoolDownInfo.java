package org.clinlx.moreloreeffectsplugins.skilsys;

public class CoolDownInfo {
    public static final long runningSign = -1024;

    public CoolDownInfo() {
    }


    private long skillTypeLastUseTime = 0;

    public long getLastUseTime() {
        return skillTypeLastUseTime;
    }

    private long skillTypeCoolDownTime = 0;

    public long getCoolDownTime() {
        return skillTypeCoolDownTime;
    }


    public void setSkillTypeCoolDownTimeLen(long coolDownTime) {
        skillTypeCoolDownTime = coolDownTime;
    }

    public void useSkillTypeCoolDown(long coolDownTime) {
        skillTypeLastUseTime = System.currentTimeMillis();
        setSkillTypeCoolDownTimeLen(coolDownTime);
    }

    public boolean skillTypeReady() {
        if (skillTypeCoolDownTime < 0) return false;
        if (skillTypeCoolDownTime == 0) return true;
        if (skillTypeLastUseTime <= 0) return true;
        return System.currentTimeMillis() - skillTypeLastUseTime > skillTypeCoolDownTime;
    }

    public long getWaitTime() {
        if (skillTypeCoolDownTime <= 0) return skillTypeCoolDownTime;
        if (skillTypeLastUseTime <= 0) return 0;
        long waitTime = skillTypeCoolDownTime - (System.currentTimeMillis() - skillTypeLastUseTime);
        if (waitTime < 0) return 0;
        return waitTime;
    }

    public String getWaitTimeStr() {
        long waitTime = getWaitTime();
        if (waitTime == runningSign) return "§e直到技能结束§r";
        else if (waitTime < 0) return "§e无限久§r";
        double waitTimeSecond = waitTime * 0.001;
        return "§e" + String.format("%.2f", waitTimeSecond) + "秒§r";
    }
}
