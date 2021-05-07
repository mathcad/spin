local function getChildren(parentCode)
    local res = {}
    if KEYS[1] == nil or parentCode == nil then
        return res
    end

    local nodeStr = redis.call("HGET", KEYS[1], parentCode)
    if nodeStr then
        local node = cjson.decode(nodeStr)
        if node and node.children then
            local codes = node.children
            return redis.call("HMGET", KEYS[1], unpack(codes))
        end
    end
    return res
end

return getChildren(ARGV[1])
