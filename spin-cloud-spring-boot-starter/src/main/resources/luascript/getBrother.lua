local function getBrother(rootKey, keys)
    local res = {}
    if rootKey == nil or keys == nil or table.getn(keys) == 0 then
        return res
    end

    for _, val in pairs(keys) do
        local nodeStr = redis.call("HGET", rootKey, val)
        if nodeStr then
            local node = cjson.decode(nodeStr)
            if node and node.parent then
                nodeStr = redis.call("HGET", rootKey, node.parent)
                if nodeStr then
                    node = cjson.decode(nodeStr)
                    if node and node.children then
                        local brother = redis.call("HMGET", rootKey, unpack(node.children))
                        for _, b in pairs(brother) do
                            table.insert(res, b)
                        end
                    end
                end
            end
        end
    end

    return res
end

return getBrother(KEYS[1], ARGV)
