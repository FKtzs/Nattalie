package net.pursue.features.module.modules.movement;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.util.EnumHand;
import net.pursue.event.EventTarget;
import net.pursue.event.events.packet.EventPacket;
import net.pursue.event.events.player.EventScreen;
import net.pursue.event.events.update.EventMotion;
import net.pursue.event.events.update.EventUpdate;
import net.pursue.event.events.world.EventWorldLoad;
import net.pursue.features.module.Mod;
import net.pursue.utils.Category;

import static net.pursue.utils.MinecraftMc.mc;

public class StoreNoSlow extends Mod {
    public StoreNoSlow() {
        super("StoreNoSlow","商店无减速", Category.MOVEMENT);
    }

    /**
     * Store NoSlow
     * Code by XinKong (qq226355502)
     * Open Source by Pursue (qq193923709)
     * Fix by Pursue
     * 使用教程：买好东西后开启这个功能后右键一次商店就可以跑吃跑喝了，跑吃会出现物品不会清除的问题，手动点一下让刷新即可
     * Date: 2024/02/18
     */

    private int windowsID;
    private GuiContainer container;
    private boolean noslow;

    @Override
    public void en() {
        windowsID = 0;
        noslow = false;
    }

    @Override
    public void dis() {
        if (container != null) {
            mc.player.connection.sendPacket(new CPacketCloseWindow(windowsID));
            container = null;
        }
    }

    @EventTarget
    private void onScreen(EventScreen eventScreen) { // 没有这个事件就抄水影的
        GuiScreen guiScreen = eventScreen.getGuiScreen();

        if (guiScreen instanceof GuiContainer && !(guiScreen instanceof GuiInventory)) {
            container = (GuiContainer) eventScreen.getGuiScreen();
        }
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (eventMotion.getType() == EventMotion.Type.Pre) {
            Item stack = mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem();

            noslow = (stack instanceof ItemFood || stack instanceof ItemPotion);
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (!mc.player.isEntityAlive() || mc.player.ticksExisted <= 1) {
            container = null;
            noslow = false;
        }

        // 1.8没有副手就改成mc.thePlayer.getHeldItem()
        if (windowsID != 0 && noslow) {
            mc.player.connection.sendPacket(new CPacketClickWindow(windowsID,0,0, ClickType.THROW, mc.player.getHeldItem(EnumHand.OFF_HAND), (short) 0));
        }
    }

    @EventTarget
    private void onPacket(EventPacket event) {
        Packet<?> packet = event.getPacket();

        if (windowsID != 0) {
            if (noslow) {
                if (packet instanceof CPacketPlayerDigging cPacketPlayerDigging && cPacketPlayerDigging.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    event.cancelEvent();
                }

                if (packet instanceof CPacketCloseWindow) {
                    event.cancelEvent();
                }
            }
        }

        if (packet instanceof SPacketOpenWindow sPacketOpenWindow) {
            windowsID = sPacketOpenWindow.getWindowId();
            event.cancelEvent();
        }

        // SPacketCloseWindow的windowId是private，请改成public

        if (packet instanceof SPacketCloseWindow sPacketCloseWindow) {
            if (container != null && sPacketCloseWindow.windowId == container.inventorySlots.windowId) {
                container = null;
            }
        }
    }

    @EventTarget
    private void onWold(EventWorldLoad eventWorldLoad) {
        setEnable(false);
    }
}
