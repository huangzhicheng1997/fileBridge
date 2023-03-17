package com.github.fileBridge.common.se;

import com.github.fileBridge.common.logger.GlobalLogger;
import com.github.fileBridge.common.utils.FileUtil;
import com.github.fileBridge.common.utils.StringUtils;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.luajc.LuaJC;
import org.luaj.vm2.script.LuaScriptEngine;
import org.luaj.vm2.script.LuaScriptEngineFactory;
import org.luaj.vm2.script.LuajContext;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ZhiCheng
 * @date 2022/11/24 16:45
 */
public class ScriptsAccessor {

    private final File scriptsDir;

    private LuajContext context;

    private Bindings globalBindings;

    private final Map<String, CompiledScript> scripts = new HashMap<>();

    public static ScriptsAccessor newScriptLoader() throws IOException {
        String luaPath = System.getProperty("log.agent.lua.path");
        File file;
        if (StringUtils.isBlank(luaPath)) {
            File dir = FileUtil.newFile(System.getProperty("user.dir"));
            file = FileUtil.newFile(dir.getParentFile().getAbsolutePath(), "lua");
        } else {
            file = new File(luaPath);
        }
        if (!file.exists() || file.isFile()) {
            throw new FileNotFoundException(file.getPath());
        }
        return new ScriptsAccessor(file.getAbsolutePath());

    }

    public ScriptsAccessor(String baseDir) {
        scriptsDir = new File(baseDir);
        if (scriptsDir.isFile() || !scriptsDir.exists()) {
            GlobalLogger.getLogger().warn("script load failed,because file is not exist or file is not a dir,basedir is" + baseDir);
        }
        load();
    }

    private void load() {
        //bindings
        LuaScriptEngine luaScriptEngine = (LuaScriptEngine) new LuaScriptEngineFactory().getScriptEngine();
        context = (LuajContext) luaScriptEngine.getContext();
        globalBindings = luaScriptEngine.createBindings();

        loadLua(scriptsDir, luaScriptEngine, null);
        loadJLib();
        loadLuaLib(luaScriptEngine);
    }

    private void loadLuaLib(LuaScriptEngine luaScriptEngine) {
        File[] libs = scriptsDir.listFiles(f -> f.isDirectory() && f.getName().equals("module"));
        if (libs == null) {
            return;
        }
        loadLua(libs[0], luaScriptEngine, globalBindings);
    }

    private void loadLua(File scriptsDir, LuaScriptEngine luaScriptEngine, Bindings bindings) {
        File[] files = scriptsDir.listFiles((dir, name) -> name.endsWith(".lua"));
        if (files == null || files.length == 0) {
            return;
        }
        Arrays.stream(files).forEach(f -> {
            try {
                CompiledScript compile = luaScriptEngine.compile(new FileReader(f));
                if (bindings != null) {
                    compile.eval(bindings);
                }
                scripts.putIfAbsent(f.getName(), compile);
            } catch (ScriptException | FileNotFoundException ignored) {

            }
        });
    }

    public CompiledScript getScript(String name) {
        return scripts.get(name);
    }


    private void loadJLib() {
        JString.loadJString(context);
        //............加载其他的java库
    }

    public ScriptVisitor newVisitor(String scriptName) {
        Bindings bindings = new SimpleBindings();
        bindings.putAll(globalBindings);
        return new ScriptVisitor(bindings, scripts.get(scriptName));
    }

    public LuaFunctionVisitor newLuaFunctionVisitor(String scriptName, String func) {
        ScriptVisitor scriptVisitor = this.newVisitor(scriptName);
        return new LuaFunctionVisitor(scriptVisitor.luaFunction(func));
    }

    public static class ScriptVisitor {

        private final Bindings bindings;

        public ScriptVisitor(Bindings bindings, CompiledScript compiledScript) {
            Objects.requireNonNull(compiledScript);
            this.bindings = bindings;
            try {
                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }

        public LuaFunction luaFunction(String funcN) {
            return (LuaFunction) bindings.get(funcN);
        }

        public LuaValue luaValue(String value) {
            return (LuaValue) bindings.get(value);
        }

    }

    public static class LuaFunctionVisitor {

        private final LuaFunction luaFunction;

        public LuaFunctionVisitor(LuaFunction luaFunction) {
            this.luaFunction = luaFunction;
        }

        public LuaValue invokeOneResultFunc(LuaValue[] args) {
            Varargs ret;
            if (args == null || args.length == 0) {
                ret = luaFunction.invoke();
            } else {
                ret = luaFunction.invoke(args);
            }
            return ret.arg1();
        }
    }
}
