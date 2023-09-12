package org.clinlx.moreloreeffectsplugins.skilsys.luaj;

import org.luaj.vm2.lib.jse.LuajavaLib;

public class LuajavaLibCopy extends LuajavaLib {
    public LuajavaLibCopy() {
    }

    @Override
    protected Class classForName(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }
}