/*
 * 我列个豆，fly开源这么久了，你都还没有吗？
 * 一次别飞太久，也别飞太远
 * by Pursue（193923709）
 * 开源地址：https://github.com/FKtzs/Nattalie
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.Hyt.Disabler2
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.world.GameType
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "GrimFlyDis", description = "死亡后获得飞行", category = ModuleCategory.MISC)
class GrimFlyDis : Module() {

    // 用法是开着，然后掉虚空就行了，剩下的会自动执行，飞完自己关一下fly就行了
    // 每局开局手动打开一下就行了，打完了或者床没了就关
    // 希望你能明白我的意思

    private val packets = LinkedBlockingQueue<SPacketConfirmTransaction>()
    var delay = false
    private var isDead = false
    private var isFly = false

    val timer = MSTimer()
    val ac = MSTimer()
    var a = 0

    override fun onDisable() {
        delay = false
        isDead = false
        isFly = false
        while (!packets.isEmpty()){
            mc.netHandler2.handleConfirmTransaction(packets.take())
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val fly = LiquidBounce.moduleManager[Fly::class.java] as Fly // fly选香草还是啥的都行，选香草的话我建议你speed拉3.5
        if (classProvider.isGuiGameOver(mc.currentScreen)) {
            LiquidBounce.moduleManager[Disabler2::class.java].state = false // 换成你的PostDis，如果没有就删掉
            isDead = true
            timer.reset()

            if (ac.hasTimePassed(5000)) {
                Chat.print("主播似了，已为您开启飞行修复")
                ac.reset()
                a = 0
            }
        }

        if (timer.hasTimePassed(5000) && isDead) {
            LiquidBounce.moduleManager[Fly::class.java].state = true
            isFly = true

            timer.reset()

            Chat.print("已为你自动打开飞行")
        }

        if (isFly && !fly.state) {
            isDead = false

            Chat.print("主播的飞行已结束")

            a++

            if (a == 3) {
                a = 0
                delay = false
                while (!packets.isEmpty()) {
                    mc.netHandler2.handleConfirmTransaction(packets.take())
                }

                LiquidBounce.moduleManager[Disabler2::class.java].state = true // 换成你的PostDis，如果没有就删掉

                Chat.print("发包已全部归还")

                isFly = false
            }
        }
    }

    @EventTarget
    fun onPacket(event:PacketEvent) {
        val packet = event.packet.unwrap()
        if (packet is SPacketPlayerPosLook) {
            if (mc2.player.capabilities.isFlying && isDead) {
                delay = true
            }
        }
        if (delay) {
            if (packet is SPacketConfirmTransaction) {
                packets.add(packet)
                event.cancelEvent()
                mc2.connection!!.sendPacket(CPacketConfirmTransaction())
            }
        }
    }
}
