package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Phase extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.PACKETFLY));
    public Setting<PacketFlyMode> type = register(new Setting("Type", PacketFlyMode.SETBACK, v -> mode.getValue() == Mode.PACKETFLY));

    public Setting<Integer> xMove = register(new Setting("XMove", 625, 1, 1000, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK, "XMovement speed."));
    public Setting<Integer> yMove = register(new Setting("YMove", 625, 1, 1000, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK, "YMovement speed."));
    public Setting<Integer> zMove = register(new Setting("ZMove", 625, 1, 1000, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK, "ZMovement speed."));
    public Setting<Boolean> extra = register(new Setting("ExtraPacket", true, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK));
    public Setting<Integer> offset = register(new Setting("Offset", 1337, 0, 1337, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK && extra.getValue(), "Up speed."));
    public Setting<Boolean> fallPacket = register(new Setting("FallPacket", true, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK));
    public Setting<Boolean> teleporter = register(new Setting("Teleport", true, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK));
    public Setting<Boolean> boundingBox = register(new Setting("BoundingBox", true, v -> mode.getValue() == Mode.PACKETFLY && type.getValue() == PacketFlyMode.SETBACK));

    private static Phase INSTANCE = new Phase();
    private boolean teleport = true;

    public Phase() {
        super("Phase", "Makes you able to phase through blocks.", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Phase getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Phase();
        }
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.noClip = false;
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(type.getValue() == PacketFlyMode.NONE || event.getStage() != 0 || mc.isSingleplayer() || mode.getValue() != Mode.PACKETFLY) {
            return;
        }

        if(type.getValue() == PacketFlyMode.SETBACK) {
            if(!boundingBox.getValue()) {
                double[] directionalSpeed = getMovementByDirection();
                double posX = directionalSpeed[0];
                double posY = directionalSpeed[1];
                double posZ = directionalSpeed[2];
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(posX, posY, posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                if (extra.getValue()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, offset.getValue() + mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
                }
                if (fallPacket.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                mc.player.setPosition(posX, posY, posZ);
                teleport = !teleporter.getValue() || !teleport;
                event.setX(0.0);
                event.setY(0.0);
                event.setX(0.0);
                mc.player.noClip = true;
            }
        }
    }

    @SubscribeEvent
    public void onMove(UpdateWalkingPlayerEvent event) {
        if(fullNullCheck() || event.getStage() != 0 || !boundingBox.getValue() || type.getValue() != PacketFlyMode.SETBACK || mode.getValue() != Mode.PACKETFLY) {
            return;
        }

        double[] dirSpeed = this.getMotion(teleport ? 0.0225F : 0.0224F);
        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + (double)dirSpeed[0], mc.player.posY + (mc.gameSettings.keyBindJump.isKeyDown() ? (this.teleport ? 0.0625D : 0.0624D) : 1.0E-8D) - (mc.gameSettings.keyBindSneak.isKeyDown() ? (this.teleport ? 0.0625D : 0.0624D) : 2.0E-8D), mc.player.posZ + (double)dirSpeed[1], mc.player.rotationYaw, mc.player.rotationPitch, false));
        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, -1337.0D, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
        mc.player.setPosition(mc.player.posX + dirSpeed[0], mc.player.posY + (mc.gameSettings.keyBindJump.isKeyDown() ? (this.teleport ? 0.0625D : 0.0624D) : 1.0E-8D) - (mc.gameSettings.keyBindSneak.isKeyDown() ? (this.teleport ? 0.0625D : 0.0624D) : 2.0E-8D), mc.player.posZ + (double)dirSpeed[1]);
        this.teleport = !this.teleport;
        mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0.0D;
        mc.player.noClip = this.teleport;
    }

    private double[] getMovementByDirection() {
        double[] results = new double[3];
        switch(Phobos.rotationManager.getDirection4D()) {
            case 0:
                results[0] = mc.player.posX
                        + (mc.gameSettings.keyBindLeft.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindRight.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001);
                results[1] = mc.player.posY
                        + (mc.gameSettings.keyBindJump.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindSneak.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000002);
                results[2] = mc.player.posZ
                        + (mc.gameSettings.keyBindForward.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindBack.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001);
                break;
            case 1:
                results[0] = mc.player.posX
                        + (mc.gameSettings.keyBindBack.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindForward.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001);
                results[1] = mc.player.posY
                        + (mc.gameSettings.keyBindJump.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindSneak.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000002);
                results[2] = mc.player.posZ
                        + (mc.gameSettings.keyBindLeft.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindRight.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001);
                break;
            case 2:
                results[0] = mc.player.posX
                        + (mc.gameSettings.keyBindRight.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindLeft.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001);
                results[1] = mc.player.posY
                        + (mc.gameSettings.keyBindJump.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindSneak.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000002);
                results[2] = mc.player.posZ
                        + (mc.gameSettings.keyBindBack.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindForward.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001);
                break;
            case 3:
                results[0] = mc.player.posX
                        + (mc.gameSettings.keyBindForward.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindBack.isKeyDown() ? (teleport ? xMove.getValue() / 10000.0 : (xMove.getValue() - 1) / 10000.0) : 0.00000001);
                results[1] = mc.player.posY
                        + (mc.gameSettings.keyBindJump.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindSneak.isKeyDown() ? (teleport ? yMove.getValue() / 10000.0 : (yMove.getValue() - 1) / 10000.0) : 0.00000002);
                results[2] = mc.player.posZ
                        + (mc.gameSettings.keyBindRight.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001)
                        - (mc.gameSettings.keyBindLeft.isKeyDown() ? (teleport ? zMove.getValue() / 10000.0 : (zMove.getValue() - 1) / 10000.0) : 0.00000001);
                break;
            default:
        }
        return results;
    }

    /*private double[] moveLooking(int ignored) {
        return new double[]{mc.player.rotationYaw * 360 / 360 * 180 / 180, 0};
    }*/

    private double[] getMotion(double speed) {
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationYaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (moveForward != 0.0f) {
            if (moveStrafe > 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
            }
            else if (moveStrafe < 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
            }
            moveStrafe = 0.0f;
            if (moveForward > 0.0f) {
                moveForward = 1.0f;
            }
            else if (moveForward < 0.0f) {
                moveForward = -1.0f;
            }
        }
        double posX = moveForward * speed * -Math.sin(Math.toRadians(rotationYaw)) + moveStrafe * speed * Math.cos(Math.toRadians(rotationYaw));
        double posZ = moveForward * speed * Math.cos(Math.toRadians(rotationYaw)) - moveStrafe * speed * -Math.sin(Math.toRadians(rotationYaw));
        return new double[] {posX, posZ};
    }

    public enum Mode {
        PACKETFLY
    }

    public enum PacketFlyMode {
        NONE,
        SETBACK
    }
}
