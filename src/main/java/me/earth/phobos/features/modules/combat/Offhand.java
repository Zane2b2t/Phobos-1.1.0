package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class Offhand extends Module {
    public Setting<Bind> offHandGapple = register(new Setting("Gapple", new Bind(-1)));
    public Setting<Float> gappleHealth = register(new Setting("G-Health", 6.0f, 0.1f, 36.0f));
    public Setting<Float> gappleHoleHealth = register(new Setting("G-H-Health", 4.0f, 0.1f, 36.0f));
    public Setting<Bind> offHandCrystal = register(new Setting("Crystal", new Bind(-1)));
    public Setting<Float> crystalHealth = register(new Setting("C-Health", 20.0f, 0.1f, 36.0f));
    public Setting<Float> crystalHoleHealth = register(new Setting("C-H-Health", 8.0f, 0.1f, 36.0f));
    public Setting<Float> cTargetDistance = register(new Setting("C-Distance", 10.0f, 1.0f, 20.0f));
    public Setting<Bind> obsidian = register(new Setting("Obsidian", new Bind(-1)));
    public Setting<Float> obsidianHealth = register(new Setting("O-Health", 18.0f, 0.1f, 36.0f));
    public Setting<Float> obsidianHoleHealth = register(new Setting("O-H-Health", 8.0f, 0.1f, 36.0f));
    public Setting<Bind> webBind = register(new Setting("Webs", new Bind(-1)));
    public Setting<Float> webHealth = register(new Setting("W-Health", 18.0f, 0.1f, 36.0f));
    public Setting<Float> webHoleHealth = register(new Setting("W-H-Health", 8.0f, 0.1f, 36.0f));
    public Setting<Boolean> holeCheck = register(new Setting("Hole-Check", true));
    public Setting<Boolean> crystalCheck = register(new Setting("Crystal-Check", false));
    public Setting<Integer> updates = register(new Setting("Updates", 1, 1, 2));
    public Mode mode = Mode.CRYSTALS;
    private int oldSlot = -1;
    private boolean swapToTotem = false;
    public Offhand() {
        super("Offhand", "Allows you to switch up your Offhand.", Category.COMBAT, true, false, false);
    }

    @Override
    public void onTick() {
        if (nullCheck() || updates.getValue() == 1) {
            return;
        }
        doOffhand();
    }

    @Override
    public void onUpdate() {
        if (nullCheck() || updates.getValue() == 2) {
            return;
        }
        doOffhand();
    }

    public void doOffhand() {
        if (mc.currentScreen instanceof GuiContainer)
            return;
        if (!shouldTotem()) {
            if (!(mc.player.getHeldItemOffhand() != ItemStack.EMPTY && isItemInOffhand())) {
                final int slot = getSlot() < 9 ? getSlot() + 36 : getSlot();
                if (getSlot() != -1) {
                    oldSlot = slot;
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                }
            }
        } else if (!(mc.player.getHeldItemOffhand() != ItemStack.EMPTY && mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)) {
            final int slot = getTotemSlot() < 9 ? getTotemSlot() + 36 : getTotemSlot();
            if (getTotemSlot() != -1) {
                mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, oldSlot, 0, ClickType.PICKUP, mc.player);
                oldSlot = -1;
            }
        }
    }

    private boolean nearPlayers() {
        return mode == Mode.CRYSTALS && mc.world.playerEntities.stream().noneMatch(e -> e != mc.player && !Phobos.friendManager.isFriend(e) && mc.player.getDistance(e) <= cTargetDistance.getValue());
    }
    private boolean isItemInOffhand() {
        switch (mode) {
            case GAPPLES:
                return mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;
            case CRYSTALS:
                return mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
            case OBSIDIAN:
                return mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).block == Blocks.OBSIDIAN;
            case WEBS:
                return mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).block == Blocks.WEB;
        }
        return false;
    }
    private boolean isHeldInMainHand() {
        switch (mode) {
            case GAPPLES:
                return mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE;
            case CRYSTALS:
                return mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
            case OBSIDIAN:
                return mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemMainhand().getItem()).block == Blocks.OBSIDIAN;
            case WEBS:
                return mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemMainhand().getItem()).block == Blocks.WEB;
        }
        return false;
    }

    private boolean shouldTotem() {
        if (isHeldInMainHand() || isSwapToTotem()) return true;
        if (holeCheck.getValue() && EntityUtil.isInHole(mc.player)) {
            return (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= getHoleHealth() || mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA || mc.player.fallDistance >= 3 || nearPlayers() || (crystalCheck.getValue() && isCrystalsAABBEmpty());
        }
        return (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= getHealth() || mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA || mc.player.fallDistance >= 3 || nearPlayers() || (crystalCheck.getValue() && isCrystalsAABBEmpty());
    }

    private boolean isNotEmpty(BlockPos pos) {
        return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().anyMatch(e -> e instanceof EntityEnderCrystal);
    }

    private float getHealth() {
        switch (mode) {
            case CRYSTALS:
                return crystalHealth.getValue();
            case GAPPLES:
                return gappleHealth.getValue();
            case OBSIDIAN:
                return obsidianHealth.getValue();
        }
        return webHealth.getValue();
    }

    private float getHoleHealth() {
        switch (mode) {
            case CRYSTALS:
                return crystalHoleHealth.getValue();
            case GAPPLES:
                return gappleHoleHealth.getValue();
            case OBSIDIAN:
                return obsidianHoleHealth.getValue();
        }
        return webHoleHealth.getValue();
    }

    private boolean isCrystalsAABBEmpty() {
        return isNotEmpty(mc.player.getPosition().add(1, 0, 0)) || isNotEmpty(mc.player.getPosition().add(-1, 0, 0)) || isNotEmpty(mc.player.getPosition().add(0, 0, 1)) || isNotEmpty(mc.player.getPosition().add(0, 0, -1)) || isNotEmpty(mc.player.getPosition());
    }

    int getStackSize() {
        int size = 0;
        if (shouldTotem()) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        } else if (mode == Mode.OBSIDIAN) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.OBSIDIAN) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        } else if (mode == Mode.WEBS) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.WEB) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        } else {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == (mode == Mode.CRYSTALS ? Items.END_CRYSTAL : Items.GOLDEN_APPLE)) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        }
        return size;
    }

    int getSlot() {
        int slot = -1;
        if (mode == Mode.OBSIDIAN) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.OBSIDIAN) {
                    slot = i;
                    break;
                }
            }
        } else if (mode == Mode.WEBS) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.WEB) {
                    slot = i;
                    break;
                }
            }
        } else {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == (mode == Mode.CRYSTALS ? Items.END_CRYSTAL : Items.GOLDEN_APPLE)) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    int getTotemSlot() {
        int totemSlot = -1;
        for (int i = 45; i > 0; i--) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        return totemSlot;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            if (offHandCrystal.getValue().getKey() == Keyboard.getEventKey()) {
                if (mode == Mode.CRYSTALS) {
                    setSwapToTotem(!isSwapToTotem());
                } else setSwapToTotem(false);
                setMode(Mode.CRYSTALS);
            }
            if (offHandGapple.getValue().getKey() == Keyboard.getEventKey()) {
                if (mode == Mode.GAPPLES) {
                    setSwapToTotem(!isSwapToTotem());
                } else setSwapToTotem(false);
                setMode(Mode.GAPPLES);
            }
            if (obsidian.getValue().getKey() == Keyboard.getEventKey()) {
                if (mode == Mode.OBSIDIAN) {
                    setSwapToTotem(!isSwapToTotem());
                } else setSwapToTotem(false);
                setMode(Mode.OBSIDIAN);
            }
            if (webBind.getValue().getKey() == Keyboard.getEventKey()) {
                if (mode == Mode.WEBS) {
                    setSwapToTotem(!isSwapToTotem());
                } else setSwapToTotem(false);
                setMode(Mode.WEBS);
            }
        }
    }


    @Override
    public String getDisplayInfo() {
        return String.valueOf(getStackSize());
    }

    @Override
    public String getDisplayName() {
        if (!shouldTotem()) {
            switch (mode) {
                case GAPPLES:
                    return "OffhandGapple";
                case WEBS:
                    return "OffhandWebs";
                case OBSIDIAN:
                    return "OffhandObby";
                default:
                    return "OffhandCrystal";
            }
        }
        return "AutoTotem";
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isSwapToTotem() {
        return swapToTotem;
    }

    public void setSwapToTotem(boolean swapToTotem) {
        this.swapToTotem = swapToTotem;
    }

    public enum Mode {
        CRYSTALS,
        GAPPLES,
        OBSIDIAN,
        WEBS
    }
}
