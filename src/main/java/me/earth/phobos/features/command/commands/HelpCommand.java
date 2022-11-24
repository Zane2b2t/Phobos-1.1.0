package me.earth.phobos.features.command.commands;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.command.Command;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("commands");
    }

    @Override
    public void execute(String[] commands) {
        sendMessage("You can use following commands: ");
        for(Command command : Phobos.commandManager.getCommands()) {
            sendMessage(Phobos.commandManager.getPrefix() + command.getName());
        }
    }
}
