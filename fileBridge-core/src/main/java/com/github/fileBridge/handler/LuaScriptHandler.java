package com.github.fileBridge.handler;

import com.github.fileBridge.common.config.OutputYml;
import com.github.fileBridge.common.logger.GlobalLogger;
import com.github.fileBridge.common.Event;
import com.github.fileBridge.common.utils.StringUtils;
import com.github.fileBridge.event.EventHandlerPipeline;
import com.github.fileBridge.common.se.LuaValueSerializer;
import com.github.fileBridge.common.se.ScriptsAccessor;
import org.luaj.vm2.LuaValue;

import java.util.Map;

/**
 * @author ZhiCheng
 * @date 2022/11/25 15:19
 */
public class LuaScriptHandler implements EventHandler {

    private ScriptsAccessor.LuaFunctionVisitor mappingsFuncVisitor;
    private ScriptsAccessor.LuaFunctionVisitor handleFuncVisitor;
    private final String luaScriptName;

    public LuaScriptHandler(ScriptsAccessor scriptsAccessor, OutputYml outputYml) {
        this.luaScriptName = outputYml.getScriptName();
        if (StringUtils.isBlank(luaScriptName)) {
            return;
        }
        try {
            this.mappingsFuncVisitor = scriptsAccessor.newLuaFunctionVisitor(luaScriptName, "mappings");
        } catch (Exception e) {
            GlobalLogger.getLogger().warn("load script error,script is " + luaScriptName + ",and function is 'mappings'");
        }
        this.handleFuncVisitor = scriptsAccessor.newLuaFunctionVisitor(luaScriptName, "handle");

    }

    @Override
    public void handle(Event event, EventHandlerPipeline pipeline) {
        if (event == null) {
            return;
        }
        try {
            if (StringUtils.isBlank(luaScriptName)) {
                return;
            }
            var content = getContent(event);
            if (null != content) {
                event = new Event(event.absPath(), content, event.mapping(), event.output(), event.offset(), event.id());
            }
            var mappings = getMappings(event);
            if (null != mappings) {
                event.mapping().putAll(mappings);
            }
        } catch (Exception e) {
            GlobalLogger.getLogger().error("run script failed", e);
        } finally {
            pipeline.fireNext(event);
        }
    }

    private Map<String, String> getMappings(Event event) {
        if (mappingsFuncVisitor == null) {
            return null;
        }
        try {
            LuaValue ret = mappingsFuncVisitor.invokeOneResultFunc(new LuaValue[]{LuaValue.valueOf(event.content())});
            if (ret == null) {
                return null;
            }
            return LuaValueSerializer.toStringMap(ret);
        } catch (Exception e) {
            GlobalLogger.getLogger().warn("load script error,script is " + luaScriptName + ",and function is 'mappings'");
        }
        return null;
    }

    private String getContent(Event event) {
        if (this.handleFuncVisitor == null) {
            return null;
        }
        try {
            LuaValue ret = handleFuncVisitor.invokeOneResultFunc(new LuaValue[]{LuaValue.valueOf(event.content())});
            if (null == ret) {
                return null;
            }
            return LuaValueSerializer.toString(ret);
        } catch (Exception e) {
            GlobalLogger.getLogger().warn("load script error,script is " + luaScriptName + ",and function is 'handle'");
        }
        return null;
    }
}
