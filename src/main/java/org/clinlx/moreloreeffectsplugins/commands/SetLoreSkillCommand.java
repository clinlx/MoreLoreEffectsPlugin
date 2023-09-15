package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.Tools;

import java.util.Arrays;
import java.util.List;

public class SetLoreSkillCommand extends BaseCommand {

    public SetLoreSkillCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4无权使用");
            return true;
        }
        if (args.length > 3) {
            try {
                //获取技能名
                String skillName = Tools.filterColorChar(args[0]);
                //获取技能类型
                String skillType = Tools.filterColorChar(args[1]);
                //获取冷却时间
                long coolDown;
                if (args[2].equals("无穷大"))
                    coolDown = -1;
                else
                    coolDown = Long.parseLong(args[2]);
                //获取指令串
                StringBuilder skillEffectBuilder = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    skillEffectBuilder.append(args[i]);
                    if (i != args.length - 1) skillEffectBuilder.append(" ");
                }
                String skillEffect = skillEffectBuilder.toString();
                plugin.getSkillData().setSkill(skillName, skillType, coolDown, Tools.decodeColorChar(skillEffect));
                sender.sendMessage("成功设置技能: §e" + skillName + "§r(效果: “§b/" + skillEffect + "§r” ;类型: \" + skillType + \" ;冷却: " + coolDown + ")");
                if (!plugin.getSkillData().saveTheSkill(skillName, false))
                    sender.sendMessage("§4保存技能失败");
            } catch (Exception ignore) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return StartBy(args[0], plugin.getSkillData().getSkillList());
        } else if (args.length == 2) {
            return StartBy(args[1], plugin.getSkillData().tpyeList);
        } else if (args.length == 3) {
            return StartBy(args[2], Arrays.asList("无穷大", "0", "1000", "3000", "5000", "7000", "9000"));
        } else if (args.length == 4) {
            return StartBy(args[3], Arrays.asList("effect", "tp", "say"));
        } else if (args.length > 4) {
            try {
                int cutLength = 4;
                String[] newArgs = new String[args.length - cutLength];
                System.arraycopy(args, cutLength, newArgs, 0, args.length - cutLength);
                String commandName = args[cutLength - 1];
                PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);
                if (pluginCommand == null) {
                    return emptyList;
                }
                return pluginCommand.getTabCompleter().onTabComplete(sender, pluginCommand, commandName, newArgs);
            } catch (Exception e) {
                return emptyList;
            }
        } else return emptyList;
    }
}
