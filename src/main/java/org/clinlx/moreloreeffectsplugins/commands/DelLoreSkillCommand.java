package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;

import java.io.File;
import java.util.List;

public class DelLoreSkillCommand extends BaseCommand {

    public DelLoreSkillCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4无权使用");
            return true;
        }
        if (args.length > 0) {
            if (args.length > 1) return false;
            SkillInfo skillInfo = plugin.getSkillData().getSkill(args[0]);
            if (plugin.getSkillData().delSkill(args[0])) {
                //删除info.json
                File file = new File("./loreskills/" + args[0] + "/info.json");
                if (file.exists()) {
                    file.delete();
                }
                if (skillInfo.skillEffect != null)
                    sender.sendMessage("成功将技能: §e" + args[0] + "§r(类型: " + skillInfo.skillType + " ;效果: “§b/" + skillInfo.skillEffect + "§r”)移除");
                else
                    sender.sendMessage("成功将技能: §e" + args[0] + "§r(类型: " + skillInfo.skillType + ")移除");
            } else
                sender.sendMessage("§4删除失败，技能: §e" + args[0] + "§r§4 不存在");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return plugin.getSkillData().getSkillList();
        } else
            return emptyList;
    }
}
