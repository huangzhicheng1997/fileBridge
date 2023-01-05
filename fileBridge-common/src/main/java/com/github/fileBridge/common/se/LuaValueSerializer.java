package com.github.fileBridge.common.se;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhiCheng
 * @date 2022/11/25 17:04
 */
public class LuaValueSerializer {

    public static Map<String, String> toStringMap(LuaValue luaValue) {
        Map<String, String> map = new HashMap<>();
        if (luaValue.istable()) {
            LuaTable table = luaValue.checktable();
            for (LuaValue key : table.keys()) {
                LuaValue value = table.get(key);
                if (value.isstring() && key.isstring()) {
                    map.put(key.checkstring().tojstring(), value.checkstring().tojstring());
                }
            }
        }
        return map;
    }

    public static String toString(LuaValue luaValue) {
        if (luaValue.isstring()) {
            LuaString luaString = luaValue.checkstring();
            return luaString.tojstring();
        }
        return null;
    }
}
