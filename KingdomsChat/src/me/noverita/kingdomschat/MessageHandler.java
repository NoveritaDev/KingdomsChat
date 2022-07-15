package me.noverita.kingdomschat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class MessageHandler implements Listener {
    //TODO: Audible ping noise
    //TODO: Block specific players
    //TODO: Block messages containing particular words
    private static MessageHandler instance = null;

    private final Map<String,Channel> channels;
    private final Map<String, Set<Player>> players;
    private final Map<Player, String> playerActiveChannel;
    private final Set<Player> emphasizedPlayers;
    private final Map<String, ChatColor> tags;
    private final Map<String, ChatColor> channelIndicatorColour;
    private final Map<String, String> aliases;
    private final Map<Player, Player> lastMessageSeen;
    private final Set<Player> helpers;
    private String defaultChannel;
    private final ChatColor actionColor;
    private final ChatColor defaultColor;
    private final Map<Player, ChatColor> nameColour;

    private Random rng;
    private Map<Player, Long> lastRoll;

    public static MessageHandler getInstance() {
        if (instance == null) {
            instance = new MessageHandler();
        }
        return instance;
    }

    public Collection<String> getChannels() {
        return aliases.keySet();
    }

    private MessageHandler() {
        channels = new HashMap<>();
        players = new HashMap<>();
        playerActiveChannel = new HashMap<>();
        emphasizedPlayers = new HashSet<>();
        tags = new HashMap<>();
        channelIndicatorColour = new HashMap<>();
        lastMessageSeen = new HashMap<>();
        aliases = new HashMap<>();
        defaultChannel = null;
        helpers = new HashSet<>();

        actionColor = ChatColor.GOLD;
        defaultColor = ChatColor.WHITE;

        tags.put("a",ChatColor.RED);
        tags.put("h",ChatColor.GREEN);
        tags.put("s",ChatColor.AQUA);
        tags.put("c",ChatColor.YELLOW);
        tags.put("sar",ChatColor.ITALIC);

        rng = new Random();
        lastRoll = new HashMap<>();
        nameColour = new HashMap<>();
    }

    public void addChannel(Channel channel) {
        Bukkit.getLogger().info(channel.toString());
        channels.put(channel.getName(), channel);
        aliases.put(channel.getShortName(), channel.getName());
        players.put(channel.getName(), new HashSet<>());
        channelIndicatorColour.put(channel.getName(), channel.getIndicatorColour());

        if (defaultChannel == null || channel.getIsDefault()) {
            defaultChannel = channel.getName();
        }
    }
    public void toggleEmphasis(Player p) {
        if (emphasizedPlayers.contains(p)) {
            p.sendMessage("Your messages are no longer emphasized.");
            emphasizedPlayers.remove(p);
        } else {
            p.sendMessage("Your messages are now emphasized.");
            emphasizedPlayers.add(p);
        }
    }

    public boolean changeChannel(Player player, String channelName) {
        String name = channelName.toLowerCase();
        if (channels.containsKey(name)) {
            if (player.hasPermission(channels.get(name).getPermission())) {
                playerActiveChannel.put(player, name);
                players.get(name).add(player);
                player.sendMessage("Changed to " + name + " channel.");
                return true;
            }
        } else if (aliases.containsKey(name)) {
            String fullName = aliases.get(name);
            if (player.hasPermission(channels.get(fullName).getPermission())) {
                playerActiveChannel.put(player, fullName);
                players.get(fullName).add(player);
                player.sendMessage("Changed to " + fullName + " channel.");
                return true;
            }
        }
        return false;
    }

    public boolean leaveChannel(Player player, String channelName) {
        String name = channelName.toLowerCase();
        if (channels.containsKey(name)) {
            if (!channels.get(name).getIsMandatory()) {
                String activeChannel = playerActiveChannel.get(player);
                if (activeChannel.equals(name)) {
                    playerActiveChannel.put(player, defaultChannel);
                }
                boolean b = players.get(name).remove(player);
                if (b) {
                    player.sendMessage("Left " + name + " channel.");
                }
                return b;
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "That channel is mandatory, you cannot leave it.");
            }
            return true;
        } else if (aliases.containsKey(name)) {
            if (!channels.get(aliases.get(name)).getIsMandatory()) {
                String fullName = aliases.get(name);
                String activeChannel = playerActiveChannel.get(player);
                if (activeChannel.equals(fullName)) {
                    playerActiveChannel.put(player, defaultChannel);
                }
                boolean b = players.get(fullName).remove(player);
                if (b) {
                    player.sendMessage("Left " + fullName + " channel.");
                }
                return b;
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "That channel is mandatory, you cannot leave it.");
            }
            return true;
        }
        return false;
    }

    public void reloadHandler() {
        for (Player p: Bukkit.getOnlinePlayers()) {
            boolean flag = true;
            File playerFile = new File("./plugins/KingdomsChat/players/" + p.getUniqueId() + ".csv");
            if (playerFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(playerFile))) {
                    String line = br.readLine();
                    playerActiveChannel.put(p, line);
                    line = br.readLine();
                    String[] otherChannels = line.split(",");
                    for (String c: otherChannels) {
                        players.get(c).add(p);
                    }
                    line = br.readLine();
                    flag = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (flag) {
                for (Channel c : channels.values()) {
                    if (c.getIsAutomatic()) {
                        players.get(c.getName()).add(p);
                    }
                }
                changeChannel(p, defaultChannel);
                if (p.hasPermission("kingdomschat.readsendonly")) {
                    helpers.add(p);
                }
            }
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        boolean flag = true;
        File playerFile = new File("./plugins/KingdomsChat/players/" + p.getUniqueId() + ".csv");
        if (playerFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(playerFile))) {
                String line = br.readLine();
                playerActiveChannel.put(p, line);
                line = br.readLine();
                String[] otherChannels = line.split(",");
                for (String c: otherChannels) {
                    players.get(c).add(p);
                }
                flag = false;
                line = br.readLine();
                if (line != null) {
                    try {
                        ChatColor color = ChatColor.of(line);
                        setNameColor(p, color);
                    } catch (IllegalArgumentException e) {
                        setNameColor(p, ChatColor.WHITE);
                    }
                } else {
                    nameColour.put(p, ChatColor.WHITE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (flag) {
            for (Channel c : channels.values()) {
                if (c.getIsAutomatic()) {
                    players.get(c.getName()).add(p);
                }
            }
            changeChannel(p, defaultChannel);
            if (p.hasPermission("kingdomschat.readsendonly")) {
                helpers.add(p);
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        StringBuilder fileOutput = new StringBuilder();

        fileOutput.append(playerActiveChannel.get(event.getPlayer()));
        fileOutput.append('\n');

        boolean hasChannel = false;
        for (Channel c: channels.values()) {
            boolean flag = leaveChannel(event.getPlayer(), c.getName());
            if (flag) {
                if (hasChannel) {
                    fileOutput.append(',');
                    fileOutput.append(c.getName());
                } else {
                    hasChannel = true;
                    fileOutput.append(c.getName());
                }
            }
        }

        fileOutput.append('\n');
        ChatColor color = nameColour.remove(event.getPlayer());
        fileOutput.append(Objects.requireNonNullElse(color, ChatColor.WHITE).getName());

        lastRoll.remove(event.getPlayer());

        File playerFile = new File("./plugins/KingdomsChat/players/" + event.getPlayer().getUniqueId() + ".csv");
        try {
            if (!playerFile.exists()) {
                playerFile.createNewFile();
            }
            PrintWriter writer = new PrintWriter(playerFile, StandardCharsets.UTF_8);
            writer.print(fileOutput);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        helpers.remove(event.getPlayer());
        emphasizedPlayers.remove(event.getPlayer());
        lastMessageSeen.remove(event.getPlayer());
    }

    void setNameColor(Player p, ChatColor cc) {
        nameColour.put(p, cc);
    }

    @EventHandler
    private void onChatMessage(AsyncPlayerChatEvent event) {
        Player origin = event.getPlayer();
        boolean emphasis = emphasizedPlayers.contains(origin);
        String channel = playerActiveChannel.get(origin);
        String processed;

        // Add some hover text over the sender's name.
        StringBuilder htb = new StringBuilder();
        if (!origin.getDisplayName().equals(origin.getName())) {
            htb.append("Name: ");
            htb.append(origin.getDisplayName());
            htb.append('\n');
        }
        htb.append("IGN: ");
        htb.append(origin.getName());
        HoverEvent nameHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(htb.toString()));
        ClickEvent nameClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/characters get _ (" + origin.getName() + ")");

        TextComponent nameComponent;
        Channel channel1 = channels.get(channel);
        if (channel1 != null && channel1.getIsOOC()) {
            //processed = getShownName(origin) + ": " + event.getMessage();
            processed = event.getMessage();
            nameComponent = new TextComponent(getShownName(origin) + ": ");
            nameComponent.setColor(nameColour.get(event.getPlayer()));
        } else if (event.getMessage().startsWith("[!]")) {
            nameComponent = new TextComponent("[!] ");
            processed = preprocess('*' + event.getMessage().substring(3).strip() + '*', emphasis);
            nameComponent.setColor(ChatColor.GOLD);
        } else {
            //processed = getShownName(origin) + ": " + preprocess(event.getMessage(), emphasis);
            processed =  preprocess(event.getMessage(), emphasis);
            nameComponent = new TextComponent(getShownName(origin) + ": ");
            nameComponent.setColor(nameColour.get(event.getPlayer()));
        }
        nameComponent.setHoverEvent(nameHover);
        nameComponent.setClickEvent(nameClick);

        List<BaseComponent> components = new ArrayList<>();
        components.add(nameComponent);
        components.add(new TextComponent(processed));

        send(
                //processed,
                components,
                origin.getLocation(),
                channel,
                origin
        );
        event.setCancelled(true);
    }

    // OOC Channels are set to show the players IGN, whereas the in character ones show their character name.
    private String getShownName(Player player) {
        if (channels.get(playerActiveChannel.get(player)).getIsOOC()) {
            return player.getName();
        }
        return player.getDisplayName();
    }

    private String preprocess(String message, boolean isImportant) {

        // Handle tone tagging
        int index = message.length();
        String tagString = "";
        ChatColor baseColor = defaultColor;
        for (String tag: tags.keySet()) {
            if (message.strip().endsWith("/"+tag)) {
                baseColor = tags.get(tag);
                index = message.length() - tag.length() - 1;
                tagString = ChatColor.DARK_GRAY + "/" + tag;
                break;
            }
        }
        if (index > 0) {
            message = message.substring(0,index).strip();
        }

        // Apply some preprocessing for strings with quotation marks. It's easiest to just modify the message, then
        // do action stuff after.
        if (message.contains("\"")) {
            StringBuilder stepOne = new StringBuilder();

            if (!message.startsWith("\"") && !message.startsWith("*")) {
                stepOne.append('*');
            }

            boolean quotesOpen = message.startsWith("\"");
            for (int i = 0; i < message.length(); ++i) {
                char temp = message.charAt(i);
                if (temp == '"' && i != 0 && i != message.length() - 1) {
                    quotesOpen = !quotesOpen;
                    if (quotesOpen) {
                        stepOne.append("* \"");
                    } else {
                        stepOne.append("\" *");
                    }
                } else {
                    stepOne.append(temp);
                }
            }

            if (!message.endsWith("\"")) {
                stepOne.append('*');
            }

            message = stepOne.toString();
        }

        // Start action processing
        StringBuilder sb = new StringBuilder();

        sb.append(baseColor);
        if (isImportant) {
            sb.append(ChatColor.BOLD);
        }

        boolean isAction = false;
        boolean isUnderlined = false;
        for (int i = 0; i < message.length(); ++i) {
            char temp = message.charAt(i);
            if (temp == '*') {
                isAction = !isAction;
                if (isAction) {
                    sb.append(actionColor);
                    sb.append(ChatColor.ITALIC);
                    sb.append(temp);
                } else {
                    sb.append(temp);
                    sb.append(ChatColor.RESET);
                    if (isImportant) {
                        sb.append(ChatColor.BOLD);
                    }
                    sb.append(baseColor);
                    if (isUnderlined) {
                        sb.append(ChatColor.UNDERLINE);
                    }
                }
            } else if (temp == '_') {
                isUnderlined = !isUnderlined;
                if (isUnderlined) {
                    sb.append(ChatColor.UNDERLINE);
                } else {
                    sb.append(ChatColor.RESET);
                    if (isImportant) {
                        sb.append(ChatColor.BOLD);
                    }
                    if (isAction) {
                        sb.append(actionColor);
                    }
                    sb.append(baseColor);
                }
            } else {
                sb.append(temp);
            }
        }

        sb.append(ChatColor.RESET); // Specifically to cancel underlining and italics

        // Separating the tone tag helps with some of the processing. This just adds it back.
        sb.append(' ');
        sb.append(tagString);

        return ChatColor.RESET + sb.toString();
    }

    public String getChannel(Player p) {
        return playerActiveChannel.get(p);
    }

    public void send(String message, Location location, String channelName, Player sender) {
        Channel channel = channels.get(channelName);

        String prefix = channelIndicatorColour.get(channel.getName())
                + "["
                + channel.getShortName().toUpperCase()
                + "] "
                + ChatColor.RESET;

        message = prefix + message;

        double rangeLimit = channel.getRangeLimit();
        if (rangeLimit > 0) {
            for (Player p: players.get(channelName)) {
                if (p.getWorld().equals(sender.getWorld()) && p.getLocation().distance(location) <= rangeLimit) {
                    Player lastSeen = lastMessageSeen.get(p);
                    if ((sender != lastSeen && emphasizedPlayers.contains(lastSeen))
                            || (sender != lastSeen && emphasizedPlayers.contains(sender))) {
                        p.sendMessage("");
                    }
                    lastMessageSeen.put(p, sender);
                    p.sendMessage(message);
                }
            }
        } else {
            for (Player p: players.get(channelName)) {
                if (channel.getIsSendOnly()) {
                    sender.sendMessage(message);
                    for (Player helper : helpers) {
                        helper.sendMessage(message);
                    }
                } else {
                    Player lastSeen = lastMessageSeen.get(p);
                    if (sender != lastSeen && (emphasizedPlayers.contains(lastSeen))
                            || (sender != lastSeen && emphasizedPlayers.contains(sender))) {
                        p.sendMessage("");
                    }
                    lastMessageSeen.put(p, sender);
                    p.sendMessage(message);
                }
            }
        }
    }

    public void send(List<BaseComponent> components, Location location, String channelName, Player sender) {
        Channel channel = channels.get(channelName);

        String prefix = channel.getIndicatorColour() + "[" + channel.getShortName().toUpperCase() + "] " + ChatColor.RESET;
        TextComponent message = new TextComponent(prefix);
        for (BaseComponent bc: components) {
            message.addExtra(bc);
        }
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ch " + channelName));

        Bukkit.getLogger().info(message.toPlainText());

        double rangeLimit = channel.getRangeLimit();
        if (rangeLimit > 0) {
            for (Player p: players.get(channelName)) {
                if (p.getWorld().equals(sender.getWorld()) && p.getLocation().distance(location) <= rangeLimit) {
                    Player lastSeen = lastMessageSeen.get(p);
                    if ((sender != lastSeen && emphasizedPlayers.contains(lastSeen))
                            || (sender != lastSeen && emphasizedPlayers.contains(sender))) {
                        p.sendMessage("");
                    }
                    lastMessageSeen.put(p, sender);
                    p.spigot().sendMessage(message);
                }
            }
        } else {
            for (Player p: players.get(channelName)) {
                if (channel.getIsSendOnly()) {
                    if (!helpers.contains(sender)) {
                        sender.spigot().sendMessage(message);
                    }
                    for (Player helper : helpers) {
                        helper.spigot().sendMessage(message);
                    }
                } else {
                    Player lastSeen = lastMessageSeen.get(p);
                    if (sender != lastSeen && (emphasizedPlayers.contains(lastSeen))
                            || (sender != lastSeen && emphasizedPlayers.contains(sender))) {
                        p.sendMessage("");
                    }
                    lastMessageSeen.put(p, sender);
                    p.spigot().sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        String message = event.getDeathMessage();
        event.setDeathMessage(null);
        World w = event.getEntity().getWorld();
        Location l = event.getEntity().getLocation();

        for (Player p: Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(w)) {
                if (p.getLocation().distance(l) < 20) {
                    p.sendMessage(ChatColor.RED + message);
                }
            }
        }
        Bukkit.getLogger().info(String.format("%s %s", l, message));
    }

    @EventHandler
    public void onRoll(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            Long lastRollTime = lastRoll.get(player);
            long millis = System.currentTimeMillis();
            if (lastRollTime == null || millis - lastRollTime > 1000) {
                String channel = getChannel(player);
                MessageHandler.getInstance().send(
                        player.getDisplayName() + " rolled a " + org.bukkit.ChatColor.BOLD + (rng.nextInt(20) + 1) + org.bukkit.ChatColor.RESET + " on a d20.",
                        player.getLocation(),
                        channel,
                        player
                );
                lastRoll.put(player, millis);
            } else {
                player.sendMessage("Rolling is on cooldown.");
            }
        }
    }
}
