package org.clinlx.moreloreeffectsplugins.skilsys;

import java.util.HashMap;

public class CoolDownSys {

    public CoolDownSys() {
        skillTypeCoolDownInfo = new HashMap<>();
    }

    private final HashMap<String, CoolDownInfo> skillTypeCoolDownInfo;

    //TODO: 数据持久化
    public CoolDownInfo getTypeCoolDownInfo(String skillType) {
        synchronized (skillTypeCoolDownInfo) {
            if (!skillTypeCoolDownInfo.containsKey(skillType)) {
                skillTypeCoolDownInfo.put(skillType, new CoolDownInfo());
            }
            return skillTypeCoolDownInfo.get(skillType);
        }
    }
}
