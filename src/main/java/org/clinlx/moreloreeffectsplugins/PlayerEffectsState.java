package org.clinlx.moreloreeffectsplugins;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.clinlx.moreloreeffectsplugins.Tools.getPlayerAllEquipments;

interface EffectValueChange {
    void handle(double a, double b, Player player);
}

public class PlayerEffectsState {
    //保存主类的实例
    private final MoreLoreEffectsPlugin mainPlugin;
    
    public PlayerEffectsState(MoreLoreEffectsPlugin mainPlugin) {
        this.mainPlugin = mainPlugin;
        playerEffects = new HashMap<>();
        effectRegister = new HashMap<>();
        effectRegister.put("逗比值", (oldValue, newValue, player) -> {
            
        });
    }
    
    private final HashMap<String, EffectValueChange> effectRegister;
    private final HashMap<Player, HashMap<String, Double>> playerEffects;
    
    public void updatePlayerLoreEffects(Player player) {
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                updatePlayerLoreEffectsNow(player);
//            }
//        }.runTaskLater(mainPlugin, 1L);
    }
    
    public void updatePlayerLoreEffectsNow(Player player) {
        if (!playerEffects.containsKey(player))
            playerEffects.put(player, new HashMap<>());
        HashMap<String, Double> oldDictionary = playerEffects.get(player);
        HashMap<String, Double> dictionary = new HashMap<>();
        for (String key : effectRegister.keySet()) {
            dictionary.put(key, (double) 0);
        }
        //获取玩家装备列表
        ItemStack[] equipments = getPlayerAllEquipments(player);
        //计算不同装备数值和
        for (int i = 0; i < 6; i++) {
            ItemStack item = equipments[i];
            if (item == null) continue;
            try {
                //获取Lore list
                List<String> lorelist = item.getItemMeta().getLore();
                //查询词条
                for (String theLore : lorelist) {
                    String result = Tools.filterColorChar(theLore);
                    result = Tools.filterDescChar(result);
                    String[] parts = result.split(":");
                    if (parts.length != 2) continue;
                    String head = parts[0].trim();
                    String end = parts[1].trim();
                    String[] numCuts = end.split("\\+");
                    for (String key : effectRegister.keySet()) {
                        if (head.equals(key)) {
                            if (numCuts.length == 0) continue;
                            String lastNumCut = numCuts[numCuts.length - 1].trim();
                            try {
                                double loreValueNum = Double.parseDouble(lastNumCut);
                                if (loreValueNum > 0) {
                                    dictionary.put(key, dictionary.get(key) + loreValueNum);
                                }
                            } catch (Exception ignored) {
                                mainPlugin.printLog("[异常]一个物品的" + key + "词条的值获取失败！");
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        
        for (Map.Entry<String, EffectValueChange> entry : effectRegister.entrySet()) {
            double oldValue = getPlayerEffects(player, entry.getKey());
            double newValue = dictionary.get(entry.getKey());
            if (oldValue != newValue) {
                mainPlugin.printLog("玩家" + player.getName() + "的属性[" + entry.getKey() + "]从(+" + oldValue + ")更新为(+" + newValue + ")");
                entry.getValue().handle(oldValue, newValue, player);
                oldDictionary.put(entry.getKey(), newValue);
            }
        }
    }
    
    public double getPlayerEffects(Player player, String key) {
        Map<String, Double> dictionary = playerEffects.get(player);
        Double value = dictionary.get(key);
        if (value == null) {
            return 0;
        }
        return value;
    }
}
