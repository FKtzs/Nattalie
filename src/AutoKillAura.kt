/*
 * 提供于那些懒到杀戮都不舍得自己开的
 * 放心食用，打完人会自动关，如果没自动关，那就是你附近还有人
 * by Pursue（193923709）
 * 开源地址：https://github.com/FKtzs/Nattalie
 */
package net.ccbluex.liquidbounce.features.module.modules.Hyt

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.NoFriends
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "AutoKillAura", description = "顾名思义", category = ModuleCategory.HYT)
class AutoKillAura : Module() {

    private val range = FloatValue("Range", 6f, 3f, 8f)
    private val teams = BoolValue("Teams",true) // 队伍检测，关联的是你Teams的设置，你Teams调好了这个就不会出错

    private var aura = false
    var team = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc2.player
        val theWorld = mc.theWorld!!

        if (teams.get()) {
            for (entity in theWorld.loadedEntityList) {
                if (!classProvider.isEntityLivingBase(entity) || !isEnemy(entity))
                    continue
                team = true
            }
            for (entity in theWorld.loadedEntityList) {
                if (isEnemy(entity)) {
                    team = false
                }
            }
        }
        if (thePlayer != null) {
            var foundRange = false
            val worldPlayers = mc2.world.playerEntities
            for (player in worldPlayers) {
                if (player != thePlayer && thePlayer.getDistance(player) <= range.get()) {
                    foundRange = true
                    break
                }
            }
            if (teams.get()) {
                if (foundRange) {
                    if (!aura && team) {
                        LiquidBounce.moduleManager[KillAura::class.java].state = true
                        aura = true
                    }
                } else if (aura || !team) {
                    LiquidBounce.moduleManager[KillAura::class.java].state = false
                    aura = false
                }
            } else {
                if (foundRange) {
                    if (!aura) {
                        LiquidBounce.moduleManager[KillAura::class.java].state = true
                        aura = true
                    }
                } else if (aura) {
                    LiquidBounce.moduleManager[KillAura::class.java].state = false
                    aura = false
                }
            }
        }
    }
    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: IEntity?): Boolean {
        if (classProvider.isEntityLivingBase(entity) && entity != null && (EntityUtils.targetDead || isAlive(entity.asEntityLivingBase())) && entity != mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.invisible)
                return false

            if (EntityUtils.targetPlayer && classProvider.isEntityPlayer(entity)) {
                val player = entity.asEntityPlayer()

                if (player.spectator || AntiBot.isBot(player))
                    return false

                if (player.isClientFriend() && !LiquidBounce.moduleManager[NoFriends::class.java].state)
                    return false

                val teams = LiquidBounce.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity.asEntityLivingBase())
            }

            return EntityUtils.targetMobs && entity.isMob() || EntityUtils.targetAnimals && entity.isAnimal()
        }

        return false
    }
    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: IEntityLivingBase) = entity.entityAlive && entity.health > 0 || entity.hurtTime > 5
}
