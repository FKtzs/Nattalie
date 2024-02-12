/*
 * 提供给那些没有无水、无岩浆、无蜘蛛网的公益或内部
 * NoWeb、NoLiquid和快速上梯子作者为starontop，NoLava为我在这个基础上添加的
 * 开源地址：https://github.com/FKtzs/Nattalie
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
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoWeb-NoLiquid", description = "NoWeb,Liquid,Lava", category = ModuleCategory.MOVEMENT)
class NoWeb : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        val theWorld = mc.theWorld
        val searchBlocks = BlockUtils.searchBlocks(6)
        val searchBlocks2 = BlockUtils.searchBlocks(3)
        
        for (block in searchBlocks){
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
            /* 别忘了在LiquidBounce_at这个文件里面加上这句：

            public net.minecraft.entity.Entity field_70171_ac #inWater

             */
            if (blocks == Blocks.LAVA) {
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                (theWorld as WorldClientImpl).wrapped.setBlockToAir(blockpos)
            }
        }
        for (block in searchBlocks2) {
            val blockpos = block.key.unwrap()
            val blocks = block.value.unwrap()

            if(blocks is BlockWeb){
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                mc2.player.isInWeb = false
            }
        }

        if (mc2.player.isOnLadder && mc2.gameSettings.keyBindJump.isKeyDown) { // 这个为快速上梯子，忘记标注啦
            if (mc2.player.motionY >= 0.0) {
                mc2.player.motionY = 0.1786
            }
        }
    }

    override val tag: String
        get() = "bypass"
}
