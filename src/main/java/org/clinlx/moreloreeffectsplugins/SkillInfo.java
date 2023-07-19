package org.clinlx.moreloreeffectsplugins;


public class SkillInfo {
    public SkillInfo(String skillEffect, long skillCoolDown) {
        this.skillEffect = skillEffect;
        this.skillCoolDown = skillCoolDown;
    }
    
    String skillEffect;
    long skillCoolDown;
}
