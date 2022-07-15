package me.noverita.kingdomschat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

public class DiceRollCommand implements CommandExecutor {
    private Random rng;

    public DiceRollCommand() {
        rng = new Random();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > 0) {
            if (commandSender instanceof Player) {
                try {
                    String input = String.join("", strings).replace(" ", "").toLowerCase();
                    String[] sections = input.split("\\+");
                    int total = 0;
                    int numRolls = 0;
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < sections.length; ++j) {
                        String[] values = sections[j].split("d");
                        int numDice = Integer.parseInt(values[0]);
                        int typeDice = Integer.parseInt(values[1]);
                        sb.append("(");
                        numRolls += numDice;
                        if (numRolls > 20 || typeDice > 100) {
                            return false;
                        }
                        for (int i = 0; i < numDice; ++i) {
                            int temp = rng.nextInt(typeDice) + 1;
                            sb.append(temp);
                            if (i != numDice - 1) {
                                sb.append("+");
                            }
                            total += temp;
                        }
                        sb.append(")");
                        if (j != sections.length - 1) {
                            sb.append('+');
                        }
                    }

                    Player player = (Player) commandSender;
                    String channel = MessageHandler.getInstance().getChannel((Player) commandSender);

                    MessageHandler.getInstance().send(
                            player.getDisplayName() + " rolled " + ChatColor.BOLD + total + ChatColor.RESET + " on " + input + ": " + sb.toString(),
                            player.getLocation(),
                            channel,
                            player
                    );
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        /*if (strings.length == 1) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                String channel = MessageHandler.getInstance().getChannel((Player) commandSender);
                int roll = rng.nextInt(Integer.parseInt(strings[0])) + 1;

                MessageHandler.getInstance().send(
                        player.getDisplayName() + " rolled a " + ChatColor.BOLD + roll + ChatColor.RESET + " on a " + strings[0] + " sided die.",
                        player.getLocation(),
                        channel
                );
            }
            return true;
        } else if (strings.length == 2) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                String channel = MessageHandler.getInstance().getChannel((Player) commandSender);

                int numDice = Integer.parseInt(strings[0]);
                int typeDie = Integer.parseInt(strings[1]);

                StringBuilder sb = new StringBuilder();
                int[] rolls = new int[numDice];
                int total = 0;

                sb.append("(");
                for (int i = 0; i < numDice; ++i) {
                    int temp = rng.nextInt(typeDie) + 1;
                    sb.append(temp);
                    if (i != numDice - 1) {
                        sb.append("+");
                    }
                    rolls[i] = temp;
                    total += temp;
                }
                sb.append(")");

                MessageHandler.getInstance().send(
                        player.getDisplayName() + " rolled a total of " + ChatColor.BOLD + total + ChatColor.RESET + " on " + numDice + " " + typeDie + "-sided dice. " + sb.toString(),
                        player.getLocation(),
                        channel
                );
                return true;
            }
        }*/
        return false;
    }
}
