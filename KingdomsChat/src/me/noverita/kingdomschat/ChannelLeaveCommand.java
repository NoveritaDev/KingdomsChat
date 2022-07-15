package me.noverita.kingdomschat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelLeaveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1 && commandSender instanceof Player) {
            return MessageHandler.getInstance().leaveChannel((Player) commandSender, strings[0]);
        }
        return false;
    }
}
