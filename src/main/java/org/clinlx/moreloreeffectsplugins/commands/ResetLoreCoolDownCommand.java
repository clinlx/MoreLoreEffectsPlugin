package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;

import java.util.ArrayList;
import java.util.List;

public class ResetLoreCoolDownCommand extends BaseCommand {
    public ResetLoreCoolDownCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4无权使用");
            return true;
        }
        if (args.length != 2) {
            return false;
        }
        String playerName = args[0];
        String skillName = args[1];
        boolean allPlayer = playerName.equals("*");
        boolean allSkill = skillName.equals("*");
        if (!allPlayer && !Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(playerName))) {
            sender.sendMessage("§4玩家不存在");
            return true;
        }
        if (!allSkill && !plugin.getSkillData().getSkillList().contains(skillName)) {
            sender.sendMessage("§4技能不存在");
            return true;
        }
        if (allPlayer && allSkill) {
            plugin.getSkillData().clearAllCoolDownSys();
            sender.sendMessage("§a已重置所有玩家所有技能冷却");
        }
        if (allPlayer && !allSkill) {
            String skillType = plugin.getSkillData().getSkill(skillName).skillType;
            plugin.getSkillData().clearTypeCoolDownSys(skillType);
            sender.sendMessage("§a已重置所有玩家的" + skillName + "技能冷却");
        }
        if (!allPlayer && allSkill) {
            plugin.getSkillData().clearPlayerCoolDownSys(playerName);
            sender.sendMessage("§a已重置" + playerName + "的所有技能冷却");
        }
        if (!allPlayer && !allSkill) {
            String skillType = plugin.getSkillData().getSkill(skillName).skillType;
            plugin.getSkillData().clearPlayerTypeCoolDownSys(playerName, skillType);
            sender.sendMessage("§a已重置" + playerName + "的" + skillName + "技能冷却");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            //补全玩家名
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> playerNames.add(player.getName()));
            return playerNames;
        } else if (args.length == 2) {
            //补全技能名
            return plugin.getSkillData().getSkillList();
        }
        return emptyList;
    }
}
