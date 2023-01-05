strings = {}

--按下标访问字符
function strings.charAt(s, i)
    return string.sub(s, i, i);
end

function strings.split(s, delimiter)
    return jstring.split(s, delimiter)
end

return strings;

