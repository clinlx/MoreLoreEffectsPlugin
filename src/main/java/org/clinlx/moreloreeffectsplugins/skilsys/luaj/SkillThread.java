package org.clinlx.moreloreeffectsplugins.skilsys.luaj;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.CoolDownInfo;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SkillThread extends LuaThread {
    private static final HashMap<Long, SkillThread> skillThreadMap = new HashMap<>();
    private static final HashMap<String, List<Long>> runningThread = new HashMap<>();

    public static long getThreadNum() {
        synchronized (skillThreadMap) {
            return skillThreadMap.size();
        }
    }

    public static void putSkillThread(long id, SkillThread thread) {
        synchronized (skillThreadMap) {
            if (!runningThread.containsKey(thread.playerName))
                runningThread.put(thread.playerName, new ArrayList<>());
            runningThread.get(thread.playerName).add(id);
            skillThreadMap.put(id, thread);
        }
    }

    public static void removeSkillThread(long id) {
        synchronized (skillThreadMap) {
            SkillThread thread = skillThreadMap.get(id);
            if (thread == null) return;
            if (runningThread.containsKey(thread.playerName))
                runningThread.get(thread.playerName).remove(id);
            skillThreadMap.remove(id);
        }
    }

    public static SkillThread getSkillThread(long id) {
        synchronized (skillThreadMap) {
            return skillThreadMap.get(id);
        }
    }

    public static long[] getPlayerSkillThreads(String playerName) {
        synchronized (skillThreadMap) {
            if (!runningThread.containsKey(playerName)) return new long[0];
            List<Long> list = runningThread.get(playerName);
            return list.stream().mapToLong(Long::longValue).toArray();
        }
    }

    public final String playerName;
    public final Player player;
    public final ItemStack item;
    public final SkillInfo skillInfo;
    public final boolean changeCoolDown;
    public volatile boolean inPreProcess = true;
    public volatile Object returnObj = null;
    public volatile int hasReturn = 0;

    private SkillThread(String userName, ItemStack item, SkillInfo skillInfo, boolean changeCoolDown) {
        super(skillInfo.skillCode.luaHead, skillInfo.skillCode.preProcess, skillInfo.skillCode.codeBody);
        //设置
        this.playerName = userName;
        this.player = Bukkit.getPlayer(userName);
        this.item = item;
        this.skillInfo = skillInfo;
        this.changeCoolDown = changeCoolDown;
        //通过Globals加载luaHead
        luaHeadChunk.call(LuaValue.valueOf(userName), LuaValue.valueOf(getId()));
    }

    public static long UseLuaSkill(String userName, ItemStack item, SkillInfo skillInfo, boolean changeCoolDown) {
        if (userName == null || item == null || skillInfo == null || skillInfo.skillCode == null) {
            printLog("UseLuaSkill参数错误");
            return -1;
        }

        MoreLoreEffectsPlugin.getInstance().getLogger().info("玩家'" + userName + "'使用了技能[" + skillInfo.skillName + "]");

        //创建线程,技能初始化
        SkillThread thread;
        try {
            thread = new SkillThread(userName, item, skillInfo, changeCoolDown);
        } catch (Exception e) {
            printLog(e.getMessage());
            Player player = Bukkit.getPlayer(userName);
            if (player.isOnline()) {
                if (player.isOp())
                    player.sendMessage(e.getMessage());
                player.sendMessage("§c§l[§4§lMoreLoreEffects§c§l]§r§c§l技能存在语法错误，请联系管理员！");
            }
            return -1;
        }

        putSkillThread(thread.getId(), thread);

        //设置冷却
        if (changeCoolDown)
            skillInfo.getCoolDownInfo(userName).useSkillTypeCoolDown(CoolDownInfo.runningSign);

        //运行预处理部分
        if (!skillInfo.skillCode.preProcess.trim().isEmpty()) {
            try {
                thread.preProcessChunk.call();
            } catch (Exception e) {
                removeSkillThread(thread.getId());
                printLog(e.getMessage());
                //for (StackTraceElement i : e.getStackTrace())
                //    printLog(i.toString());
                Player player = Bukkit.getPlayer(userName);
                if (player.isOnline()) {
                    if (player.isOp())
                        player.sendMessage(e.getMessage());
                    player.sendMessage("§c§l[§4§lMoreLoreEffects§c§l]§r§c§l技能预处理出错，请联系管理员！");
                }
                //设置冷却
                if (changeCoolDown)
                    skillInfo.getCoolDownInfo(userName).setSkillTypeCoolDownTimeLen(0);
                return -1;
            }
        }
        thread.inPreProcess = false;

        new BukkitRunnable() {
            @Override
            public void run() {
                //启动线程
                thread.start();
            }
        }.runTask(MoreLoreEffectsPlugin.getInstance());

        return thread.getId();
    }

    public static void FinishLuaSKill(long id) {
        SkillThread thread = getSkillThread(id);
        if (thread == null) {
            printLog("FinishLuaSKill参数错误");
            return;
        }
        thread.needStop = true;
    }

    public static void HandlerLuaException(Exception e, SkillThread skillThread) {
        if (e.getCause() != null && e.getCause().toString().trim().equals("org.clinlx.moreloreeffectsplugins.skilsys.luaj.LuaStopException: Lua Stop")) {
            printLog("玩家‘" + skillThread.playerName + "’死亡或离开，技能[" + skillThread.skillInfo.skillName + "]效果中止");
            if (skillThread.player.isOnline()) {
                skillThread.player.sendMessage("§c§l技能[§r§e" + skillThread.skillInfo.skillName + "§c§l]效果中止");
            }
        } else {
            printLog(e.getMessage());
            printLog("玩家‘" + skillThread.playerName + "’使用技能[" + skillThread.skillInfo.skillName + "]时异常");
            //for (StackTraceElement i : e.getStackTrace())
            //    printLog(i.toString());
            if (skillThread.player.isOnline()) {
                if (skillThread.player.isOp())
                    skillThread.player.sendMessage(e.getMessage());
                skillThread.player.sendMessage("§c§l[§4§lMoreLoreEffects§c§l]§r§c§l技能运行出错，请联系管理员！");
            }

        }
    }

    @Override
    protected void StartLua() {
        try {
            luaContentChunk.call();
        } catch (Exception e) {
            HandlerLuaException(e, this);
        } finally {
            CoolDownInfo coolDownInfo = this.skillInfo.getCoolDownInfo(this.playerName);
            if (coolDownInfo != null && this.changeCoolDown) {
                if (coolDownInfo.getWaitTime() == CoolDownInfo.runningSign) {
                    coolDownInfo.useSkillTypeCoolDown(this.skillInfo.skillCoolDown);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    removeSkillThread(getId());
                }
            }.runTask(MoreLoreEffectsPlugin.getInstance());
        }
    }
}
