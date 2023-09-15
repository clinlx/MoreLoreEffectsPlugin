package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.Tools;
import org.clinlx.moreloreeffectsplugins.skilsys.CoolDownInfo;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckLoreSkillCommand extends BaseCommand {
    public CheckLoreSkillCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (!(sender instanceof Player)) { // 判断输入者的类型 为了防止出现 控制台或命令方块 输入的情况
//            sender.sendMessage("执行者必须是一名玩家!");
//            return true;
//        }
//        Player player = (Player) sender;
//        ItemStack mainHand = player.getInventory().getItemInMainHand();
//        if (mainHand == null) {
//            player.sendMessage("§4必须手持一件物品");
//            return true;
//        }
        if (args.length != 1) {
            return false;
        }
        SkillInfo skillInfo = plugin.getSkillData().getSkill(args[0]);
        sender.sendMessage("技能名: [" + skillInfo.skillName + "]");
        sender.sendMessage("技能类型: '" + skillInfo.skillType + "'");
        if (skillInfo.skillCoolDown == CoolDownInfo.runningSign)
            sender.sendMessage("技能冷却: 无限久");
        else
            sender.sendMessage("技能冷却: " + skillInfo.skillCoolDown * 0.001 + "秒");
        sender.sendMessage("技能描述: \"" + skillInfo.skillInformation + "\"");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return emptyList;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            // 如果玩家手持的物品为空, 则返回所有技能
            if (mainHand == null || mainHand.getItemMeta() == null || mainHand.getItemMeta().getLore() == null) {
                return StartBy(args[0], plugin.getSkillData().getSkillList());
            }
            List<String> skillNameList = Tools.getItemLoreValueStr(plugin, "技能", mainHand, " ");
            List<String> cSkNameList = Tools.getItemLoreValueStr(plugin, "消耗", mainHand, " ");
            List<String> aSkNameList = Tools.getItemLoreValueStr(plugin, "攻击特效", mainHand, " ");
            Set<String> set = new HashSet<>();                  // 创建一个空的 Set
            // 将三个列表中的元素添加到 Set 中
            set.addAll(skillNameList);
            set.addAll(cSkNameList);
            set.addAll(aSkNameList);
            // 将 Set 转换为 List 并返回
            return StartBy(args[0], new ArrayList<>(set));
        }
        return emptyList;
    }
}
