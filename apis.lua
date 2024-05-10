Server:Log("")
 -- 在服务器控制台打出日志

Server:Wait(millisecond)
 -- 等待一段时间后继续执行

Server:Command("")
 -- 以服务器身份执行命令

Server:CommandWithRes("")
 -- 以服务器身份执行命令，并且获取命令的返回值（该命令比普通的Command多1tick的延迟，用于获取返回值）

Server:SetSaveData("Key",11)
 -- 根据Key，在服务器的硬盘上永久存储数据，根据Key来存取
 -- 该数据存储在服务器空间，全服务器共享，支持数字和字符串
 -- 使用“\/:*?"<>|~”等字符作为Key可能会导致奇怪行为，请勿包含这些字符
 
Server:GetSaveData("Key")
 -- 根据Key，获取对应Key在服务器空间存储的数据,返回此数据

Server:AddSaveData("Key",-1)
 -- 根据Key，对服务器空间的数据进行自增，并且返回增加后的数据

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

Player:SetSaveData("Key",11)
 -- 根据Key，在服务器的硬盘上永久存储数据，根据Key来存取
 -- 该数据存储在玩家数据空间，每个玩家独享自己的变量数据空间
 -- 使用“\/:*?"<>|~”等字符作为Key可能会导致奇怪行为，请勿包含这些字符

Player:GetSaveData("Key")
 -- 根据Key，获取对应Key在玩家数据空间存储的数据,返回此数据

Player:AddSaveData("Key",-1)
 -- 根据Key，对玩家数据空间的数据进行自增，并且返回增加后的数据 

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

 ---------- 特殊参数Args ----------
Args.InvokeMode
 -- 根据技能触发方式的不同，有三种值："Skill"、"Consume"、"Attack"
 ------- Skill -------
    -- 普通技能，触发参数暂无
 ------- Consume -------
    -- 消耗品，触发参数暂无
 ------- Attack -------
    Args.ExpectAttackDamage
    Args.FinalAttackDamage
    Args.TargetEntityName
    Args.TargetEntityId
    Args.TargetEntityPosX
    Args.TargetEntityPosY
    Args.TargetEntityPosZ
    Args.TargetEntityHeight
    Args.TargetEntityWidth
    Args.TargetEntityHealth
    Args.TargetEntityMaxHealth
    -- 攻击特效，触发参数共11条