/*
 * 更好的NoSlow，已开源出跑拉弓，购入跑吃找Pursue
 * 跑拉弓请保证弓在左边的格子是空气或者方块，不能为剑、食物、药水等
 * by Pursue（193923709）
 * 开源地址：https://github.com/FKtzs/Nattalie
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import me.utils.PacketUtils
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.WEnumHand
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.createUseItemPacket
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "GrimNoSlow", description = "No items use the slow", category = ModuleCategory.MOVEMENT)
class GrimNoSlow : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Post", "Vanilla"), "Post")

    private val ac = FloatValue("Consume Forward Multiplier", 1.0F, 0.2F, 1.0F)
    private val ab = FloatValue("Consume Strafe Multiplier", 1.0F, 0.2F, 1.0F)

    private val usw = BoolValue("walkEat", false) // 走吃

    // 跑拉弓请保证弓在左边的格子是空气或者方块，不能为剑、食物、药水等

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!(mc.gameSettings.keyBindUseItem.isKeyDown)) {
            return
        }

        val curSlot = mc.thePlayer!!.inventory.currentItem
        val spoof = if (curSlot == 0) 1 else -1

        when(modeValue.get().toLowerCase()) {
            "vanilla" -> {
                mc.thePlayer!!.motionX=mc.thePlayer!!.motionX
                mc.thePlayer!!.motionY=mc.thePlayer!!.motionY
                mc.thePlayer!!.motionZ= mc.thePlayer!!.motionZ
            }
            "post" -> {
                if (classProvider.isItemSword(mc.thePlayer!!.heldItem?.item)) {
                    if (event.eventState == EventState.PRE) {
                        mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(0, 0, 0), EnumFacing.DOWN))
                    }
                    if (event.eventState == EventState.POST) {
                        PacketUtils.sendPacketC0F()
                        mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
                    }
                }
                if (classProvider.isItemBow(mc.thePlayer!!.heldItem?.item)) {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(curSlot + spoof))
                        mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
                        mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(curSlot))
                    }
                    if (event.eventState == EventState.POST) {
                        mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                    }
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (usw.get()) {
            if (mc.thePlayer!!.onGround) {
                ac.set(0.79)
                ab.set(0.2)
            } else {
                ac.set(0.0)
                ab.set(0.0)
            }
        } else {
            ac.set(0.2)
            ab.set(0.2)
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer!!.heldItem?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: IItem?, isForward: Boolean): Float {
        return when {
            classProvider.isItemFood(item) || classProvider.isItemPotion(item) || classProvider.isItemBucketMilk(item) -> {
                if (isForward) this.ac.get() else this.ab.get()
            }
            classProvider.isItemSword(item) -> 1.0F

            classProvider.isItemBow(item) -> 0.2F
            else -> 0.2F
        }
    }

    override val tag: String
        get() = modeValue.get()
}
