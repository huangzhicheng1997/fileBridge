package com.github.fileBridge.common.se;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.script.LuajContext;

/**
 * @author ZhiCheng
 * @date 2022/11/24 17:32
 */
public class JString {

    public static void loadJString(LuajContext context) {
        LuaTable jstring = new LuaTable();
        jstring.set("charAt", new CharAt());
        jstring.set("split", new Split());
        //.....添加其他的java string方法
        context.globals.set("jstring", jstring);
    }

    public static class CharAt extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return LuaValue.valueOf("" + arg1.toString().charAt(arg2.toint()));
        }
    }

    public static class Split extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue s, LuaValue delimiter) {
            String[] strings = s.toString().split(delimiter.toString());
            LuaValue[] luaValues = new LuaValue[strings.length];
            for (int i = 0; i < luaValues.length; i++) {
                luaValues[i] = LuaValue.valueOf(strings[i]);
            }
            return LuaTable.listOf(luaValues);
        }
    }


}
