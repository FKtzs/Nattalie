/*
 * 提供于那些懒到杀戮都不舍得自己开的
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

    private var aura = false
    var team = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc2.player

        if (thePlayer != null) {
            var foundRange = false
            val worldPlayers = mc2.world.playerEntities
            for (player in worldPlayers) {
                if (player != thePlayer && thePlayer.getDistance(player) <= range.get()) {
                    foundRange = true
                    break
                }
            }
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
