package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;

import java.util.List;

public class LoreSkillFileCommand extends BaseCommand {

    public LoreSkillFileCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    //TODO: 和书与笔进行交互，并且进行格式检验
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4无权使用");
            return true;
        }
        if (args.length == 0) {
            try {

            } catch (Exception ignore) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        //TODO: 补全测试
        return null;
    }
}
