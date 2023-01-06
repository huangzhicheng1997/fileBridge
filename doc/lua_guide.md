# LUA脚本介绍

在/bin/lua下存放了".lua"文件，以及一个module文件夹

- module文件夹用来存放公共库，例如strings.lua,可以被/bin/lua下的.lua文件调用
- 非module文件夹下的.lua文件之间不能互相调用，但是可以去调用module下的公共模块

非module下的脚本文件可以实现两个方法（不需要全部都实现）


1. mappings方法 用来对日志文本进行提取生成kv的结构化数据

```lua
--入参为string类型的日志内容，需要返回kv的table类型（map）
function mappings(logContent)
    local mapping = {};
    mapping["time"] = "2022-01-31";
    mapping["content"] = "content";
    mapping["logLevel"] = "INFO";
    return mapping
end
```
2. handle方法用于对日志数据进行处理，例如脱敏。

```lua
--入参为string类型的日志内容，返回处理后的日志内容要求类型也是string
function handle(logContent)
    return content;
end
```

以上两个方法调用顺序上是先执行handle后执行mappings