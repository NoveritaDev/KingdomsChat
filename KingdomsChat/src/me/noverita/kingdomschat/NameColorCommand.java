package me.noverita.kingdomschat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class NameColorCommand implements CommandExecutor, TabCompleter {
    private final Set<ChatColor> colours = new HashSet<>(
            Arrays.asList(
                    ChatColor.AQUA,
                    //ChatColor.BLACK,
                    ChatColor.BLUE,
                    //ChatColor.DARK_AQUA,
                    //ChatColor.DARK_BLUE,
                    //ChatColor.DARK_GRAY,
                    //ChatColor.DARK_GREEN,
                    //ChatColor.DARK_PURPLE,
                    //ChatColor.DARK_RED,
                    ChatColor.GOLD,
                    ChatColor.GRAY,
                    ChatColor.GREEN,
                    ChatColor.LIGHT_PURPLE,
                    ChatColor.RED,
                    ChatColor.WHITE,
                    ChatColor.YELLOW
            )
    );

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            if (strings.length == 1) {
                try {
                    ChatColor color = ChatColor.of(strings[0]);
                    if (colours.contains(color)) {
                        MessageHandler.getInstance().setNameColor((Player) commandSender, color);
                    } else {
                        commandSender.sendMessage("You do not have permission to use that color.");
                    }
                } catch (IllegalArgumentException e) {
                    commandSender.sendMessage("That is not a valid colour.");
                }
            } else {
                commandSender.sendMessage("Please specify a colour.");
            }
        } else {
            commandSender.sendMessage("This command can only be done by a player.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            List<String> temp = new ArrayList<>();
            String text = strings[0].toLowerCase();
            for (ChatColor cc : colours) {
                if (cc.getName().startsWith(text)) {
                    temp.add(cc.getName());
                }
            }
            return temp;
        }

        return new ArrayList<>();
    }
}
