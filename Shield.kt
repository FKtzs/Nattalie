package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack

@ModuleInfo(name = "Shield", description = "Animation", category = ModuleCategory.RENDER)
class Shield : Module() {

    private var bowStack: ItemStack? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = Minecraft.getMinecraft().player
        if (classProvider.isItemSword(mc.thePlayer!!.heldItem?.item)) {
            generateBowStack()
        } else {
            player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY)
        }
    }
    fun generateBowStack() {
        bowStack = ItemStack(Items.SHIELD)

        val player = Minecraft.getMinecraft().player
        if (player != null) {
            val offHandStack = player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND)
            if (offHandStack.isEmpty) {
                bowStack?.let { player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, it) }
            }
        }
    }
}
