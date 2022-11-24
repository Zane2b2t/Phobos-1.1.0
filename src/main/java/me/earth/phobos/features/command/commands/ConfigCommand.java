package me.earth.phobos.features.command.commands;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.util.TextUtil;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", new String[]{"<save/load>"});
    }

    @Override
    public void execute(String[] commands) {
        if(commands.length == 1) {
            sendMessage("You`ll find the config files in your gameProfile directory under phobos/config");
            return;
        }

        if(commands.length >= 2) {
            switch(commands[0]) {
                case "save" :
                    Phobos.configManager.saveConfig();
                    sendMessage(TextUtil.GREEN + "Config has been saved.");
                    break;
                case "load" :
                    Phobos.configManager.loadConfig();
                    sendMessage(TextUtil.GREEN + "Config has been loaded.");
                    break;
                default :
                    sendMessage(TextUtil.RED + "Not a valid command... Possible usage: <save/load>");
                    break;
            }
        }
    }
}
