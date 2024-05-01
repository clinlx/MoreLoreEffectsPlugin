Server:Log("")
 -- 在服务器控制台打出日志

Server:Wait(millisecond)
 -- 等待一段时间后继续执行

Server:Command("")
 -- 以服务器身份执行命令

Server:CommandWithRes("")
 -- 以服务器身份执行命令，并且获取命令的返回值（该命令比普通的Command多1tick的延迟，用于获取返回值）

Server:buildWorldPos(worldPos)
 -- 复制一个WorldPos对象

Server:buildWorldPos(location)
 -- 用Location对象构建一个WorldPos对象

Server:buildWorldPos("worldName",x,y,z)
 -- 用3维坐标构建一个WorldPos对象

Server:buildWorldPos("worldName",x,y,z,yaw,pitch)
 -- 用5维坐标构建一个WorldPos对象

Server:DrawParticle(particleName,p_num,worldPos,range_x,range_y,range_z)
 -- 播放粒子

Skill:getDefCd()
 -- 获取当前技能在配置文件中所设置的技能cd时间，单位毫秒

Skill:SetCoolDown(millisecond)
 -- 设置当前技能所在类技能的冷却时间(就是让该技能进入冷却)

Skill:SetTypeCoolDown("typeName",millisecond)
 -- 使用类型名来设置某一类技能的冷却时间(就是让该技能进入冷却)

Player:Inform("")
 -- 给玩家发消息

Player:Command("")
 -- 以释放技能的玩家的身份执行命令
Player:CommandWithRes("")
 -- 以释放技能的玩家的身份执行命令，并且获取命令的返回值（该命令比普通的Command多1tick的延迟，用于获取返回值）

Player:Teleport(worldPos)
 -- 传送玩家到指定WorldPos

Player:Teleport(x,y,z)
 -- 传送玩家到当前世界的指定坐标

Player:getName()
 -- 返回玩家的玩家名

Player:getWorldName() 
-- 返回玩家所在的世界名，可以用来构造WorldPos

Player:getX()
 -- 返回玩家所在的坐标x

Player:getY()
 -- 返回玩家所在的坐标y

Player:getZ()
 -- 返回玩家所在的坐标z

Player:getYaw()
 -- 返回玩家所在的旋转坐标yaw

Player:getPitch()
 -- 返回玩家所在的旋转坐标pitch

Player:getWorldPos()
 -- 返回玩家所在的WorldPos

Player:getMaxHealth()
 -- 返回玩家的最大生命值

Player:getHealth()
 -- 返回玩家的当前生命值
 
Player:setHealth(double)
 -- 修改玩家的当前生命值