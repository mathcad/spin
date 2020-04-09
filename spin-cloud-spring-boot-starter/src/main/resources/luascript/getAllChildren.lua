local rootKey = KEYS[1]
local parent = ARGV
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
            nodeJson = redis.call("HGET", rootKey, val)
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

if rootKey and parent and table.getn(parent) then
    return getAllChild(parent, {})
end

return {}
