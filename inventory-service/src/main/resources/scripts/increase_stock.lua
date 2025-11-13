
local current_stock = redis.call('GET', KEYS[1])
local quantity_to_increase = tonumber(ARGV[1])

if current_stock == false  then
	return 0
else
	redis.call('INCRBY', KEYS[1], quantity_to_increase)
	return 1
end