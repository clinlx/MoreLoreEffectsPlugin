package org.clinlx.moreloreeffectsplugins.skilsys;


import com.google.gson.annotations.Expose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.clinlx.moreloreeffectsplugins.skilsys.luaj.SkillThread;
import org.luaj.vm2.LuaTable;

public class SkillInfo {
    public static void setName(SkillInfo skillInfo, String name) {
        skillInfo.skillName = name;
    }

    public SkillInfo(String skillName, String skillType) {
        this.skillName = skillName;
        this.skillType = skillType;
        skillInformation = "一个叫做[" + skillName + "]的’" + skillType + "‘类技能";
    }

    public SkillInfo(String skillName, String skillType, long skillCoolDown, String skillEffect) {
        this(skillName, skillType);
        this.skillCoolDown = skillCoolDown;
        this.skillEffect = skillEffect;
    }

    public SkillInfo(String skillName, String skillType, SkillCode skillCode) {
        this(skillName, skillType);
        this.skillCode = skillCode;
    }

    public SkillInfo(String skillName, String skillType, long skillCoolDown, String skillEffect, SkillCode skillCode) {
        this(skillName, skillType);
        this.skillCoolDown = skillCoolDown;
        this.skillEffect = skillEffect;
        this.skillCode = skillCode;
    }

    public void invoke(String playerName, boolean changeCoolDown, String invokeMode, LuaTable args) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return;
        if (!player.isOnline()) return;
        if (adminOnly && !player.isOp()) {
            player.sendMessage("§c§l你没有权限使用这个技能!");
            return;
        }
        if (skillCode != null) {
            // 确保存在参数
            if (args == null) args = new LuaTable();
            args.set("InvokeMode", invokeMode);
            // 执行Lua代码
            SkillThread.UseLuaSkill(playerName, player.getInventory().getItemInMainHand(), this, changeCoolDown, args);
        } else {
            if (skillEffect == null) {
                player.sendMessage("§c§l技能效果为空!");
                return;
            }
            // 原始命令
            String cmd = this.skillEffect;
            //§s 替换为使用者名字
            cmd = cmd.replace("§s", playerName);
            // 获取玩家位置
            Location location = player.getLocation();
            //§x、§y、§z 替换为使用者的坐标
            cmd = cmd.replace("§x", String.valueOf(location.getX()));
            cmd = cmd.replace("§y", String.valueOf(location.getY()));
            cmd = cmd.replace("§z", String.valueOf(location.getZ()));
            // 执行命令
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            // 在目标位置播放粒子效果
            location.setY(location.getY() + 1.5);
            player
                    .getWorld()
                    .spawnParticle(Particle.CRIT_MAGIC, location, 25, 0.5, 0.5, 0.5);
            //进入CD
            if (changeCoolDown)
                getCoolDownInfo(playerName).useSkillTypeCoolDown(skillCoolDown);
        }
    }

    public boolean tryUseOnceBy(String playerName) {
        if (getCoolDownInfo(playerName).skillTypeReady()) {
            invoke(playerName, true, "Debug", null);
            return true;
        }
        return false;
    }

    public CoolDownInfo getCoolDownInfo(String playerName) {
        return MoreLoreEffectsPlugin.getInstance().getSkillData().getCoolDownSys(playerName).getTypeCoolDownInfo(skillType);
    }

    public static void printLog(String message) {
        MoreLoreEffectsPlugin.getInstance().getLogger().info(message);
    }

    public String skillName;
    @Expose
    public String skillType = "default";
    @Expose
    public long skillCoolDown = -1;//-1为无限长
    @Expose
    public String skillInformation = null;
    @Expose
    public boolean echoCoolDownLeftInform = true;
    @Expose
    public boolean adminOnly = false;
    @Expose
    public boolean cancelMouseButton = true;
    @Expose
    public String skillEffect = null;
    public SkillCode skillCode = null;

}
