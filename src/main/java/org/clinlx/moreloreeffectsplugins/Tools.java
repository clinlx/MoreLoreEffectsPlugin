package org.clinlx.moreloreeffectsplugins;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Tools {

    public static String filterColorChar(String input) {
        return input.replaceAll("(§[a-zA-Z0-9])", "");
    }

    public static String filterDescChar(String input) {
        return input.replaceAll("(&[a-zA-Z0-9])|(\\(.*\\))|%", "");
    }

    public static String decodeColorChar(String input) {
        input = input.replaceAll("(&([&a-zA-Z0-9]))", "§$2");
        input = input.replaceAll("§&", "&");
        return input;
    }

    public static String encodeColorChar(String input) {
        input = input.replaceAll("&", "&&");
        input = input.replaceAll("(§([a-zA-Z0-9]))", "&$2");
        return input;
    }

    public static ItemStack[] getPlayerAllEquipments(HumanEntity player) {
        //装备列表
        ItemStack[] res = new ItemStack[6];
        //获取头盔物品
        res[0] = player.getEquipment().getHelmet();
        //获取胸甲物品
        res[1] = player.getEquipment().getChestplate();
        //获取护腿物品
        res[2] = player.getEquipment().getLeggings();
        //获取靴子物品
        res[3] = player.getEquipment().getBoots();
        //获取主手物品
        res[4] = player.getEquipment().getItemInMainHand();
        //获取副手物品
        res[5] = player.getEquipment().getItemInOffHand();
        //返回装备列表
        return res;
    }

    public static List<String> getItemLoreValueStr(MoreLoreEffectsPlugin plugin, String key, ItemStack fromItem, String cutStr) {
        if (fromItem == null) return null;
        //TODO:实现临时修改某itemstack的数值，还有“无法触发攻击词条”
        //返回值
        List<String> values = new ArrayList<>();
        try {
            //获取Lore list
            List<String> lorelist = fromItem.getItemMeta().getLore();
            //查询词条
            for (String theLore : lorelist) {
                theLore = theLore.replaceAll("(§k [\\S\\s]+§l )", "");
                String result = Tools.filterColorChar(theLore);
                String[] parts = result.split(":");
                if (parts.length != 2) continue;
                if (parts[0].trim().equals(key)) {
                    String[] numCuts = parts[1].trim().split(cutStr);
                    if (numCuts.length == 0) continue;
                    String lastNumCut = numCuts[numCuts.length - 1].trim();
                    values.add(lastNumCut);
                }
            }
        } catch (Exception ignore) {
            return null;
        }
        return values;
    }

    public static double getItemLoreValueDoubleSum(MoreLoreEffectsPlugin plugin, String key, ItemStack fromItem) {
        List<String> values = getItemLoreValueStr(plugin, key, fromItem, "\\+");
        if (values == null) return 0;
        double res = 0;
        for (String value : values) {
            try {
                value = filterDescChar(value);
                double loreValueNum = Double.parseDouble(value);
                if (loreValueNum > 0) {
                    res += loreValueNum;
                }
            } catch (Exception ignored) {
                plugin.printLog("[异常]物品‘" + fromItem.getItemMeta().getDisplayName() + "’上的词条“" + key + "”解析为double数值失败！");
            }
        }
        return res;
    }

    public static double getItemLoreValueDoubleMax(MoreLoreEffectsPlugin plugin, String key, ItemStack fromItem) {
        List<String> values = getItemLoreValueStr(plugin, key, fromItem, " ");
        if (values == null) return -1;
        double res = -1;
        for (String value : values) {
            try {
                value = filterDescChar(value);
                double loreValueNum = Double.parseDouble(value);
                res = Math.max(res, loreValueNum);
            } catch (Exception ignored) {
                plugin.printLog("[异常]物品‘" + fromItem.getItemMeta().getDisplayName() + "’上的词条“" + key + "”解析为double数值失败！");
            }
        }
        return res;
    }
}
