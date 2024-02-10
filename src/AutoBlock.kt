/*
 * 提供于那些没有postDis的公益或内部的自动防砍，这个可以让你们在对刀的时候和那些防砍跑的一样的效果（没有NoXZ你打个屁花雨庭啊，这么残疾别玩了）
 * NoSlow模式选香草都没问题，原理是什么请看下面注释
 * by Pursue（193923709）
 * 开源地址：https://github.com/FKtzs/Nattalie
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock

@ModuleInfo(name = "AutoBlock", description = "AutoBlock", category = ModuleCategory.COMBAT)
class AutoBlock : Module() {

    private val blockRange = FloatValue("BlockRange", 5f, 0f, 8f)
    private val nopostRange = FloatValue("NoPostDisRange",1.5f,0f,2.2f)
    private val c08 = BoolValue("SendC08", true)
    private val noPostDis = BoolValue("NoPostDis",false) // 小声BB，花雨庭就算和谐了postDis，这个也可以绕噢

    var block = false
    private var dis = false
    private var hit = false
    private var blocking = false
    val a = LiquidBounce.moduleManager[KillAura::class.java] as KillAura

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        hit = RotationUtils.isFaced(a.target, 0.05)
        if (noPostDis.get()) {
            if (mc2.player != null) {
                var foundRange = false
                val worldPlayers = mc2.world.playerEntities
                for (player in worldPlayers) {
                    if (player != mc2.player && mc2.player.getDistance(player) <= nopostRange.get()) { // 当玩家在另一个玩家旁边不超过2格时，Grim就不叠noSlowA了，这时候香草防砍都可以
                        foundRange = true
                        break
                    }
                }
                if (foundRange) {
                    block = true
                } else if (block) {
                    block = false
                }
            }
        } else {
            if (mc2.player != null) {
                var foundRange = false
                val worldPlayers = mc2.world.playerEntities
                for (player in worldPlayers) {
                    if (player != mc2.player && mc2.player.getDistance(player) <= blockRange.get()) { // 有PostDis和叠Post的NoSlow就不需要离人1.3格才防砍,直接随便防了
                        foundRange = true
                        break
                    }
                }
                if (foundRange) {
                    block = true
                } else if (block) {
                    block = false
                }
            }
        }
        if (a.state && block && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) {
            mc.gameSettings.keyBindUseItem.pressed = true
            blocking = true
        } else if (blocking) {
            mc.gameSettings.keyBindUseItem.pressed = false
            blocking = false
        }
        val objectMouseOver = mc.objectMouseOver
        if (objectMouseOver != null
            && EntityUtils.isSelected(objectMouseOver.entityHit, true)
            && blocking && !hit) {
            dis = true
        } else {
            dis = false
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()
        if (!dis && (packet is CPacketPlayerTryUseItemOnBlock) && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item) && blocking && !hit && a.state) {
            event.cancelEvent()
        }
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!MovementUtils.isMoving) {
            return
        }
        if (c08.get()){
            if (event.eventState == EventState.PRE && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) {
                mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerBlockPlacement(mc.thePlayer!!.inventory.getCurrentItemInHand() as IItemStack))
            }
        }
    }
}
