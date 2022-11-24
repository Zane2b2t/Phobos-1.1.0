package me.earth.phobos.mixin.mixins;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.misc.BetterPortals;
import me.earth.phobos.features.modules.movement.Flight;
import me.earth.phobos.features.modules.movement.Phase;
import me.earth.phobos.features.modules.movement.Sprint;
import me.earth.phobos.features.modules.player.Freecam;
import me.earth.phobos.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerSP.class, priority = 9998)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP(Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_) {
        super(p_i47378_2_, p_i47378_3_.getGameProfile());
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreenHook(EntityPlayerSP entityPlayerSP) {
        if(!(BetterPortals.getInstance().isOn() && BetterPortals.getInstance().portalChat.getValue())) {
            entityPlayerSP.closeScreen();
        }
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void displayGuiScreenHook(Minecraft mc, GuiScreen screen) {
        if(!(BetterPortals.getInstance().isOn() && BetterPortals.getInstance().portalChat.getValue())) {
            mc.displayGuiScreen(screen);
        }
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setSprinting(Z)V", ordinal = 2))
    public void onLivingUpdate(EntityPlayerSP entityPlayerSP, boolean sprinting) {
        if(Sprint.getInstance().isOn() && Sprint.getInstance().mode.getValue() == Sprint.Mode.RAGE && (Util.mc.player.movementInput.moveForward != 0.0f || Util.mc.player.movementInput.moveStrafe != 0.0f)) {
            entityPlayerSP.setSprinting(true);
        } else {
            entityPlayerSP.setSprinting(sprinting);
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void pushOutOfBlocksHook(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
    if((Phase.getInstance().isOn() && Phase.getInstance().type.getValue() != Phase.PacketFlyMode.NONE) || Freecam.getInstance().isOn() || (Flight.getInstance().isOn() && Flight.getInstance().mode.getValue() == Flight.Mode.PACKET && Flight.getInstance().better.getValue() && Flight.getInstance().phase.getValue())) {
            info.setReturnValue(false);
        }
    }
	
	@Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void preMotion(CallbackInfo info) {
		UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(0);
		MinecraftForge.EVENT_BUS.post(event);
    }
    
	@Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void postMotion(CallbackInfo info) {
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(1);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = "Lnet/minecraft/client/entity/EntityPlayerSP;setServerBrand(Ljava/lang/String;)V", at = @At("HEAD"))
    public void getBrand(String brand, CallbackInfo callbackInfo) {
        if(Phobos.serverManager != null) {
            Phobos.serverManager.setServerBrand(brand);
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType moverType, double x, double y, double z) {
        final MoveEvent event = new MoveEvent(0, moverType, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
        if(!event.isCanceled()) {
            super.move(event.getType(), event.getX(), event.getY(), event.getZ());
        }
    }
}
