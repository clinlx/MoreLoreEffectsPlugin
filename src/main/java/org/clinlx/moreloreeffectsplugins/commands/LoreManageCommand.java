package org.clinlx.moreloreeffectsplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LoreManageCommand extends BaseCommand {
    private static final HashMap<String, List<String>> clipboard = new HashMap<>();

    public LoreManageCommand(MoreLoreEffectsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4无权使用");
            return true;
        }
        if (!(sender instanceof Player)) { // 判断输入者的类型 为了防止出现 控制台或命令方块 输入的情况
            sender.sendMessage("执行者必须是一名玩家!");
            return true;
        }
        Player player = (Player) sender;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null) {
            player.sendMessage("§4必须手持一件物品");
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (args[0].equals("add")) {
            if (args.length == 3) {
//                if (ListLoreCommand.getLoreList().contains(args[1])) {
                String LoreWord = Tools.decodeColorChar(args[1]);
                String LoreValue = Tools.decodeColorChar(args[2]);
                ItemMeta itemMeta = mainHand.getItemMeta();
                List<String> lores = itemMeta.getLore();
                if (lores == null) lores = new ArrayList<>();
                lores.add(LoreWord + ": " + LoreValue);
                itemMeta.setLore(lores);
                mainHand.setItemMeta(itemMeta);
                player.sendMessage("§a已将Lore词条§6" + LoreWord + "§a添加到物品");
//                } else {
//                    player.sendMessage("§4Lore词条不存在");
//                }
                return true;
            } else
                return false;
        }
        if (args.length == 1) {
            if (args[0].equals("copy")) {
                clipboard.put(player.getName(), mainHand.getItemMeta().getLore());
                player.sendMessage("§a已将物品Lore复制到剪切板");
            } else if (args[0].equals("paste")) {
                if (clipboard.containsKey(player.getName())) {
                    ItemMeta itemMeta = mainHand.getItemMeta();
                    itemMeta.setLore(clipboard.get(player.getName()));
                    mainHand.setItemMeta(itemMeta);
                    player.sendMessage("§a已将剪切板Lore粘贴到物品");
                } else {
                    player.sendMessage("§4剪切板为空");
                }
            } else if (args[0].equals("clear")) {
                ItemMeta itemMeta = mainHand.getItemMeta();
                itemMeta.setLore(null);
                mainHand.setItemMeta(itemMeta);
                player.sendMessage("§a已清空物品Lore");
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "copy", "paste", "clear");
        } else if (args[0].equals("add")) {
            if (args.length == 2) {
                return ListLoreCommand.getLoreList();
            } else if (args.length == 3) {
                if (args[1].equals("技能") || args[1].equals("消耗") || args[1].equals("攻击特效")) {
                    return plugin.getSkillData().getSkillList();
                }
                return Arrays.asList("0", "+0", "+0%");
            }
        }
        return null;
    }
}
