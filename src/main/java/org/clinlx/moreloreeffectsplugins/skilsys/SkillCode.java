package org.clinlx.moreloreeffectsplugins.skilsys;

import org.clinlx.moreloreeffectsplugins.skilsys.luaj.SkillLuaApi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SkillCode {
    public SkillCode() {
        String apiClassName = SkillLuaApi.class.getName();
        //TODO:FinalProcess,用于实现Unsafe被线程强行Stop后的安全处理
        luaHead =
                "CALLER_NAME = select(1, ...);\n" +
                        "CALL_BODY_ID = select(2, ...);\n" +
                        "Player = luajava.newInstance(\"" + apiClassName + "$PlayerApi\",CALLER_NAME,CALL_BODY_ID);\n" +
                        "Skill = luajava.newInstance(\"" + apiClassName + "$SkillApi\",CALLER_NAME,CALL_BODY_ID);\n" +
                        "Server = luajava.newInstance(\"" + apiClassName + "$ServerApi\",CALL_BODY_ID);\n";//luajava.bindClass
        preProcess = "";
        codeBody = "";
    }

    public boolean LoadFromLuaFile(String luaPath) {
        StringBuilder preProcess = new StringBuilder();
        StringBuilder luaBody = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(luaPath));
            List<String> stringList = new ArrayList<>();
            String line;
            boolean isPreProcess = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("--------EndPreProcess--------")) {
                    isPreProcess = false;
                    luaBody.append("\n");
                    continue;
                }
                if (isPreProcess) {
                    preProcess.append(line).append("\n");
                    luaBody.append("\n");
                } else {
                    luaBody.append(line).append("\n");
                }
            }
            if (isPreProcess) {
                StringBuilder temp = luaBody;
                luaBody = preProcess;
                preProcess = temp;
            }
            reader.close();
            this.preProcess = preProcess.toString();
            this.codeBody = luaBody.toString();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public final String luaHead;
    public String preProcess;
    public String codeBody;
}
