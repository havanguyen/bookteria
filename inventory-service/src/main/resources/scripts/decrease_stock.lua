
local current_stock = redis.call('GET', KEYS[1])
local quantity_to_decrease = tonumber(ARGV[1])

if current_stock == false or tonumber(current_stock) < quantity_to_decrease then
  return 0
else
  redis.call('DECRBY', KEYS[1], quantity_to_decrease)
  return 1
end