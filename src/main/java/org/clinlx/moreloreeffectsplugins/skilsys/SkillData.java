package org.clinlx.moreloreeffectsplugins.skilsys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.scheduler.BukkitRunnable;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.luaj.SkillThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SkillData {
    MoreLoreEffectsPlugin plugin;
    private HashMap<String, SkillInfo> skillsRegister;
    private final HashMap<String, CoolDownSys> playerCoolDownSys;
    public final List<String> tpyeList;

    public SkillData(MoreLoreEffectsPlugin plugin) {
        this.plugin = plugin;
        playerCoolDownSys = new HashMap<>();
        tpyeList = new ArrayList<>();
        tpyeList.add("default");
        reLoadSkills();
        new BukkitRunnable() {
            @Override
            public void run() {
                printLog("当前有" + SkillThread.getThreadNum() + "个技能线程正在运行");
            }
        }.runTaskTimer(plugin, 20 * 30, 20 * 60 * 5);
    }

    public List<String> getSkillList() {
        return new ArrayList<String>(skillsRegister.keySet());
    }

    public boolean skillExist(String name) {
        return skillsRegister.containsKey(name);
    }

    public SkillInfo getSkill(String name) {
        return skillsRegister.get(name);
    }

    public CoolDownSys getCoolDownSys(String playerName) {
        if (!playerCoolDownSys.containsKey(playerName)) {
            playerCoolDownSys.put(playerName, new CoolDownSys());
        }
        return playerCoolDownSys.get(playerName);
    }

    public void clearAllCoolDownSys() {
        playerCoolDownSys.clear();
    }

    public void clearPlayerCoolDownSys(String playerName) {
        playerCoolDownSys.remove(playerName);
    }

    public void clearTypeCoolDownSys(String skillType) {
        for (String playerName : playerCoolDownSys.keySet()) {
            clearPlayerTypeCoolDownSys(playerName, skillType);
        }
    }

    public void clearPlayerTypeCoolDownSys(String playerName, String skillType) {
        getCoolDownSys(playerName).getTypeCoolDownInfo(skillType).setSkillTypeCoolDownTimeLen(0);
    }

    public void setSkill(String name, String skillType, long skillCoolDown, String skillEffect) {
        SkillCode newCode = null;
        if (skillExist(name)) {
            newCode = getSkill(name).skillCode;
        }
        tpyeList.add(skillType);
        skillsRegister.put(name, new SkillInfo(name, skillType, skillCoolDown, skillEffect, newCode));
    }

    public void setSkill(String name, String skillType, SkillCode skillCode) {
        String newEffect = null;
        if (skillExist(name)) {
            newEffect = getSkill(name).skillEffect;
        }
        tpyeList.add(skillType);
        skillsRegister.put(name, new SkillInfo(name, skillType, -1, newEffect, skillCode));
    }

    public void setSkill(String name, SkillInfo skillInfo) {
        if (skillInfo == null)
            return;
        tpyeList.add(skillInfo.skillType);
        skillsRegister.put(name, skillInfo);
    }

    public boolean delSkill(String name) {
        if (!skillExist(name)) return false;
        skillsRegister.remove(name);
        return true;
    }

    public void printLog(String message) {
        plugin.getLogger().info(message);
    }

    private static final String directoryPath = "./loreskills";

    public void reLoadSkills() {
        skillsRegister = new HashMap<>();
        //遍历loreskills下文件夹name
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files == null) {
            printLog("Lore技能列表文件夹 \"" + directoryPath + "\" 打开失败！");
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                String skillName = file.getName();
                if (!loadTheSkill(skillName)) {
                    printLog("Lore技能 \"" + skillName + "\" 加载失败！");
                    continue;
                }
            }
        }
    }

    public boolean loadTheSkill(String skillName) {
        SkillInfo newSkillInfo;
        //存在info.json
        String infoPath = directoryPath + "/" + skillName + "/info.json";
        File infoFile = new File(infoPath);
        if (!infoFile.exists()) return false;
        //读取info.json
        try {
            //读取info.json全部到String不成功返回false
            StringBuilder infoJsonBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(infoFile));
            String line;
            while ((line = reader.readLine()) != null) {
                infoJsonBuilder.append(line);
            }
            reader.close();
            //反序列化info.json不成功返回false
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            SkillInfo loadedSkillInfo = gson.fromJson(infoJsonBuilder.toString(), SkillInfo.class);
            if (loadedSkillInfo == null) return false;
            if (loadedSkillInfo.skillType == null)
                loadedSkillInfo.skillType = "[ " + skillName + " ]";
            SkillInfo.setName(loadedSkillInfo, skillName);
            newSkillInfo = loadedSkillInfo;
        } catch (Exception e) {
            return false;
        }
        //存在code.lua则读取
        String luaPath = directoryPath + "/" + skillName + "/code.lua";
        File codeFile = new File(luaPath);
        if (codeFile.exists()) {
            newSkillInfo.skillCode = new SkillCode();
            if (!newSkillInfo.skillCode.LoadFromLuaFile(luaPath)) {
                printLog("Lore技能 \"" + skillName + "\" 的代码文件加载失败！");
                newSkillInfo.skillCode = null;
            }
        }
        setSkill(skillName, newSkillInfo);
        return true;
    }

    public boolean saveTheSkill(String skillName, boolean changeLuaCode) {
        SkillInfo skillInfo = getSkill(skillName);
        //不存在文件夹则创建
        String skillPath = directoryPath + "/" + skillInfo.skillName;
        File skillFile = new File(skillPath);
        if (!skillFile.exists()) {
            if (!skillFile.mkdirs()) {
                printLog("Lore技能 \"" + skillInfo.skillName + "\" 文件夹创建失败！");
                return false;
            }
        }
        //序列化info.json
        String infoPath = skillPath + "/info.json";
        File infoFile = new File(infoPath);
        try {
            //序列化info.json不成功返回false
            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
            String infoJson = gson.toJson(skillInfo);
            if (infoJson == null) return false;
            //创建info.json不成功返回false
            if (!infoFile.exists()) {
                if (!infoFile.createNewFile()) return false;
            }
            //写入info.json
            java.io.FileWriter writer = new java.io.FileWriter(infoFile);
            writer.write(infoJson);
            writer.close();
        } catch (Exception e) {
            return false;
        }
        //skillInfo存在skillCode则写入
        if (changeLuaCode && skillInfo.skillCode != null) {
            String luaPath = skillPath + "/code.lua";
            File luaFile = new File(luaPath);
            //写入skillInfo.skillCode.codeBody
            try {
                //写入code.lua不成功返回false
                if (!luaFile.exists()) {
                    if (!luaFile.createNewFile()) return false;
                }
                java.io.FileWriter writer = new java.io.FileWriter(luaFile);
                writer.write(skillInfo.skillCode.preProcess.trim());
                writer.write("\n--------EndPreProcess--------\n");
                writer.write(skillInfo.skillCode.codeBody.trim());
                writer.close();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
