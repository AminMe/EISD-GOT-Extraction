--data = io.open("Arya_Stark.txt"):read("*all")
--data = data:gsub("%%(%w%w)", function(s) return string.char(tonumber(s,16)) end)
--file = io.open("res.txt", "w")
--file:close()
-- io.lines(file) si on ne specifie rien => input


function scandir(directory)
    local i, t, popen = 0, {}, io.popen
    local pfile = popen('ls -a "'..directory..'"')
    for filename in pfile:lines() do
    	if(filename=="." or filename==".." or filename==".DS_Store") then
    		print("Not supported : "..filename)
    	else
    		i = i + 1
	        t[i] = filename
    	end
    end
    pfile:close()
    return t
end


function characters(file)

end

function locations(file)

end

function noblehouses(file)

end

function hex16(filename)
	print("Opening ... "..filename)
	data = io.open(filename):read("*all")
	data = data:gsub("%%(%w%w)", function(s) return string.char(tonumber(s,16)) end)
	--filename = string.gsub(filename,".txt","");
	--file = io.open(filename.."_lua.txt", "w")
	file = io.open(filename, "w")
	file:write(data)
	file:close()
end


racine = "structured"
dossiers = scandir(racine)
-- parcourir le dossier structured
for key,value in pairs(dossiers) do
	print(key.." -> "..value)

	dirCorpus = scandir(racine.."/"..value)
	for kC, vC in pairs(dirCorpus) do
		--print(kC.." -> "..vC)
		hex16(racine.."/"..value.."/"..vC)
	end

	--[[if value=="Characters" then
		dirCharacters = scandir(racine.."/"..value)
	elseif value=="Locations" then
		dirLocations = scandir(racine.."/"..value)
	elseif value=="Noble_houses" then
		dirHouses = scandir(racine.."/"..value)
		
	end--]]
	print("[[[[[[end key]]]]]]")
end
