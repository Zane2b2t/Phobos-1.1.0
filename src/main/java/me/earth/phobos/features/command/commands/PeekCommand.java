package me.earth.phobos.features.command.commands;

import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.misc.ToolTips;
import me.earth.phobos.util.TextUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class PeekCommand extends Command {

    public PeekCommand() {
        super("peek", new String[]{"<player>"});
    }

    @Override
    public void execute(String[] commands) {
        if(commands.length == 1) {
            ItemStack stack = mc.player.getHeldItemMainhand();
            if(stack != null && stack.getItem() instanceof ItemShulkerBox) {
                ToolTips.displayInv(stack, null);
            } else {
                Command.sendMessage(TextUtil.RED + "You need to hold a Shulker in your mainhand.");
                return;
            }
        }

        if(commands.length > 1) {
            if(ToolTips.getInstance().isOn() && ToolTips.getInstance().shulkerSpy.getValue()) {
                 for(Map.Entry<EntityPlayer, ItemStack> entry : ToolTips.getInstance().spiedPlayers.entrySet()) {
                     if(entry.getKey().getName().equalsIgnoreCase(commands[0])) {
                         ItemStack stack = entry.getValue();
                         ToolTips.displayInv(stack, entry.getKey().getName());
                         break;
                     }
                 }
            } else {
                Command.sendMessage(TextUtil.RED + "You need to turn on Tooltips - ShulkerSpy");
            }
        }
    }
}
