package org.clinlx.moreloreeffectsplugins;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class MoreLoreEffectsPlugin extends JavaPlugin {
    
    public Random random;
    private double jmpAtkDmgAddPrg = 0.05;//Def: 0.5
    
    public double getJmpAtkDmgMut() {
        return jmpAtkDmgAddPrg > 0 ? jmpAtkDmgAddPrg + 1 : 1.5;
    }
    
    @Override
    public void onEnable() {
        //初始化
        random = new Random();
        timePlayerLastUseSkill = new HashMap<>();
        LoadSkills();
        //功能
        playerState = new PlayerEffectsState(this);
        EventsListener damageEventListener = new EventsListener(this);
        Bukkit.getPluginManager().registerEvents(damageEventListener, this);
        printLog("更多Lore词条插件启动，版本：" + getDescription().getVersion());
    }
    
    @Override
    public void onDisable() {
        SaveSkills();
    }
    
    private void LoadSkills() {
        skillsRegister = new HashMap<>();
        try {
            String line = "";
            File file = new File("./LoreSkillsList.data");
            InputStreamReader streamReader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    String[] saves = line.trim().split(" ");
                    //技能名
                    byte[] decodedBytes1 = Base64.getDecoder().decode(saves[0]);
                    if (decodedBytes1 == null) throw new Exception("[技能名]项解码失败!");
                    String skillName = new String(decodedBytes1, StandardCharsets.UTF_8);
                    //冷却信息
                    long skillCoolDown = Long.parseLong(saves[1]);
                    //效果信息
                    byte[] decodedBytes2 = Base64.getDecoder().decode(saves[2]);
                    if (decodedBytes2 == null) throw new Exception("[技能效果]项解码失败!");
                    String skillEffect = new String(decodedBytes2, StandardCharsets.UTF_8);
                    skillsRegister.put(skillName, new SkillInfo(skillEffect, skillCoolDown));
                } catch (Exception ignore) {
                    printLog("Lore技能列表文件中读取到一行错误的数据：" + line);
                    continue;
                }
            }
        } catch (Exception ignore) {
            printLog("Lore技能列表文件打开失败！");
        }
    }
    
    private void SaveSkills() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("./LoreSkillsList.data", false);
            for (Map.Entry<String, SkillInfo> skill : skillsRegister.entrySet()) {
                try {
                    //存储技能名
                    byte[] bytes1 = skill.getKey().getBytes(StandardCharsets.UTF_8);
                    fileOutputStream.write(Base64.getEncoder().encodeToString(bytes1).getBytes());
                    fileOutputStream.write(" ".getBytes());
                    //存储冷却信息
                    fileOutputStream.write(Long.toString(skill.getValue().skillCoolDown).getBytes());
                    fileOutputStream.write(" ".getBytes());
                    //存储效果信息
                    byte[] bytes2 = skill.getValue().skillEffect.getBytes(StandardCharsets.UTF_8);
                    fileOutputStream.write(Base64.getEncoder().encodeToString(bytes2).getBytes());
                    fileOutputStream.write("\n".getBytes());
                } catch (IOException e) {
                    continue;
                }
            }
        } catch (FileNotFoundException e) {
            printLog("保存数据到Lore技能列表文件失败！");
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (Exception e) {
                printLog("关闭文件出错!");
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lorelist")) {
            if (!(sender instanceof Player)) { // 判断输入者的类型 为了防止出现 控制台或命令方块 输入的情况
                sender.sendMessage("执行者必须是一名玩家!");
                return true;
            }
            String page = "all";
            if (args.length > 0) {
                if (args.length > 1) return false;
                page = args[0];
                if (!page.equals("all") &&
                        !page.equals("uni") &&
                        !page.equals("atk") &&
                        !page.equals("dfn")) return false;
            }
            Player player = (Player) sender;
            player.sendMessage("");
            player.sendMessage("Lore中的符号均为英文符号且内容不包含空格。Lore中的颜色符号如[&0]、百分号[%]、以及小括号及其内容[(**)]会被忽略。");
            if (page.equals("all") || page.equals("uni")) {
                player.sendMessage("----------------");
                player.sendMessage("§6[通用]");
                player.sendMessage("等级限制: value" + "§r§7§l (若装备者等级低于此数值，则装备会在进入战斗时掉落)");
                player.sendMessage("技能: 技能名" + "§r§7§l (手持物品按右键触发指定名称的技能，使用/slsk命令以设置技能名称对应的效果，任何物品之间共享冷却时间)");
                player.sendMessage("消耗: 技能名" + "§r§7§l (同上，但触发结束后消耗手中物品，无冷却时间)");
            }
            if (page.equals("all") || page.equals("atk")) {
                player.sendMessage("----------------");
                player.sendMessage("§d[攻击者增益]");
                player.sendMessage("§d§l(攻击者拥有Lore词条时，禁止使用亡灵杀手与截肢杀手，这会带来无法预料的增伤)");
                player.sendMessage("武器攻击: value" + "§r§7§l (作为主手近战时，无论其性质和附魔如何，将造成的基础伤害设为此数值。将跳劈伤害系数改为x" + getJmpAtkDmgMut() + ")");
                player.sendMessage("攻击力: +value" + "§r§7§l (作为任意装备时，增加主手近战攻击力，在百分比增益前结算)");
                player.sendMessage("百分比攻击: +value" + "§7§l%" + "§r§7§l (作为任意装备时，增加主手近战攻击力)");
                player.sendMessage("吸血: +value" + "§r§7§l (作为主手近战时，增加攻击造成的回复)");
                player.sendMessage("暴击率: +value" + "§7§l%" + "§r§7§l (作为任意装备时，增加攻击暴击的几率)");
                player.sendMessage("暴击伤害: +value" + "§7§l%" + "§r§7§l (作为任意装备时，增加攻击暴击时的增伤比，基础倍率为150%)");
            }
            if (page.equals("all") || page.equals("dfn")) {
                player.sendMessage("----------------");
                player.sendMessage("§b[防守者增益]");
                player.sendMessage("§b§l(减伤在原版护甲值后结算，减伤只能减免生物造成的伤害，无法减免：摔落，场地，药水，燃烧等异常造成的伤害)");
                player.sendMessage("减伤: +value" + "§r§7§l (作为任意装备时，减少被生物攻击的伤害，在百分比增益前结算)");
                player.sendMessage("百分比减伤: +value" + "§7§l%" + "§r§7§l (作为任意装备时，减少被生物攻击的伤害)");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("loreskills")) {
            if (!sender.isOp()) {
                sender.sendMessage("§4无权使用");
                return true;
            }
            sender.sendMessage("");
            sender.sendMessage("[当前技能列表]");
            sender.sendMessage("----------------");
            for (String skillName : skillsRegister.keySet()) {
                SkillInfo skillInfo = skillsRegister.get(skillName);
                sender.sendMessage("技能: §e" + skillName + "§r(效果: “§b/" + skillInfo.skillEffect + "§r” ;冷却: " + skillInfo.skillCoolDown + ")");
            }
            sender.sendMessage("----------------");
            return true;
        }
        if (command.getName().equalsIgnoreCase("setloreskill")) {
            if (!sender.isOp()) {
                sender.sendMessage("§4无权使用");
                return true;
            }
            if (args.length > 3) {
                try {
                    String skillName = Tools.filterColorChar(args[0]);
                    long coolDown = Long.parseLong(args[1]);
                    StringBuilder skillEffectBuilder = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        for (int j = 0; j < args[i].length(); j++) {
                            if (j + 1 < args[i].length() && args[i].charAt(j) == '&') {
                                if ((Character.isDigit(args[i].charAt(j + 1)) || Character.isLetter(args[i].charAt(j + 1))))
                                    skillEffectBuilder.append('§');
                                else {
                                    if (args[i].charAt(j + 1) == '&') j++;
                                    skillEffectBuilder.append('&');
                                }
                            } else skillEffectBuilder.append(args[i].charAt(j));
                        }
                        skillEffectBuilder.append(" ");
                    }
                    String skillEffect = skillEffectBuilder.toString();
                    sender.sendMessage("成功设置技能: §e" + skillName + "§r(效果: “§b/" + skillEffect + "§r” ;冷却: " + coolDown + ")");
                    skillsRegister.put(skillName, new SkillInfo(skillEffect, coolDown));
                } catch (Exception ignore) {
                    return false;
                }
                return true;
            }
            return false;
        }
        if (command.getName().equalsIgnoreCase("delloreskill")) {
            if (!sender.isOp()) {
                sender.sendMessage("§4无权使用");
                return true;
            }
            if (args.length > 0) {
                if (args.length > 1) return false;
                if (!skillsRegister.containsKey(args[0])) {
                    sender.sendMessage("§4删除失败，技能: §e" + args[0] + "§r§4 不存在");
                    return true;
                }
                sender.sendMessage("成功将技能: §e" + args[0] + "§r(效果: “§b/" + skillsRegister.get(args[0]).skillEffect + "§r”)移除");
                skillsRegister.remove(args[0]);
                return true;
            }
            return false;
        }
        if (command.getName().equalsIgnoreCase("loadskillcode")) {
            if (!sender.isOp()) {
                sender.sendMessage("§4无权使用");
                return true;
            }
            
            return false;
        }
        if (args.length > 2) {
            try {
                String skillName = Tools.filterColorChar(args[0]);
                
                //String skillEffect = ;
                //skillsRegister.put(skillName, new SkillInfo(skillEffect, coolDown));
            } catch (Exception ignore) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    public void printLog(String message) {
        getLogger().info(message);
    }
    
    private PlayerEffectsState playerState;
    
    public PlayerEffectsState getPlayerState() {
        return playerState;
    }
    
    public HashMap<String, SkillInfo> skillsRegister;
    public HashMap<String, Long> timePlayerLastUseSkill;
    
}
