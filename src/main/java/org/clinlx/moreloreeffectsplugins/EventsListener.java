package org.clinlx.moreloreeffectsplugins;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.clinlx.moreloreeffectsplugins.skilsys.CoolDownInfo;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;
import org.clinlx.moreloreeffectsplugins.skilsys.luaj.SkillThread;

import java.util.List;

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

public class EventsListener implements Listener {
    //保存主类的实例
    private final MoreLoreEffectsPlugin mainPlugin;

    //构造方法
    public EventsListener(MoreLoreEffectsPlugin mainPlugin) {
        this.mainPlugin = mainPlugin;
    }

    //简化调用
    private double getValueDouble(String key, ItemStack item) {
        return Tools.getItemLoreValueDoubleSum(mainPlugin, key, item);
    }

    private boolean wrongLevelLimit(MoreLoreEffectsPlugin plugin, HumanEntity entity, int itemKind, ItemStack item, Cancellable event, boolean playerIsAtk, boolean tryDrop) {
        //非玩家跳过检测
        if (!(entity instanceof Player)) return false;
        Player player = (Player) entity;
        if (Tools.getItemLoreValueDoubleMax(plugin, "等级限制", item) > player.getLevel()) {
            if (tryDrop) {
                player.sendMessage("[提示]§4你觉得自己现有的等级驾驭不住“§e" + (item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : (item.getItemMeta().hasLocalizedName() ? item.getItemMeta().getLocalizedName() : item.getType().name())) + "§4”的力量，一开始战斗，它便§5从你身上滑落§4。");
                switch (itemKind) {
                    case -1:
                        break;
                    case 0:
                        if (player.getInventory().getHelmet() != null) {
                            player.getInventory().setHelmet(null);
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                        break;
                    case 1:
                        if (player.getInventory().getChestplate() != null) {
                            player.getInventory().setChestplate(null);
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                        break;
                    case 2:
                        if (player.getInventory().getLeggings() != null) {
                            player.getInventory().setLeggings(null);
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                        break;
                    case 3:
                        if (player.getInventory().getBoots() != null) {
                            player.getInventory().setBoots(null);
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                        break;
                    case 4:
                        if (player.getInventory().getItemInMainHand() != null) {
                            player.getInventory().setItemInMainHand(null);
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                        break;
                    case 5:
                        if (player.getInventory().getItemInOffHand() != null) {
                            player.getInventory().setItemInOffHand(null);
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                        break;
                }
                //玩家如果是攻击者，取消攻击事件
                if (playerIsAtk) {
                    event.setCancelled(true);
                }
            }
            return true;
        }
        return false;
    }

    //任何生物被其他生物攻击时触发
    @EventHandler
    public void OnEntityDamaged(EntityDamageByEntityEvent event) {
        //获取伤害量便于修改
        double newDmg = event.getDamage();
        //获取信息
        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();
        //攻击者为类人
        if (damagerEntity instanceof HumanEntity) {
            //转换攻击者类人对象
            HumanEntity damagerPlayer = (HumanEntity) damagerEntity;
            //结算基础伤害
            double tryBaseDmg = Tools.getItemLoreValueDoubleMax(mainPlugin, "武器攻击", damagerPlayer.getInventory().getItemInMainHand());
            if (tryBaseDmg >= 0) {
                newDmg = tryBaseDmg;
            }
            //定义数值总和
            double incDmgSum = 0;
            double incDmgPrgSum = 0;
            double whackChance = 0;
            double extraWhackDamage = 0;
            double healNumSum = 0;
            //获取玩家装备列表
            ItemStack[] equipments = Tools.getPlayerAllEquipments(damagerPlayer);
            ItemStack mainHandItem = equipments[4];
            ItemStack offHandItem = equipments[5];
            //计算不同装备数值和
            boolean levelRight = true;
            for (int i = 0; i < 6; i++) {
                ItemStack item = equipments[i];
                //不满足等级要求的部位强制掉落
                if (wrongLevelLimit(mainPlugin, damagerPlayer, i, item, event, true, true)) {
                    levelRight = false;
                    continue;
                }
                //统计装备属性
                incDmgSum += getValueDouble("攻击力", item);
                incDmgPrgSum += getValueDouble("百分比攻击", item);
                whackChance += getValueDouble("暴击率", item);
                extraWhackDamage += getValueDouble("暴击伤害", item);
            }
            if (!levelRight) {
                event.setCancelled(true);
                return;
            }
            //仅主手起效
            healNumSum += getValueDouble("吸血", mainHandItem);
            //对修改过基础攻击的武器重新做算法，计算实际攻击伤害
            //附魔增伤计算：
            double realSpcIncDmg = 0;
            //锋利
            int Ect_ALL = damagerPlayer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
            if (Ect_ALL > 0)
                realSpcIncDmg += (Ect_ALL + 1) * 0.5;
//            //亡灵杀手
//            int Ect_UNDEAD = damagerPlayer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
//            //截肢杀手
//            int Ect_ARTHROPODS = damagerPlayer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
            //获取基础伤害
            double realBaseDmg = damagerPlayer.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
            //实际伤害去掉附魔增伤，剩下的就是跳劈等原版的加成
            double realFinalDmg = event.getDamage() - realSpcIncDmg;
            double prgFromBase = 1.0;
            if (realBaseDmg > 0) {
                prgFromBase = realFinalDmg / realBaseDmg;
                //跳劈等原版加成上限
                if (prgFromBase > mainPlugin.getJmpAtkDmgMut())
                    prgFromBase = mainPlugin.getJmpAtkDmgMut();
            }
            //为修改了基础伤害的武器增加跳劈
            if (tryBaseDmg >= 0) {
                if (prgFromBase > 1.0) {
                    newDmg *= prgFromBase;
                }
            }
            //【结算数值】
            //结算攻击力变更
            if (incDmgSum != 0) {
                newDmg += incDmgSum;
            }
            //结算百分比攻击力变更
            if (incDmgPrgSum != 0) {
                if (incDmgPrgSum < -100) incDmgPrgSum = -100;
                newDmg *= (100.0 + incDmgPrgSum) * 0.01;
            }
            //结算暴击率
            if (whackChance > 0) {
                if (mainPlugin.random.nextInt(10000) < whackChance * 100) {
                    //结算暴击伤害
                    newDmg *= 1.5 + (extraWhackDamage * 0.01);
                    try {
                        // 在目标位置播放粒子效果
                        Location location = targetEntity.getLocation();
                        location.setY(location.getY() + 1);
                        damagerPlayer
                                .getWorld()
                                .spawnParticle(Particle.REDSTONE, location, 75, 0.6, 1.2, 0.6);
                    } catch (Exception ignore) {
                        mainPlugin.printLog("播放暴击粒子异常");
                    }
                }
            }
            //结算吸血
            if (healNumSum > 0) {
                //计算新生命值
                double newHealth = damagerPlayer.getHealth() + healNumSum;
                //获取攻击者上限
                double playerMaxHealth = damagerPlayer.getAttribute(GENERIC_MAX_HEALTH).getValue();
                //确保不超出上限
                if (newHealth > playerMaxHealth) {
                    newHealth = playerMaxHealth;
                }
                damagerPlayer.setHealth(newHealth);
            }
            //蓄力不满的减伤
            if (prgFromBase < 0.95) {
                newDmg *= prgFromBase;
            } else {
                if (damagerPlayer instanceof Player) {
                    Player player = (Player) damagerPlayer;
                    String playerName = player.getName();
                    //TODO: 蓄力满的加成
                    //触发主手攻击特效
                    //TODO: 触发主手攻击特效(和技能基本类似但是临时可以设置Lore属性的乘数或绝对值)
                    //获取物品的技能词条
                    List<String> skillNameList = Tools.getItemLoreValueStr(mainPlugin, "攻击特效", mainHandItem, " ");
                    //确认物品有技能词条
                    if (skillNameList == null || skillNameList.size() == 0)
                        return;
                    //是否禁止物品右键事件触发
                    boolean cancelButton = false;
                    //遍历触发每一个技能
                    for (String skillName : skillNameList) {
                        //确认这个技能存在
                        if (mainPlugin.getSkillData().skillExist(skillName)) {
                            //获取技能信息
                            SkillInfo skillInfo = mainPlugin.getSkillData().getSkill(skillName);
                            //确认是否取消按键
                            if (skillInfo.cancelMouseButton)
                                cancelButton = true;
                            //确认技能CD
                            if (skillInfo.getCoolDownInfo(playerName).skillTypeReady()) {
                                //触发技能
                                skillInfo.invoke(playerName, true, "Attack", null);
                                //TODO: 增加字段，触发时是否提醒
                                player.sendMessage("触发攻击特效[§e" + skillName + "§r]");
                            } else {
                                //技能在CD
                                if (System.currentTimeMillis() - skillInfo.getCoolDownInfo(playerName).getLastUseTime() >= 10
                                        && skillInfo.echoCoolDownLeftInform) {
                                    if (skillInfo.getCoolDownInfo(playerName).getWaitTime() == CoolDownInfo.runningSign) {
                                        //player.sendMessage("");
                                    } else {
                                        player.sendMessage("§4在触发攻击特效[§e" + skillName + "§r§4]前还需要等待" + skillInfo.getCoolDownInfo(playerName).getWaitTimeStr() + "§4的时间！");
                                    }
                                }
                            }
                        } else {
                            //技能不存在
                            player.sendMessage("攻击特效[§e" + skillName + "§r]的效果不存在！");
                        }
                    }
                    //设置物品右键事件触发情况
                    event.setCancelled(cancelButton);
                }
            }
        }
        //受击者为类人
        if (targetEntity instanceof HumanEntity) {
            //转换受击者类人对象
            HumanEntity targetPlayer = (HumanEntity) targetEntity;
            //定义减伤量总和
            double recDmgSum = 0;
            //定义减伤百分比总和
            double recDmgPrgSum = 0;
            //获取玩家装备列表
            ItemStack[] equipments = Tools.getPlayerAllEquipments(targetPlayer);
            //计算不同装备数值和
            for (int i = 0; i < 6; i++) {
                ItemStack item = equipments[i];
                //不满足等级要求的部位强制掉落
                if (wrongLevelLimit(mainPlugin, targetPlayer, i, item, event, false, true))
                    continue;
                //统计装备属性
                recDmgSum += getValueDouble("减伤", item);
                recDmgPrgSum += getValueDouble("百分比减伤", item);
            }
            //【结算数值】
            //结算护甲值
            double targetEntityDef = targetPlayer.getAttribute(Attribute.GENERIC_ARMOR).getValue();
            double targetEntityTOU = targetPlayer.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
            newDmg = Math.ceil(newDmg * (1 - Math.min(20, Math.max(targetEntityDef / 5, targetEntityDef - newDmg / (2 + (targetEntityTOU / 4))) / 25)));
            //结算减伤
            if (recDmgSum != 0) {
                newDmg -= recDmgSum;
            }
            //结算百分比减伤
            if (recDmgPrgSum != 0) {
                if (recDmgPrgSum > 100) recDmgPrgSum = 100;
                if (recDmgPrgSum < 0) recDmgPrgSum = 0;
                newDmg *= (100.0 - recDmgPrgSum) * 0.01;
            }
        }
        //确保伤害合法
        if (newDmg <= 0) {
            newDmg = 0;
        }
        //设置伤害
        event.setDamage(newDmg);
    }

    //玩家手持物品槽位变更时触发
    @EventHandler
    public void OnChangedHeld(PlayerItemHeldEvent event) {
        mainPlugin.getPlayerState().updatePlayerLoreEffects(event.getPlayer());
    }

    //当玩家死亡时触发
    @EventHandler
    public void OnPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (long id : SkillThread.getPlayerSkillThreads(player.getName())) {
            SkillThread.FinishLuaSKill(id);
        }
    }

    //玩家重生事件触发
    @EventHandler
    public void OnRespawn(PlayerRespawnEvent event) {
        mainPlugin.getPlayerState().updatePlayerLoreEffects(event.getPlayer());
    }

    //玩家进入服务器事件触发
    @EventHandler
    public void OnJoinServer(PlayerJoinEvent event) {
        mainPlugin.getPlayerState().updatePlayerLoreEffects(event.getPlayer());
    }

    //玩家退出服务器事件触发
    @EventHandler
    public void OnQuitServer(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (long id : SkillThread.getPlayerSkillThreads(player.getName())) {
            SkillThread.FinishLuaSKill(id);
        }
    }

    //当玩家关闭背包时触发
    @EventHandler
    public void OnInventoryClose(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            mainPlugin.getPlayerState().updatePlayerLoreEffects(player);
        }
    }

    //玩家丢出物品时触发
    @EventHandler
    public void OnDropItem(PlayerDropItemEvent event) {
        mainPlugin.getPlayerState().updatePlayerLoreEffects(event.getPlayer());
    }

    //当玩家与装甲架交互并且进行交换, 取回或放置物品时触发本事件
    @EventHandler
    public void OnArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        mainPlugin.getPlayerState().updatePlayerLoreEffects(event.getPlayer());
    }

    //某玩家工具耐久消耗完毕时触发
    @EventHandler
    public void OnItemBreak(PlayerItemBreakEvent event) {
        mainPlugin.getPlayerState().updatePlayerLoreEffects(event.getPlayer());
    }

    //任何生物拾取物品时触发
    @EventHandler
    public void OnItemBreak(EntityPickupItemEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            mainPlugin.getPlayerState().updatePlayerLoreEffects(player);
        }
    }

    //玩家用快捷键互换主手和副手的物品时触发本事件
    @EventHandler
    public void OnSwapHandItems(PlayerSwapHandItemsEvent event) {
        mainPlugin.getPlayerState().updatePlayerLoreEffects(event.getPlayer());
    }

    //当玩家对一个对象或空气进行交互时触发本事件
    @EventHandler
    public void OnPlayerClick(PlayerInteractEvent event) {
        //忽略空手点击
        if (event.getItem() == null)
            return;
        //获取数据
        Player player = event.getPlayer();
        String playerName = player.getName();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
        ItemStack offItem = player.getInventory().getItemInOffHand();
        boolean isMainHand = event.getItem().equals(mainItem);
        ////左右键均可部分
        //不满足等级要求的部位强制掉落
        if (wrongLevelLimit(mainPlugin, player, event.getItem().equals(mainItem) ? 4 : 5, event.getItem(), event, true, true)) {
            return;
        }
        //如果按右键
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            //副手BUG修复(主手有技能时取消副手的右键按键效果)
            if (!isMainHand) {
                //获取主手物品的技能词条
                List<String> mainHandNameList = Tools.getItemLoreValueStr(mainPlugin, "消耗", mainItem, " ");
                if (mainHandNameList == null || mainHandNameList.size() == 0) {
                    mainHandNameList = Tools.getItemLoreValueStr(mainPlugin, "技能", mainItem, " ");
                }
                //禁止物品右键事件触发
                if (mainHandNameList != null && mainHandNameList.size() != 0) {
                    event.setCancelled(true);
                }
            }

            //获取物品的消耗品技能词条
            boolean readyConsume = false;
            List<String> skillNameList = Tools.getItemLoreValueStr(mainPlugin, "消耗", event.getItem(), " ");
            if (skillNameList != null && skillNameList.size() > 0) {
                readyConsume = true;
            }
            //获取物品的技能词条
            if (!readyConsume)
                skillNameList = Tools.getItemLoreValueStr(mainPlugin, "技能", event.getItem(), " ");

            //确认物品有技能词条
            if (skillNameList == null || skillNameList.size() == 0)
                return;
            //是否禁止物品右键事件触发
            boolean cancelButton = false;
            //遍历触发每一个技能
            for (String skillName : skillNameList) {
                //确认这个技能存在
                if (mainPlugin.getSkillData().skillExist(skillName)) {
                    //获取技能信息
                    SkillInfo skillInfo = mainPlugin.getSkillData().getSkill(skillName);
                    //确认是否取消按键
                    if (skillInfo.cancelMouseButton)
                        cancelButton = true;
                    //确认技能CD
                    if (skillInfo.getCoolDownInfo(playerName).skillTypeReady()) {
                        //触发技能
                        skillInfo.invoke(playerName, true, (readyConsume ? "Consume" : "Skill"), null);
                        //TODO: 增加字段，触发时是否提醒
                        player.sendMessage("触发" + (readyConsume ? "消耗" : "") + "技能[§e" + skillName + "§r]");
                    } else {
                        //技能在CD
                        if (System.currentTimeMillis() - skillInfo.getCoolDownInfo(playerName).getLastUseTime() >= 10
                                && skillInfo.echoCoolDownLeftInform) {
                            if (skillInfo.getCoolDownInfo(playerName).getWaitTime() == CoolDownInfo.runningSign) {
                                //player.sendMessage("");
                            } else {
                                player.sendMessage("§4在使用" + (readyConsume ? "消耗" : "") + "技能[§e" + skillName + "§r§4]前还需要等待" + skillInfo.getCoolDownInfo(playerName).getWaitTimeStr() + "§4的时间！");
                            }
                        }
                    }
                } else {
                    //技能不存在
                    player.sendMessage("技能[§e" + skillName + "§r]的效果不存在！");
                }
            }
            //设置物品右键事件触发情况
            event.setCancelled(cancelButton);
            //消耗物品
            if (readyConsume) {
                if (event.getItem().equals(offItem)) {
                    offItem.setAmount(offItem.getAmount() - 1);
                } else if (event.getItem().equals(mainItem)) {
                    mainItem.setAmount(mainItem.getAmount() - 1);
                }
            }
            return;
        }
        //如果按左键
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {


        }
    }
    /*
    InventoryClickEvent当玩家点击物品栏中的格子时触发事件事件.
    */
}
