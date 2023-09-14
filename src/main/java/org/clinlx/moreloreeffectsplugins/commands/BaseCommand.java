package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;

import java.util.ArrayList;
import java.util.List;

public class BaseCommand implements CommandExecutor, TabExecutor {
    MoreLoreEffectsPlugin plugin;

    protected static final List<String> emptyList = new ArrayList<>();

    public BaseCommand(MoreLoreEffectsPlugin plugin) {
        this.plugin = plugin;
    }

    public void regTo(String cmdName) {
        plugin.getCommand(cmdName).setExecutor(this);
        plugin.getCommand(cmdName).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        return emptyList;
    }

}
