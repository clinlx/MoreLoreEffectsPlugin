package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;

import java.util.List;

public class ReLoadLoreSkillCommand extends BaseCommand {
    public ReLoadLoreSkillCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4无权使用");
            return true;
        }
        if (args.length != 1) {
            return false;
        }
        if (args[0].equals("*")) {
            plugin.getSkillData().reLoadSkills();
            sender.sendMessage("§a重新加载所有技能");
        } else if (plugin.getSkillData().loadTheSkill(args[0])) {
            sender.sendMessage("§a重新加载技能: §e" + args[0] + "§r成功");
        } else {
            sender.sendMessage("§4重新加载失败，技能: §e" + args[0] + "§r§4 的文件不存在");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return plugin.getSkillData().getSkillList();
        } else
            return emptyList;
    }
}
