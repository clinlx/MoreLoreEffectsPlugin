package org.clinlx.moreloreeffectsplugins.skilsys.luaj;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.concurrent.Callable;

public class SkillLuaApi {
    public static boolean skillThreadAlive(long id) {
        SkillThread skillThread = SkillThread.getSkillThread(id);
        return skillThread != null && !skillThread.needStop;
    }

    public static void ensureThreadAlive(long id) throws LuaStopException {
        if (!skillThreadAlive(id))
            throw new LuaStopException("Lua Stop");
    }

    public static void callInBukkit(Runnable runnable, long id) {
        SkillThread skillThread = SkillThread.getSkillThread(id);
        if (skillThread.inPreProcess) {
            runnable.run();
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!skillThreadAlive(id)) return;
                runnable.run();
            }
        }.runTask(MoreLoreEffectsPlugin.getInstance());
    }

    public static <T> T callInBukkitBlock(Callable<T> callable, long id) throws Exception {
        SkillThread skillThread = SkillThread.getSkillThread(id);
        if (skillThread.inPreProcess) {
            return callable.call();
        }
        try {
            skillThread.returnObj = null;
            skillThread.hasReturn = 0;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!skillThreadAlive(id)) {
                        skillThread.hasReturn = -1024;
                        return;
                    }
                    try {
                        skillThread.returnObj = callable.call();
                        skillThread.hasReturn = 1;
                    } catch (Exception e) {
                        skillThread.returnObj = e;
                        skillThread.hasReturn = -1;
                    }
                }
            }.runTask(MoreLoreEffectsPlugin.getInstance());
            int hasReturn;
            Object retObj;
            try {
                while (skillThread.hasReturn == 0)
                    Thread.sleep(10);
                hasReturn = skillThread.hasReturn;
                retObj = skillThread.returnObj;
            } catch (Exception ce) {
                throw new LuaStopException("Lua Stop");
            }
            if (hasReturn == 1) {
                return (T) retObj;
            } else if (hasReturn == -1024) {
                throw new LuaStopException("Lua Stop");
            } else {
                throw (Exception) retObj;
            }
        } catch (LuaStopException e) {
            throw e;
        }
    }

    public static class ServerApi {
        private final long id;

        public ServerApi(long id) {
            this.id = id;
        }

        public void Log(String message) throws LuaStopException {
            ensureThreadAlive(id);
            MoreLoreEffectsPlugin.getInstance().getLogger().info(message);
        }

        public void Wait(int time) throws InterruptedException, LuaStopException {
            ensureThreadAlive(id);
            Thread.sleep(time);
        }

        public void Command(String cmd) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }, id);
        }

        //阻塞直到获取返回值
        public boolean CommandWithRes(String cmd) throws LuaStopException {
            ensureThreadAlive(id);
            try {
                return callInBukkitBlock(() -> {
                    return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }, id);
            } catch (Exception ignored) {
                return false;
            }
        }
        //TODO:播放粒子效果
    }

    public static class SkillApi {
        private final long id;
        private final Player player;

        public SkillApi(String userName, long id) {
            this.id = id;
            //获取主手物品
            player = Bukkit.getPlayer(userName);
        }

        public long getDefCd() throws LuaStopException {
            ensureThreadAlive(id);
            SkillThread skillThread = SkillThread.getSkillThread(id);
            SkillInfo skillInfo = skillThread.skillInfo;
            return skillInfo.skillCoolDown;
        }

        public void SetCoolDown(int value) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                SkillThread skillThread = SkillThread.getSkillThread(id);
                SkillInfo skillInfo = skillThread.skillInfo;
                if (skillThread.changeCoolDown) {
                    skillInfo.getCoolDownInfo(player.getName()).useSkillTypeCoolDown(value);
                }
            }, id);
        }

        //修改任意技能类型的CD
        public void SetTypeCoolDown(String type, int value) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                SkillThread skillThread = SkillThread.getSkillThread(id);
                SkillInfo skillInfo = skillThread.skillInfo;
                if (skillThread.changeCoolDown) {
                    MoreLoreEffectsPlugin.getInstance().getSkillData().getCoolDownSys(player.getName()).getTypeCoolDownInfo(type).useSkillTypeCoolDown(value);
                }
            }, id);
        }

        //TODO:随时开关隐藏右键提示冷却等待文本的功能
    }

    public static class PlayerApi {
        private final long id;
        private final Player player;

        public PlayerApi(String userName, long id) {
            this.id = id;
            player = Bukkit.getPlayer(userName);
        }

        public String getName() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getName();
        }

        public double getX() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getLocation().getX();
        }

        public double getY() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getLocation().getY();
        }

        public double getZ() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getLocation().getZ();
        }

        public double getYaw() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getLocation().getYaw();
        }

        public double getPitch() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getLocation().getPitch();
        }

        public void Inform(String message) throws LuaStopException {
            ensureThreadAlive(id);
            player.sendMessage(message);
        }

        public void Command(String cmd) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                Bukkit.dispatchCommand(player, cmd);
            }, id);
        }

        public double getHealth() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getHealth();
        }

        public double getMaxHealth() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        }

        public void setHealth(double value) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                player.setHealth(value);
            }, id);
        }

        //TODO:更多API，如修改属性
    }

    public static class UnsafeArea {
        private final long id;
        private final String userName;

        public UnsafeArea(String userName, long id) {
            this.id = id;
            this.userName = userName;
        }

        public LuaValue RunTask(LuaValue luaValue) throws Exception {
            ensureThreadAlive(id);
            return callInBukkitBlock(() -> {
                return luaValue.call(CoerceJavaToLua.coerce(new UnsafeApi(userName, id)));
            }, id);
        }
    }

    public static class UnsafeApi {
        private final long id;
        private final String userName;
        private final Player player;

        public UnsafeApi(String userName, long id) {
            this.id = id;
            this.userName = userName;
            this.player = Bukkit.getPlayer(userName);
        }

        public Player getPlayer() throws LuaStopException {
            ensureThreadAlive(id);
            return player;
        }
    }
    //TODO:ItemApi
}