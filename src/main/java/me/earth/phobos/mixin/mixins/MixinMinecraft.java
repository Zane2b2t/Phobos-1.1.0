package me.earth.phobos.mixin.mixins;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.features.modules.player.MultiTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.crash.CrashReport;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    /*@Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void displayGuiScreenHook(GuiScreen guiScreenIn, CallbackInfo info) {
        GuiEvent event = new GuiEvent(0, guiScreenIn);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }*/

    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;sync(I)V"))
    public void syncHook(int maxFps) {
        if (Managers.getInstance().betterFrames.getValue()) {
            Display.sync(Managers.getInstance().betterFPS.getValue());
        } else {
            Display.sync(maxFps);
        }
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"))
    public void displayCrashReportHook(Minecraft minecraft, CrashReport crashReport) {
        unload();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdownHook(CallbackInfo info) {
        unload();
    }

    private void unload() {
        System.out.println("Shutting down: saving configuration");
        Phobos.onUnload();
        System.out.println("Configuration saved.");
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    private boolean isHandActiveWrapper(EntityPlayerSP playerSP) {
        return !MultiTask.getInstance().isOn() && playerSP.isHandActive();
    }

    @Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", ordinal = 0), require = 1)
    private boolean isHittingBlockHook(PlayerControllerMP playerControllerMP) {
        return !MultiTask.getInstance().isOn() && playerControllerMP.getIsHittingBlock();
    }

    /*@Shadow
    private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
        return null;
    }

    @Inject(method = "Lnet/minecraft/client/Minecraft;setWindowIcon()V", at = @At("HEAD"), cancellable = true)
    public void preSetWindowIcon(CallbackInfo info) {
        try {
            BufferedImage img = ImageIO.read(Phobos.class.getResourceAsStream("/phoboslogo.png"));
            List<ByteBuffer> sizes = new ArrayList<>();
            int w = img.getWidth();
            do  {
                BufferedImage tmp = new BufferedImage(w, w, img.getType());
                tmp.createGraphics().drawImage(img, 0, 0, w, w, null);
                sizes.add(this.convertImageToBuffer(tmp));
                w >>= 1;
            } while (w >= 8);
            Display.setIcon(sizes.toArray(new ByteBuffer[0]));
            info.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer convertImageToBuffer(BufferedImage bufferedimage) throws IOException {
        int[] color = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * color.length);

        for (int i : color) {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }*/
}
