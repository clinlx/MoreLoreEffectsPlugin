package org.clinlx.moreloreeffectsplugins.skilsys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.luaj.LuaStopException;
import org.luaj.vm2.LuaValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class SkillDataJsonSaver {
    public static void saveSkillData(String dir, String key, Object skillData) throws LuaStopException {
        if (skillData != null && !(skillData instanceof Integer) && !(skillData instanceof Double) && !(skillData instanceof String))
            throw new LuaStopException("Data must be in {Integer Double String}");
        StringBuilder lockStrBuilder = new StringBuilder();
        lockStrBuilder.append(dir);
        lockStrBuilder.append("/");
        lockStrBuilder.append(key);
        String lockStr = lockStrBuilder.toString().intern();
        dir = "./loreskilldatas/" + dir;
        synchronized (lockStr) {
            try {
                new File(dir).mkdirs();
                File file = new File(dir, key + ".json");
                if (!file.exists()) {
                    file.createNewFile();
                }
                if (skillData == null) {
                    file.delete();
                    return;
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(skillData);
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(json);
                fileWriter.flush();
                fileWriter.close();
            } catch (Exception e) {
                MoreLoreEffectsPlugin.getInstance().printLog("保存技能数据" + key + "失败。" + e.getMessage());
            }
        }
    }

    public static Object loadSkillData(String dir, String key) {
        StringBuilder lockStrBuilder = new StringBuilder();
        lockStrBuilder.append(dir);
        lockStrBuilder.append("/");
        lockStrBuilder.append(key);
        String lockStr = lockStrBuilder.toString().intern();
        dir = "./loreskilldatas/" + dir;
        synchronized (lockStr) {
            Gson gson = new GsonBuilder().create();
            File file = new File(dir, key + ".json");
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                if (!file.exists()) {
                    return null;
                }
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
                String json = content.toString();
                Object result = gson.fromJson(json, Object.class);
                if (result instanceof Double && !json.contains("."))
                    result = (int) (double) result;
                return result;
            } catch (Exception e) {
                MoreLoreEffectsPlugin.getInstance().printLog("读取技能数据" + key + "失败。" + e.getMessage());
            }
        }
        return null;
    }

    public static Object AddSkillData(String dir, String key, Object skillData) throws LuaStopException {
        Object oldData = loadSkillData(dir, key);
        if (skillData == null) {
            skillData = oldData;
        } else if (oldData != null) {
            if (oldData instanceof String || skillData instanceof String) {
                skillData = oldData.toString() + skillData.toString();
            } else if (oldData instanceof Integer && skillData instanceof Integer) {
                skillData = (int) oldData + (int) skillData;
            } else {
                if (oldData instanceof Integer) oldData = (double) (int) oldData;
                if (skillData instanceof Integer) skillData = (double) (int) skillData;
                skillData = (double) oldData + (double) skillData;
            }
        }
        saveSkillData(dir, key, skillData);
        return skillData;
    }

    public static void LockSkillData(String dir, String key, LuaValue func) {
        StringBuilder lockStrBuilder = new StringBuilder();
        lockStrBuilder.append(dir);
        lockStrBuilder.append("/");
        lockStrBuilder.append(key);
        String lockStr = lockStrBuilder.toString().intern();
        synchronized (lockStr) {
            func.call();
        }
    }
}
