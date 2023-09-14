package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;

import java.util.Arrays;
import java.util.List;

public class ListLoreCommand extends BaseCommand {
    public ListLoreCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    public static List<String> getLoreList() {
        return Arrays.asList(
                "等级限制", "技能", "消耗", "攻击特效",
                "武器攻击", "攻击力", "百分比攻击", "吸血", "暴击率", "暴击伤害",
                "减伤", "百分比减伤");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String page = "all";
        if (args.length > 0) {
            if (args.length > 1) return false;
            page = args[0];
            if (!page.equals("all") &&
                    !page.equals("uni") &&
                    !page.equals("atk") &&
                    !page.equals("dfn")) return false;
        }
        sender.sendMessage("");
        sender.sendMessage("Lore中的符号均为英文符号且内容不包含空格。Lore中的颜色符号如[&0]、百分号[%]、以及小括号及其内容[(**)]会被忽略。");
        if (page.equals("all") || page.equals("uni")) {
            sender.sendMessage("----------------");
            sender.sendMessage("§6[通用]");
            sender.sendMessage("等级限制: value" + "§r§7§l (若装备者等级低于此数值，则装备会在进入战斗时掉落)");
            sender.sendMessage("技能: 技能名" + "§r§7§l (手持物品按右键触发指定名称的技能，使用/slsk命令以设置技能名称对应的效果，同类技能之间共享冷却时间)");
            sender.sendMessage("消耗: 技能名" + "§r§7§l (同“技能”，但触发结束后消耗手中物品)");
            sender.sendMessage("攻击特效: 技能名" + "§r§7§l (同“技能”，但会在攻击一个生物时触发)");
        }
        if (page.equals("all") || page.equals("atk")) {
            sender.sendMessage("----------------");
            sender.sendMessage("§d[攻击者增益]");
            sender.sendMessage("§d§l(攻击者拥有Lore词条时，禁止使用亡灵杀手与截肢杀手，这会带来无法预料的增伤)");
            sender.sendMessage("武器攻击: value" + "§r§7§l (作为主手近战时，无论其性质和附魔如何，将造成的基础伤害设为此数值。将跳劈伤害系数改为x" + plugin.getJmpAtkDmgMut() + ")");
            sender.sendMessage("攻击力: +value" + "§r§7§l (作为任意装备时，增加主手近战攻击力，在百分比增益前结算)");
            sender.sendMessage("百分比攻击: +value" + "§7§l%" + "§r§7§l (作为任意装备时，增加主手近战攻击力)");
            sender.sendMessage("吸血: +value" + "§r§7§l (作为主手近战时，增加攻击造成的回复)");
            sender.sendMessage("暴击率: +value" + "§7§l%" + "§r§7§l (作为任意装备时，增加攻击暴击的几率)");
            sender.sendMessage("暴击伤害: +value" + "§7§l%" + "§r§7§l (作为任意装备时，增加攻击暴击时的增伤比，基础倍率为150%)");
            //TODO:远程增伤
        }
        if (page.equals("all") || page.equals("dfn")) {
            sender.sendMessage("----------------");
            sender.sendMessage("§b[防守者增益]");
            sender.sendMessage("§b§l(减伤在原版护甲值后结算，减伤只能减免生物造成的伤害，无法减免：摔落，场地，药水，燃烧等异常造成的伤害)");
            sender.sendMessage("减伤: +value" + "§r§7§l (作为任意装备时，减少被生物攻击的伤害，在百分比增益前结算)");
            sender.sendMessage("百分比减伤: +value" + "§7§l%" + "§r§7§l (作为任意装备时，减少被生物攻击的伤害)");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("all", "uni", "atk", "dfn");
        } else return emptyList;
    }
}
