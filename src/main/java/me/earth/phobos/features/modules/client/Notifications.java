package me.earth.phobos.features.modules.client;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.manager.FileManager;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Notifications extends Module {

    public Setting<Boolean> totemPops = register(new Setting("TotemPops", false));
    public Setting<Integer> delay = register(new Setting("Delay", 2000, 0, 5000, v -> totemPops.getValue(), "Delays messages."));
    public Setting<Boolean> clearOnLogout = register(new Setting("LogoutClear", false));
    public Setting<Boolean> moduleMessage = register(new Setting("ModuleMessage", false));
    public Setting<Boolean> list = register(new Setting("List", false, v -> moduleMessage.getValue()));
    private Setting<Boolean> readfile = register(new Setting("LoadFile", false, v -> moduleMessage.getValue()));
    public Setting<Boolean> visualRange = register(new Setting("VisualRange", false));
    public Setting<Boolean> leaving = register(new Setting("Leaving", false, v -> visualRange.getValue()));
    public Setting<Boolean> crash = register(new Setting("Crash", false));

    private List<String> knownPlayers = new ArrayList<>();
    private static List<String> modules = new ArrayList();
    private static final String fileName = "phobos/util/ModuleMessage_List.txt";
    private Timer timer = new Timer();
    public Timer totemAnnounce = new Timer();
    private boolean check;
    private static Notifications INSTANCE = new Notifications();

    public Notifications() {
        super("Notifications", "Sends Messages.", Category.CLIENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onLoad() {
        check = true;
        loadFile();
        check = false;
    }

    @Override
    public void onEnable() {
        this.knownPlayers = new ArrayList<>();
        if(!check) {
            loadFile();
        }
    }

    @Override
    public void onUpdate() {
        if(readfile.getValue()) {
            if(!check) {
                Command.sendMessage("Loading File...");
                timer.reset();
                loadFile();
            }
            check = true;
        }

        if(check && timer.passedMs(750)) {
            readfile.setValue(false);
            check = false;
        }

        if(visualRange.getValue()) {
            List<String> tickPlayerList = new ArrayList<>();
            for (Entity entity : mc.world.playerEntities) {
                tickPlayerList.add(entity.getName());
            }
            if (tickPlayerList.size() > 0) {
                for (String playerName : tickPlayerList) {
                    if (playerName.equals(mc.player.getName())) {
                        continue;
                    }
                    if (!knownPlayers.contains(playerName)) {
                        knownPlayers.add(playerName);
                        if (Phobos.friendManager.isFriend(playerName)) {
                            Command.sendMessage("Player " + TextUtil.GREEN + playerName + TextUtil.RESET + " entered your visual range!");
                        } else {
                            Command.sendMessage("Player " + TextUtil.RED + playerName + TextUtil.RESET + " entered your visual range!");
                        }
                        return;
                    }
                }
            }

            if (knownPlayers.size() > 0) {
                for (String playerName : knownPlayers) {
                    if (!tickPlayerList.contains(playerName)) {
                        knownPlayers.remove(playerName);
                        if (leaving.getValue()) {
                            if (Phobos.friendManager.isFriend(playerName)) {
                                Command.sendMessage("Player " + TextUtil.GREEN + playerName + TextUtil.RESET + " left your visual range!");
                            } else {
                                Command.sendMessage("Player " + TextUtil.RED + playerName + TextUtil.RESET + " left your visual range!");
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    public void loadFile() {
        List<String> fileInput = FileManager.readTextFileAllLines(fileName);
        Iterator<String> i = fileInput.iterator();
        modules.clear();
        while (i.hasNext()) {
            String s = i.next();
            if (!s.replaceAll("\\s", "").isEmpty()) {
                modules.add(s);
            }
        }
    }

    @SubscribeEvent
    public void onToggleModule(ClientEvent event) {
        if(!moduleMessage.getValue()) {
            return;
        }

        if(event.getStage() == 0) {
            Module module = (Module)event.getFeature();
            if(!module.equals(this) && (modules.contains(module.getDisplayName()) || !list.getValue())) {
                Command.sendMessage(TextUtil.RED + module.getDisplayName() + " disabled.");
            }
        }

        if(event.getStage() == 1) {
            Module module = (Module)event.getFeature();
            if(modules.contains(module.getDisplayName()) || !list.getValue()) {
                Command.sendMessage(TextUtil.GREEN + module.getDisplayName() + " enabled.");
            }
        }
    }

    public static Notifications getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Notifications();
        }
        return INSTANCE;
    }

    public static void displayCrash(Exception e) {
        Command.sendMessage(TextUtil.RED + "Exception caught: " + e.getMessage());
    }
}
