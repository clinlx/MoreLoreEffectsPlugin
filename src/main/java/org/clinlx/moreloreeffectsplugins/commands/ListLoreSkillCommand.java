package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;

import java.util.List;

public class ListLoreSkillCommand extends BaseCommand {

    public ListLoreSkillCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4无权使用");
            return true;
        }
        String selectType = null;
        if (args.length > 0) {
            if (!plugin.getSkillData().tpyeList.contains(args[0])) {
                sender.sendMessage("§4无效的技能类型");
                return true;
            }
            selectType = args[0];
        }
        sender.sendMessage("");
        sender.sendMessage("[当前技能列表]");
        sender.sendMessage("----------------");
        for (String skillName : plugin.getSkillData().getSkillList()) {
            SkillInfo skillInfo = plugin.getSkillData().getSkill(skillName);
            if (selectType != null && !skillInfo.skillType.equals(selectType)) {
                continue;
            }
            if (skillInfo.skillCode != null) {
                sender.sendMessage("技能: §e" + skillName + "§r(类型: " + skillInfo.skillType + " ;效果来自配置文件)");
            } else {
                sender.sendMessage("技能: §e" + skillName + "§r(效果: “§b/" + skillInfo.skillEffect + "§r” ;类型: " + skillInfo.skillType + " ;冷却: " + skillInfo.skillCoolDown + ")");
            }
        }
        sender.sendMessage("----------------");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return plugin.getSkillData().tpyeList;
        } else return emptyList;
    }
}
