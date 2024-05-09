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
        luaHead =
                "CALLER_NAME = select(1, ...);\n" +
                        "CALL_BODY_ID = select(2, ...);\n" +
                        "Player = luajava.newInstance(\"" + apiClassName + "$PlayerApi\",CALLER_NAME,CALL_BODY_ID);\n" +
                        "Skill = luajava.newInstance(\"" + apiClassName + "$SkillApi\",CALLER_NAME,CALL_BODY_ID);\n" +
                        "Server = luajava.newInstance(\"" + apiClassName + "$ServerApi\",CALL_BODY_ID);\n" +
                        "Unsafe = luajava.newInstance(\"" + apiClassName + "$UnsafeArea\",CALLER_NAME,CALL_BODY_ID,luajava);\n" +
                        "CALLER_NAME = nil;CALL_BODY_ID = nil;luajava = nil;Args = select(3, ...);\n";//luajava.bindClass
        preProcess = "";
        codeBody = "";
        finalProcess = "";
    }

    public boolean LoadFromLuaFile(String luaPath) {
        StringBuilder preProcess = new StringBuilder();
        StringBuilder luaBody = new StringBuilder();
        StringBuilder finalProcess = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(luaPath));
            List<String> stringList = new ArrayList<>();
            String line;
            int processState = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("--------EndPreProcess--------")) {
                    if (processState != 0)
                        throw new IOException("EndPreProcess Error");
                    processState = 1;
                }
                if (line.trim().equals("--------FinalProcess--------")) {
                    if (processState != 1)
                        throw new IOException("FinalProcess Error");
                    processState = 2;
                }
                if (processState == 0) {
                    preProcess.append(line).append("\n");
                    luaBody.append("\n");
                    finalProcess.append("\n");
                } else if (processState == 1) {
                    luaBody.append(line).append("\n");
                    finalProcess.append("\n");
                } else if (processState == 2) {
                    finalProcess.append(line).append("\n");
                }
            }
            reader.close();
            if (processState == 0) {
                luaBody = preProcess;
                preProcess = new StringBuilder();
                finalProcess = new StringBuilder();
            }
            this.preProcess = preProcess.toString();
            this.codeBody = luaBody.toString();
            this.finalProcess = finalProcess.toString();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public final String luaHead;
    private String preProcess;

    public String getPreProcess() {
        return preProcess;
    }

    private String codeBody;

    public String getCodeBody() {
        return codeBody;
    }

    private String finalProcess;

    public String getFinalProcess() {
        return finalProcess;
    }
}
