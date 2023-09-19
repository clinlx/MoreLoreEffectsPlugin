package org.clinlx.moreloreeffectsplugins.skilsys.luaj;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillInfo;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.concurrent.Callable;

public class SkillLuaApi {
    public static class WorldPos {
        public static Location getBukkitLocation(WorldPos worldPos) {
            World world = Bukkit.getWorld(worldPos.worldName);
            if (world == null) return null;
            return new Location(world, worldPos.x, worldPos.y, worldPos.z, worldPos.yaw, worldPos.pitch);
        }

        public WorldPos(WorldPos source) {
            this.worldName = source.worldName;
            this.x = source.x;
            this.y = source.y;
            this.z = source.z;
            this.pitch = source.pitch;
            this.yaw = source.yaw;
        }

        public WorldPos(Location location) {
            this.worldName = location.getWorld().getName();
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.pitch = location.getPitch();
            this.yaw = location.getYaw();
        }

        public WorldPos(String worldName, double x, double y, double z) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            pitch = 0;
            yaw = 0;
        }

        public WorldPos(String worldName, double x, double y, double z, float pitch, float yaw) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public WorldPos getHeadForwardPos(double distance) {
            double radPitch = Math.toRadians(pitch);
            double radYaw = Math.toRadians(yaw);
            double cosPitch = Math.cos(radPitch);
            double sinPitch = Math.sin(radPitch);
            double cosYaw = Math.cos(radYaw);
            double sinYaw = Math.sin(radYaw);
            double dx = -sinYaw * cosPitch * distance;
            double dy = -sinPitch * distance;
            double dz = cosYaw * cosPitch * distance;
            return new WorldPos(worldName, x + dx, y + dy, z + dz, pitch, yaw);
        }

        public WorldPos getHeadForwardPosBlocked(double distance) {
            //循环，每次递进，直到遇见方块
            WorldPos pos = new WorldPos(this);
            double step = 0.25;
            while (distance > 0) {
                WorldPos nextPos = pos.getHeadForwardPos(step);
                Location nextLoc = WorldPos.getBukkitLocation(nextPos);
                if (nextLoc == null) return null;
                if (nextLoc.getBlock().getType().isSolid()) {
                    return pos;
                }
                pos = nextPos;
                distance -= step;
            }
            return pos;
        }

        public WorldPos localPosToWorld(WorldPos localPos) {
            double x = this.x + localPos.x * Math.cos(Math.toRadians(this.yaw)) - localPos.z * Math.sin(Math.toRadians(this.yaw));
            double z = this.z + localPos.x * Math.sin(Math.toRadians(this.yaw)) + localPos.z * Math.cos(Math.toRadians(this.yaw));
            return new WorldPos(worldName, x, y + localPos.y, z, pitch + localPos.pitch, yaw + localPos.yaw);
        }

        public String worldName;
        public double x, y, z;
        public float pitch, yaw;
    }

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
                try {
                    runnable.run();
                } catch (Exception e) {
                    SkillThread.HandlerLuaException(e, SkillThread.getSkillThread(id));
                }
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
            if (!skillThreadAlive(id))
                throw new LuaStopException("Lua Stop");
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

        //构造WorldPos
        public WorldPos buildWorldPos(WorldPos worldPos) {
            return new WorldPos(worldPos);
        }

        public WorldPos buildWorldPos(Location location) {
            return new WorldPos(location);
        }

        public WorldPos buildWorldPos(String worldName, double x, double y, double z) {
            return new WorldPos(worldName, x, y, z);
        }

        public WorldPos buildWorldPos(String worldName, double x, double y, double z, float pitch, float yaw) {
            return new WorldPos(worldName, x, y, z, pitch, yaw);
        }

        //播放粒子效果
        public void DrawParticle(String particleName, int p_num, WorldPos worldPos, double r_x, double r_y, double r_z) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                World world = Bukkit.getWorld(worldPos.worldName);
                Location pos = WorldPos.getBukkitLocation(worldPos);
                if (world == null || pos == null) {
                    MoreLoreEffectsPlugin.getInstance().getLogger().info("DrawParticle: world or pos is null");
                    return;
                }
                world.spawnParticle(Particle.valueOf(particleName), pos, p_num, r_x, r_y, r_z);
            }, id);
        }
    }

    public static class SkillApi {
        private final long id;
        private final Player player;

        public SkillApi(String userName, long id) {
            this.id = id;
            player = Bukkit.getPlayer(userName);
        }

        //获取技能默认CD
        public long getDefCd() throws LuaStopException {
            ensureThreadAlive(id);
            SkillThread skillThread = SkillThread.getSkillThread(id);
            SkillInfo skillInfo = skillThread.skillInfo;
            return skillInfo.skillCoolDown;
        }

        //设置当前技能CD
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
        //TODO:使用另一个Lua技能(多线程的替代品)
    }

    public static class PlayerApi {
        private final long id;
        private final Player player;

        public PlayerApi(String userName, long id) {
            this.id = id;
            player = Bukkit.getPlayer(userName);
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

        //阻塞直到获取返回值
        public boolean CommandWithRes(String cmd) throws LuaStopException {
            ensureThreadAlive(id);
            try {
                return callInBukkitBlock(() -> {
                    return Bukkit.dispatchCommand(player, cmd);
                }, id);
            } catch (Exception ignored) {
                return false;
            }
        }

        public void Teleport(WorldPos worldPos) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                Location pos = WorldPos.getBukkitLocation(worldPos);
                if (pos == null) return;
                player.teleport(pos);
            }, id);
        }

        public void Teleport(double x, double y, double z) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                player.teleport(new Location(player.getWorld(), x, y, z));
            }, id);
        }

        //获取玩家名
        public String getName() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getName();
        }

        public String getWorldName() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getWorld().getName();
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

        public WorldPos getWorldPos() throws LuaStopException {
            ensureThreadAlive(id);
            Location location = player.getLocation();
            return new WorldPos(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        }

        public double getMaxHealth() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        }


        public double getHealth() throws LuaStopException {
            ensureThreadAlive(id);
            return player.getHealth();
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
        public final UnsafeApi api;

        public final LuaValue java;

        public UnsafeArea(String userName, long id, LuaValue javaApi) {
            this.id = id;
            this.userName = userName;
            this.java = javaApi;
            this.api = new UnsafeApi(userName, id, java);
        }

        public LuaValue getUnsafeLuaJ() throws LuaStopException {
            ensureThreadAlive(id);
            return java;
        }

        public void RunTask(LuaValue luaValue) throws LuaStopException {
            ensureThreadAlive(id);
            callInBukkit(() -> {
                luaValue.call(CoerceJavaToLua.coerce(api));
            }, id);
        }

        public LuaValue RunTaskWithRes(LuaValue luaValue) throws Exception {
            ensureThreadAlive(id);
            return callInBukkitBlock(() -> {
                return luaValue.call(CoerceJavaToLua.coerce(api));
            }, id);
        }
    }

    public static class UnsafeApi {
        private final long id;
        private final String userName;
        private final Player player;

        public final LuaValue java;

        public UnsafeApi(String userName, long id, LuaValue java) {
            this.id = id;
            this.userName = userName;
            this.player = Bukkit.getPlayer(userName);
            this.java = java;
        }

        public Player getPlayer() throws LuaStopException {
            ensureThreadAlive(id);
            return player;
        }
    }
    //TODO:ItemApi
}