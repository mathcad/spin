local rootKey = KEYS[1]
local parentCode = ARGV[1]
local depth = 0

local function getAllChild(currentNode, res)
    depth = depth + 1
    if depth > 30 or currentNode == nil then
        return res
    end

    local nextNode

    if type(currentNode) == "string" then
        local nodeJson = redis.call("HGET", rootKey, currentNode)
        if nodeJson then
            local node = cjson.decode(nodeJson)
            if node and node.children then
                nextNode = node.children
            end
        end
    elseif type(currentNode) == "table" then
        nextNode = {}
        local nodeJson
        local node
        local cnt = 0
        for _, val in ipairs(currentNode) do
            nodeJson = redis.call("HGET", rootKey, tostring(val))
            if nodeJson then
                node = cjson.decode(nodeJson)
                table.insert(res, nodeJson)
                if node and node.children then
                    for _, child in ipairs(node.children) do
                        table.insert(nextNode, child)
                        cnt = cnt + 1
                    end
                end
            end
        end
        if cnt == 0 then
            nextNode = nil
        end
    else
        return res
    end

    return getAllChild(nextNode, res)
end

if rootKey and parentCode then
    return getAllChild(parentCode, {})
end

return {}
