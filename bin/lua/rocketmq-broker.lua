mappingNames = { "level", "tid" }

-- 2020-12-20 18:03:02 INFO BrokerControllerScheduledThread1 - Slave fall behind master: 20339 bytes

function mappings(logContent)
    local mapping = {}
    local stringbuffer = ''
    local nextName = 1
    local contentStartIdx
    for i = findTime(logContent, mapping), string.len(logContent) do
        if nextName > #mappingNames then
            contentStartIdx = i;
            break
        end
        local ch = strings.charAt(logContent, i)
        if ch ~= '-' then
            if ch == ' ' then
                mapping[mappingNames[nextName]] = stringbuffer
                nextName = nextName + 1
                stringbuffer = ''
            else
                stringbuffer = stringbuffer .. ch
            end
        end
        contentStartIdx = i;
    end
    mapping["content"] = string.sub(logContent, contentStartIdx + 2)
    return mapping
end

function findTime(logContent, mapping)
    local patternTimes = 0
    local time = ''
    for i = 1, string.len(logContent) do
        local ch = strings.charAt(logContent, i)
        if ch == ' ' then
            patternTimes = patternTimes + 1
            if patternTimes == 2 then
                mapping["time"] = time
                return i + 1
            end
            time = time .. ch
        else
            time = time .. ch
        end
    end
    return -1
end

function handle(content)
    return content;
end