package me.noverita.kingdomschat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {
    public static void loadConfig() throws IOException {
        Bukkit.getLogger().info("Loading config");
        File channelConfig = new File("./plugins/KingdomsChat/channels.cfg");
        Files.createDirectories(Paths.get("./plugins/KingdomsChat"));
        Files.createDirectories(Paths.get("./plugins/KingdomsChat/players"));
        boolean noConfig = channelConfig.createNewFile();
        if (noConfig) {
            Bukkit.getLogger().info("No config found, creating default.");
            createDefault(channelConfig);
        }

        List<Channel> channels = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(channelConfig))) {
            Bukkit.getLogger().info("Reading config file");
            String line = br.readLine();
            Channel current = null;
            while (line != null) {
                line = line.toLowerCase();
                String[] values = line.split(":");
                String key = values[0].strip();
                switch (key) {
                    case "channel":
                        if (current != null) {
                            channels.add(current);
                        }
                        current = new Channel();
                        current.setName(values[1].strip());
                        break;
                    case "range":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setRangeLimit(Double.parseDouble(values[1].strip()));
                        break;
                    case "permission":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setPermission(values[1].strip());
                        break;
                    case "short":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setShortName(values[1].strip());
                        break;
                    case "color":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setIndicatorColour(ChatColor.of(values[1].strip()));
                        break;
                    case "ooc":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setIsOOC(Boolean.parseBoolean(values[1].strip()));
                        break;
                    case "default":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setIsDefault(Boolean.parseBoolean(values[1].strip()));
                        break;
                    case "mandatory":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setIsMandatory(Boolean.parseBoolean(values[1].strip()));
                        break;
                    case "automatic":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setIsAutomatic(Boolean.parseBoolean(values[1].strip()));
                        break;
                    case "sendonly":
                        if (current == null) {
                            Bukkit.getLogger().info("Malformed config.");
                            return;
                        }
                        current.setIsSendOnly(Boolean.parseBoolean(values[1].strip()));
                        break;
                }
                line = br.readLine();
            }
            if (current != null) {
                channels.add(current);
            }
        }

        for (Channel c: channels) {
            MessageHandler.getInstance().addChannel(c);
        }
    }

    private static void createDefault(File channelConfig) throws IOException {
        FileWriter output = new FileWriter(channelConfig.getAbsolutePath());
        String defaultConfig = "Channel: Talk\\r\\nPermission: kingdomschat.channel.talk\\r\\nColor: DARK_GREEN\\r\\nRange: 20\\r\\nShort: t\\r\\ndefault: true\\r\\nmandatory: true\\r\\nautomatic: true\\r\\nChannel: Shout\\r\\nPermission: kingdomschat.channel.shout\\r\\nColor: DARK_RED\\r\\nRange: 50\\r\\nShort: s\\r\\nmandatory: true\\r\\nautomatic: true\\r\\nChannel: Whisper\\r\\nPermission: kingdomschat.channel.whisper\\r\\nColor: DARK_BLUE\\r\\nRange: 3\\r\\nShort: w\\r\\nmandatory: true\\r\\nautomatic: true\\r\\nChannel: OOC\\r\\nPermission: kingdomschat.channel.ooc\\r\\nColor: DARK_GRAY\\r\\nRange: -1\\r\\nShort: ooc\\r\\nooc: true\\r\\nautomatic: true\\r\\nChannel: Local OOC\\r\\nPermission: kingdomschat.channel.looc\\r\\nColor: DARK_GRAY\\r\\nRange: 20\\r\\nShort: looc\\r\\nooc: true\\r\\nautomatic: true\\r\\nChannel: Staff\\r\\nPermission: kingdomschat.channel.staff\\r\\nColor: RED\\r\\nRange: -1\\r\\nShort: staff\\r\\nChannel: Help\\r\\nPermission: kingdomschat.channel.help\\r\\nColor: RED\\r\\nRange: -1\\r\\nShort: h\\r\\nsendonly: true";
        output.write(defaultConfig);
        output.close();
    }
}
