
local mappingNames = { "time", "app", "pod", "reqUri", "traceId", "spanId", "reqTime", "tid", "level", "logger", "content" }

function mappings(logContent)
    local mappings = {}
    local mappingTimes = 0
    local i = 1
    local next = 1;
    while i <= string.len(logContent) do
        local mapping = ''
        if strings.charAt(logContent, i) == '[' then
            --查找结束符']'
            for j = i + 1, string.len(logContent) do
                --读到']'表示字段解析结束了
                local char = strings.charAt(logContent, j)
                if char == ']' then
                    mappings[mappingNames[next]] = mapping
                    mappingTimes = mappingTimes + 1
                    next = next + 1
                    i = j;
                    break
                end
                mapping = mapping .. char;
            end
            --匹配10个字段后直接跳出
            if mappingTimes >= #mappingNames - 1 then
                break
            end
        end
        i = i + 1
    end

    mappings[mappingNames[next]] = string.sub(logContent, i + 1)
    return mappings
end