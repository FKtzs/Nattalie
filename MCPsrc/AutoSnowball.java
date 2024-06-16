package net.pursue.features.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.pursue.Nattalie;
import net.pursue.event.EventTarget;
import net.pursue.event.events.player.EventJump;
import net.pursue.event.events.player.EventRotation;
import net.pursue.event.events.player.EventStrafe;
import net.pursue.event.events.update.EventMotion;
import net.pursue.event.events.update.EventUpdate;
import net.pursue.features.module.Mod;
import net.pursue.features.module.modules.player.SaveOneself;
import net.pursue.utils.*;
import net.pursue.utils.timer.TimerUtils;
import net.pursue.value.values.NumberValue;

import java.util.ArrayList;
import java.util.List;

import static net.pursue.utils.MinecraftMc.mc;

// 欢迎你看到这个注释
// 是的，这个是我补上的注释，发的时候忘记加了
// by Pursue（193923709） 电话：19178598813 邮箱：193923709@qq.com
// Nattalie水影/MCP码字开源群：673246810

public class AutoEgg extends Mod {
    private final NumberValue<Double> Range = new NumberValue<>("Range", 10.0, 0.0, 30.0, 1.0); // 距离
    private final NumberValue<Double> delay = new NumberValue<>("Delay", 1.0, 0.0, 10.0, 1.0); // 投掷延迟
    private final NumberValue<Double> number = new NumberValue<>("Number", 1.0, 0.0, 16.0, 1.0); // 投掷数量
    private final NumberValue<Double> Handoff = new NumberValue<>("HandoffDelay", 100.0, 0.0, 1000.0, 10.0); // 切换目标延迟
    private final NumberValue<Double> fov = new NumberValue<>("FOV", 90.0, 0.0, 180.0, 10.0); // 角度

    public AutoEgg() {
        super("AutoSnowball", Category.COMBAT);
        addValues(Range, number, Handoff, delay, fov);
    }

    private final TimerUtils switchTimer = new TimerUtils();

    int index;

    private float yaw, pitch;
    private final List<EntityPlayer> targets = new ArrayList<>();
    private TimerUtils timer = new TimerUtils();
    private EntityPlayer target;

    @Override
    public void enable() {
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
        index = 0;
        switchTimer.reset();
        targets.clear();
        target = null;
        super.enable();
    }

    @Override
    public void disable() {
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
        index = 0;
        switchTimer.reset();
        targets.clear();
        target = null;
        super.disable();
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (eventMotion.getType() == EventMotion.Type.Pre) {
            long te = delay.getValue().longValue() * 50L;
            if (target != null) {
                if (target.getHealth() <= 0 || target.isDead || mc.player.getDistance(target.posX, target.posY, target.posZ) > Range.getValue() || mc.player.getDistance(target.posX, target.posY, target.posZ) <= 4) {
                    target = null;
                }
            }

            if (target != null) {
                if (getEgg() < 0 || !isVisibleFOV(target, fov.getValue().intValue())) {
                    target = null;
                }
            }

            if (target != null && !Nattalie.moduleManager.getByClass(SaveOneself.class).isEnable()) {
                if (timer.hasTimePassed(te)) {
                    this.throwing();
                    timer.reset();
                }
            }

            List<EntityPlayer> targets = this.getTargets();

            if (this.targets.size() > 1) {
                if (switchTimer.hasTimePassed(Handoff.getValue().intValue())) {
                    switchTimer.reset();
                    ++this.index;
                }
            }
            if (this.index >= this.targets.size()) {
                this.index = 0;
            }

            if (!targets.isEmpty()) {
                target = targets.get(index);
            }
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (target != null) {
            calculateRotation(target);

            yaw = calculateRotation(target)[0];
            pitch = calculateRotation(target)[1];
        }

        if (target != null) {
            if (target.getDistanceToEntity(mc.player) <= Range.getValue()) {
                if (mc.player.onGround) {
                    if (target.getDistanceToEntity(mc.player) >= 8 && target.getDistanceToEntity(mc.player) <= 12) {
                        pitch -= 3;
                    } else if (target.getDistanceToEntity(mc.player) >= 13 && target.getDistanceToEntity(mc.player) <= 17) {
                        pitch -= 5;
                    } else if (target.getDistanceToEntity(mc.player) >= 18 && target.getDistanceToEntity(mc.player) <= 24) {
                        pitch -= 8;
                    } else if (target.getDistanceToEntity(mc.player) >= 25 && target.getDistanceToEntity(mc.player) <= Range.getValue()) {
                        pitch -= 11;
                    }
                }
            }
        }

        if (target == null) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
        }
    }


    @EventTarget
    private void onRotation(EventRotation eventRotation) {
        if (Nattalie.moduleManager.getByClass(SaveOneself.class).isEnable()) return; // 这个是卡空
        eventRotation.setRotation(yaw, pitch, false);
    }

    @EventTarget
    private void onStrafe(EventStrafe eventStrafe) {
        if (target != null) {
            eventStrafe.setYaw(yaw);
        }
    }

    @EventTarget
    private void onJump(EventJump eventJump) {
        if (target != null) {
            eventJump.setYaw(yaw);
        }
    }

    private List<EntityPlayer> getTargets() {
        targets.clear();
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer sb = (EntityPlayer) entity;
                if ((sb.getDistanceToEntity(mc.player) <= Range.getValue() && sb.getDistanceToEntity(mc.player) >= 4) && sb != mc.player && getEgg() > 0 && isVisibleFOV(sb, fov.getValue().intValue())) {
                    targets.add((EntityPlayer) entity);
                }
            }
        }
        return targets;
    }

    private void throwing() {
        mc.player.connection.sendPacket(new CPacketHeldItemChange(getEgg()));
        for (int a = 0; a < number.getValue(); a++) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        }
        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
    }

    public int getEgg() { // 改成坤但也行，之前是写的坤但，所以没改名
        for (int i = 0; i < 9; ++i) {
            if (!mc.player.inventoryContainer.getSlot(i + 36).getHasStack()
                    || !(mc.player.inventoryContainer.getSlot(i + 36).getStack()
                    .getItem() instanceof ItemSnowball))
                continue;
            return i;
        }
        return -1;
    }

    private float[] calculateRotation(EntityPlayer player) {
        double deltaX = player.posX - mc.player.posX;
        double deltaY = (player.posY + player.getEyeHeight()) - (mc.player.posY + mc.player.getEyeHeight());
        double deltaZ = player.posZ - mc.player.posZ;
        double yaw = Math.atan2(deltaZ, deltaX) * (180 / Math.PI) - 90;
        double pitch = -Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)) * (180 / Math.PI);

        return new float[]{(float) yaw, (float) pitch};
    }

    private boolean isVisibleFOV(final EntityPlayer e, final float fov) {
        return ((Math.abs(calculateRotation(e)[0] - mc.player.rotationYaw) % 360.0f > 180.0f) ? (360.0f - Math.abs(calculateRotation(e)[0] - mc.player.rotationYaw) % 360.0f) : (Math.abs(calculateRotation(e)[0] - mc.player.rotationYaw) % 360.0f)) <= fov;
    }
}
