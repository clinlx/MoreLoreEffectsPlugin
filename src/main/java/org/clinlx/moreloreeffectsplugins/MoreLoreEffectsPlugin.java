package org.clinlx.moreloreeffectsplugins;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.clinlx.moreloreeffectsplugins.commands.*;
import org.clinlx.moreloreeffectsplugins.skilsys.SkillData;
import org.clinlx.moreloreeffectsplugins.skilsys.luaj.JsePlatformCopy;
import org.clinlx.moreloreeffectsplugins.skilsys.luaj.SkillThread;

import java.util.Random;

public final class MoreLoreEffectsPlugin extends JavaPlugin {
    private static MoreLoreEffectsPlugin instance;

    public static MoreLoreEffectsPlugin getInstance() {
        return instance;
    }

    public Random random;
    private double jmpAtkDmgAddPrg = 0.05;//Def: 0.5

    public double getJmpAtkDmgMut() {
        return jmpAtkDmgAddPrg > 0 ? jmpAtkDmgAddPrg + 1 : 1.5;
    }

    @Override
    public void onEnable() {
        instance = this;
        //初始化
        random = new Random();
        skillData = new SkillData(this);
        //Commands
        new CheckLoreSkillCommand(this).regTo("checkloreskill");
        new ListLoreCommand(this).regTo("listlore");
        new ListLoreSkillCommand(this).regTo("listloreskill");
        new SetLoreSkillCommand(this).regTo("setloreskill");
        new DelLoreSkillCommand(this).regTo("delloreskill");
        new LoreSkillFileCommand(this).regTo("loreskillfile");
        new LoreManageCommand(this).regTo("loremanage");
        new ReLoadLoreSkillCommand(this).regTo("reloadloreskill");
        new ResetLoreCoolDownCommand(this).regTo("resetlorecooldown");
        //功能
        playerState = new PlayerEffectsState(this);
        EventsListener damageEventListener = new EventsListener(this);
        Bukkit.getPluginManager().registerEvents(damageEventListener, this);
        printLog("更多Lore词条插件启动，版本：" + getDescription().getVersion());
        JsePlatformCopy.getGlobals().load("test = nil").call();
        SkillThread.getThreadNum();
        printLog("更多Lore词条Lua运行时初始化完成。");
    }

    @Override
    public void onDisable() {
    }

    public void printLog(String message) {
        getLogger().info(message);
    }

    private SkillData skillData;

    public SkillData getSkillData() {
        return skillData;
    }

    private PlayerEffectsState playerState;

    public PlayerEffectsState getPlayerState() {
        return playerState;
    }
}
