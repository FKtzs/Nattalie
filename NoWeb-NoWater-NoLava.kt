/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.WorldClientImpl
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.injection.backend.utils.unwrap
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockWeb
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoWeb-NoWater-NoLava", description = "Prevents you from getting slowed down in webs.", category = ModuleCategory.MOVEMENT)
class NoWeb : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        val theWorld = mc.theWorld
        val searchBlocks = BlockUtils.searchBlocks(4)
        val search2Blocks = BlockUtils.searchBlocks(2)

        for (block in search2Blocks){
            val blockpos = block.key.unwrap()
            val blocks = block.value.unwrap()
            if(blocks is BlockWeb){
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                mc2.player.isInWeb = false
            }
            if(blocks is BlockLiquid){
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                mc2.player.inWater = false
            }
            if (blocks == Blocks.LAVA) {
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                (theWorld as WorldClientImpl).wrapped.setBlockToAir(blockpos)
            }
        }
        for (block in searchBlocks){
            val blockpos = block.key.unwrap()
            val blocks = block.value.unwrap()
            if (blocks is BlockLiquid){
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                mc2.player.inWater = false
            }
        }
        for (block in searchBlocks){
            val blockpos = block.key.unwrap()
            val blocks = block.value.unwrap()
            if (blocks == Blocks.LAVA) {
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                (theWorld as WorldClientImpl).wrapped.setBlockToAir(blockpos)
            }
        }

        if (mc2.player.isOnLadder && mc2.gameSettings.keyBindJump.isKeyDown) {
            if (mc2.player.motionY >= 0.0) {
                mc2.player.motionY = 0.1786
            }
        }
    }

    override val tag: String?
        get() = "bypass"
}
