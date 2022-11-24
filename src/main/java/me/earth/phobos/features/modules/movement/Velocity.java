package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {

    public Setting<Boolean> noPush = register(new Setting("NoPush", true));
    public Setting<Float> horizontal = register(new Setting("Horizontal", 0.0f, 0.0f, 100.0f));
    public Setting<Float> vertical = register(new Setting("Vertical", 0.0f, 0.0f, 100.0f));
    public Setting<Boolean> explosions = register(new Setting("Explosions", true));

    private static Velocity INSTANCE = new Velocity();

    public Velocity() {
        super("Velocity", "Allows you to control your velocity", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Velocity getINSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new Velocity();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if(event.getStage() == 0 && mc.player != null) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                SPacketEntityVelocity velocity = event.getPacket();
                if (velocity.getEntityID() == mc.player.entityId) {
                    if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                        event.setCanceled(true);
                        return;
                    }

                    velocity.motionX *= horizontal.getValue();
                    velocity.motionY *= vertical.getValue();
                    velocity.motionZ *= horizontal.getValue();
                }
            }

            if (explosions.getValue() && event.getPacket() instanceof SPacketExplosion) {
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                    event.setCanceled(true);
                    return;
                }

                SPacketExplosion velocity = event.getPacket();
                velocity.motionX *= horizontal.getValue();
                velocity.motionY *= vertical.getValue();
                velocity.motionZ *= horizontal.getValue();
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if(noPush.getValue() && event.entity.equals(mc.player)) {
            if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                event.setCanceled(true);
                return;
            }

            event.x = -event.x * horizontal.getValue();
            event.y = -event.y * vertical.getValue();
            event.z = -event.z * horizontal.getValue();
        }
    }
}
