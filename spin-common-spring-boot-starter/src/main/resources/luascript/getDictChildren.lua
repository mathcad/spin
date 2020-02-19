local function getChildren(rootKey, parentCode)
    local res = {}
    if rootKey == nil or parentCode == nil then
        return res
    end

    local nodeStr = redis.call("HGET", rootKey, parentCode)
    if nodeStr then
        local node = cjson.decode(nodeStr)
        if node and node.children then
            local codes = node.children
            return redis.call("HMGET", rootKey, unpack(codes))
        end
    end
    return res
end

return getChildren(KEYS[1], ARGV[1])
