package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.event.events.RenderEntityModelEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Skeleton extends Module {

    private final Setting<Integer> red = register(new Setting("Red", 255, 0, 255));
    private final Setting<Integer> green = register(new Setting("Green", 255, 0, 255));
    private final Setting<Integer> blue = register(new Setting("Blue", 255, 0, 255));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 255, 0, 255));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 1.5f, 0.1f, 5.0f));
    private final Setting<Boolean> colorFriends = register(new Setting("Friends", true));
    private final Setting<Boolean> invisibles = register(new Setting("Invisibles", false));

    private static Skeleton INSTANCE = new Skeleton();
    private final Map<EntityPlayer, float[][]> rotationList = new HashMap<>();

    public Skeleton() {
        super("Skeleton", "Draws a nice Skeleton.", Category.RENDER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Skeleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Skeleton();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        RenderUtil.GLPre(lineWidth.getValue());
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player != null && player != mc.getRenderViewEntity() && player.isEntityAlive() && !player.isPlayerSleeping() && (!player.isInvisible() || invisibles.getValue())) {
                if(rotationList.get(player) != null && mc.player.getDistanceSq(player) < 2500) {
                    renderSkeleton(player, rotationList.get(player), EntityUtil.getColor(player, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue(), colorFriends.getValue()));
                }
            }
        }
        RenderUtil.GlPost();
    }

    public void onRenderModel(RenderEntityModelEvent event) {
        if(event.getStage() == 0 && event.entity instanceof EntityPlayer) {
            if(event.modelBase instanceof ModelBiped) {
                ModelBiped biped = (ModelBiped)event.modelBase;
                float[][] rotations = RenderUtil.getBipedRotations(biped);
                EntityPlayer player = (EntityPlayer)event.entity;
                rotationList.put(player, rotations);
            }
        }
    }

    private void renderSkeleton(EntityPlayer player, float[][] rotations, Color color) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        Vec3d interp = EntityUtil.getInterpolatedRenderPos(player, mc.getRenderPartialTicks());
        double pX = interp.x;
        double pY = interp.y;
        double pZ = interp.z;
        GlStateManager.translate(pX, pY , pZ);
        GlStateManager.rotate(-player.renderYawOffset, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0D, 0.0D, player.isSneaking() ? -0.235D : 0.0D);
        float sneak = player.isSneaking() ? 0.6F : 0.75F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.125D, sneak, 0.0D);
        if (rotations[3][0] != 0.0F) {
            GlStateManager.rotate(rotations[3][0] * 57.295776F, 1.0F, 0.0F, 0.0F);
        }
        if (rotations[3][1] != 0.0F) {
            GlStateManager.rotate(rotations[3][1] * 57.295776F, 0.0F, 1.0F, 0.0F);
        }
        if (rotations[3][2] != 0.0F) {
            GlStateManager.rotate(rotations[3][2] * 57.295776F, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.glBegin(3);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, (-sneak), 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.125D, sneak, 0.0D);
        if (rotations[4][0] != 0.0F) {
            GlStateManager.rotate(rotations[4][0] * 57.295776F, 1.0F, 0.0F, 0.0F);
        }
        if (rotations[4][1] != 0.0F) {
            GlStateManager.rotate(rotations[4][1] * 57.295776F, 0.0F, 1.0F, 0.0F);
        }
        if (rotations[4][2] != 0.0F) {
            GlStateManager.rotate(rotations[4][2] * 57.295776F, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.glBegin(3);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, (-sneak), 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.translate(0.0D, 0.0D, player.isSneaking() ? 0.25D : 0.0D);
        GlStateManager.pushMatrix();
        double sneakOffset = 0.0;
        if (player.isSneaking()) {
            sneakOffset = -0.05;
        }

        GlStateManager.translate(0.0D, sneakOffset, player.isSneaking() ? -0.01725D : 0.0D);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.375D, sneak + 0.55D, 0.0D);
        if (rotations[1][0] != 0.0F) {
            GlStateManager.rotate(rotations[1][0] * 57.295776F, 1.0F, 0.0F, 0.0F);
        }
        if (rotations[1][1] != 0.0F) {
            GlStateManager.rotate(rotations[1][1] * 57.295776F, 0.0F, 1.0F, 0.0F);
        }
        if (rotations[1][2] != 0.0F) {
            GlStateManager.rotate(-rotations[1][2] * 57.295776F, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.glBegin(3);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, -0.5D, 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.375D, sneak + 0.55D, 0.0D);
        if (rotations[2][0] != 0.0F) {
            GlStateManager.rotate(rotations[2][0] * 57.295776F, 1.0F, 0.0F, 0.0F);
        }
        if (rotations[2][1] != 0.0F) {
            GlStateManager.rotate(rotations[2][1] * 57.295776F, 0.0F, 1.0F, 0.0F);
        }
        if (rotations[2][2] != 0.0F) {
            GlStateManager.rotate(-rotations[2][2] * 57.295776F, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.glBegin(3);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, -0.5D, 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, sneak + 0.55D, 0.0D);
        if (rotations[0][0] != 0.0F) {
            GlStateManager.rotate(rotations[0][0] * 57.295776F, 1.0F, 0.0F, 0.0F);
        }

        GlStateManager.glBegin(3);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.3D, 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.rotate(player.isSneaking() ? 25.0F : 0.0F, 1.0F, 0.0F, 0.0F);

        if (player.isSneaking()) {
            sneakOffset = -0.16175D;
        }

        GlStateManager.translate(0.0D, sneakOffset, player.isSneaking() ? -0.48025D : 0.0D);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, sneak, 0.0D);
        GlStateManager.glBegin(3);
        GL11.glVertex3d(-0.125D, 0.0D, 0.0D);
        GL11.glVertex3d(0.125D, 0.0D, 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, sneak, 0.0D);
        GlStateManager.glBegin(3);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.55D, 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, sneak + 0.55D, 0.0D);
        GlStateManager.glBegin(3);
        GL11.glVertex3d(-0.375D, 0.0D, 0.0D);
        GL11.glVertex3d(0.375D, 0.0D, 0.0D);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }
}
