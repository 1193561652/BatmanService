function getUrl()
    return "http://cnzhuitao.cn/index.html"
end



function findABString(str, type)
    local findABegin = "<dt><strong><a href=\"[^\"]+\" target=\"_blank\">A级通缉令</a></strong>"
    local findAEnd = "</div>"
    if(type == "B")
    then
        findABegin = "<dt><strong><a href=\"[^\"]+\" target=\"_blank\">B级通缉令</a></strong>"
    end
    local b1, e1, s1 = string.find(str, findABegin, 0, false)
    if(b1 == nil)
    then
        return nil
    end
    local b2, e2, s2 = string.find(str, findAEnd, b1, false)
    if(b2 == nil)
    then
        return nil
    end
    if(b2 > b1)
    then
        local aSubStr = string.sub(str, b1, b2)
        return aSubStr
    end
    return aSubStr
end


function getFirstInfo(str)
    local match = "<li><span class=\"date\">(%d*--%d*)</span><a href=\"[^\"]+\" target=\"_blank\"><strong>([^<]+)</strong></a></li>"
    local time, name = string.match(str, match, 1)
    return time,name
end


function needWorking(str)
    local aSubStr = findABString(str, "A")
    local bSubStr = findABString(str, "B")
    local timeA, nameA
    local timeB, nameB

    if(aSubStr ~= nil)
    then
        timeA, nameA = getFirstInfo(aSubStr)
    end

    if(bSubStr ~= nil)
    then
        timeB, nameB = getFirstInfo(bSubStr)
    end

    print(timeA, nameA)
    print(timeB, nameB)



    local getTime = os.date("%m-%d")
    --print(getTime)

    if(timeA == nil and timeB == nil)
    then
        return "error", "", "", ""
    end


    if(timeA ~= nil and timeA >= getTime)
    then
        return "true", "A", nameA, timeA
    end

    if(timeB ~= nil and timeB >= getTime)
    then
        return "true", "B", nameB, timeB
    end

    if(timeA ~= nil )
    then
        return "false", "A", nameA, timeA
    else
        if(timeB ~= nil)
        then
            return "false", "B", nameB, timeB
        end
    end
end