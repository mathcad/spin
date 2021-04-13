local depth = 0

local function getAllChild(currentNode, res)
    depth = depth + 1
    if depth > 50 or currentNode == nil then
        return res
    end

    local nextNode

    if type(currentNode) == "table" then
        nextNode = {}
        local nodeJson
        local node
        for _, val in pairs(currentNode) do
            nodeJson = redis.call("HGET", KEYS[1], val)
            if nodeJson then
                node = cjson.decode(nodeJson)
                table.insert(res, nodeJson)
                if node and node.children then
                    for _, child in pairs(node.children) do
                        table.insert(nextNode, child)
                    end
                end
            end
        end
        if table.getn(nextNode) == 0 then
            nextNode = nil
        end
    else
        return res
    end

    return getAllChild(nextNode, res)
end

if KEYS[1] and ARGV and table.getn(ARGV) then
    return getAllChild(ARGV, {})
end

return {}
