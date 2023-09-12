package org.clinlx.moreloreeffectsplugins.skilsys.luaj;

import org.clinlx.moreloreeffectsplugins.MoreLoreEffectsPlugin;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

public abstract class LuaThread extends Thread {
    protected final Globals globals;
    protected final LuaValue luaHeadChunk;
    protected final LuaValue preProcessChunk;
    protected final LuaValue luaContentChunk;
    public volatile boolean needStop = false;

    protected LuaThread(String luaHead, String preProcess, String luaContent) {
        //初始化lua运行时环境
        globals = JsePlatformCopy.getGlobals();
        //通过Globals加载luaHead
        luaHeadChunk = globals.load(luaHead);
        //通过Globals加载preProcess
        preProcessChunk = globals.load(preProcess);
        //通过Globals加载lua脚本
        luaContentChunk = globals.load(luaContent);
    }

    @Override
    public void run() {
        StartLua();
    }

    protected void StartLua() {
        try {
            luaContentChunk.call();
        } catch (Exception e) {
            printLog(e.getMessage());
            for (StackTraceElement i : e.getStackTrace())
                printLog(i.toString());
        }
    }

    protected static void printLog(String message) {
        MoreLoreEffectsPlugin.getInstance().getLogger().info(message);
    }
}
