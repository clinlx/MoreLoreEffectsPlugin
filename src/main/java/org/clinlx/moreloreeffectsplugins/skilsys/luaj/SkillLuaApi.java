package org.clinlx.moreloreeffectsplugins.skilsys.luaj;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;

public class SkillLuaApi {
    public static boolean skillThreadAlive(long id) {
        SkillThread skillThread = SkillThread.getSkillThread(id);
        return skillThread != null && !skillThread.needStop;
    }

    public static void ensureThreadAlive(long id) throws LuaStopException {
        if (!skillThreadAlive(id))
            throw new LuaStopException("Lua Stop");
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

        //TODO:阻塞直到获取返回值
        public void Command(String cmd) throws LuaStopException {
            ensureThreadAlive(id);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!skillThreadAlive(id)) return;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }.runTask(MoreLoreEffectsPlugin.getInstance());
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

        //TODO:修改任意技能类型的CD
        public void SetCoolDown(int value) throws LuaStopException {
            ensureThreadAlive(id);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!skillThreadAlive(id)) return;
                    SkillThread skillThread = SkillThread.getSkillThread(id);
                    SkillInfo skillInfo = skillThread.skillInfo;
                    if (skillThread.changeCoolDown) {
                        skillInfo.getCoolDownInfo(player.getName()).useSkillTypeCoolDown(value);
                    }
                }
            }.runTask(MoreLoreEffectsPlugin.getInstance());
            //MoreLoreEffectsPlugin.getInstance().getSkillLuaEventMng().addEvent("setCoolDownTime", String.valueOf(value));
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
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!skillThreadAlive(id)) return;
                    Bukkit.dispatchCommand(player, cmd);
                }
            }.runTask(MoreLoreEffectsPlugin.getInstance());
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
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!skillThreadAlive(id)) return;
                    player.setHealth(value);
                }
            }.runTask(MoreLoreEffectsPlugin.getInstance());
        }

        //TODO:更多API，如修改属性
    }

    //TODO:UnsafeApi 回调
    public static class Unsafe {
        private final long id;
        private final String userName;

        public Unsafe(String userName, long id) {
            this.id = id;
            this.userName = userName;
        }

        public void RunTask(Runnable runnable) throws LuaStopException {
            ensureThreadAlive(id);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!skillThreadAlive(id)) return;
                    runnable.run();
                }
            }.runTask(MoreLoreEffectsPlugin.getInstance());
        }
    }
    //TODO:ItemApi
}