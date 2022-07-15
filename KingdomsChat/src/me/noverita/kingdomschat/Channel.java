package me.noverita.kingdomschat;

import net.md_5.bungee.api.ChatColor;

public class Channel {
    private double distance;
    private String permission;
    private String name;
    private String shortName;
    private ChatColor colour;
    private boolean isDefault;
    private boolean isMandatory;
    private boolean isAutomatic;
    private boolean isOOC;
    private boolean isSendOnly;

    public Channel() {
        this.distance = -1;
        this.permission = "kingdomschat.channel.default";
        this.name = "default";
        this.shortName = "D";
        this.colour = ChatColor.LIGHT_PURPLE;
        isDefault = false;
        isMandatory = false;
        isAutomatic = false;
        isOOC = false;
        isSendOnly = false;
    }

    public double getRangeLimit() {
        return distance;
    }

    public void setRangeLimit(double newDistance) {
        distance = newDistance;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String newPermission) {
        permission = newPermission;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String newName) {
        shortName = newName;
    }

    public ChatColor getIndicatorColour() {
        return colour;
    }

    public void setIndicatorColour(ChatColor newColour) {
        colour = newColour;
    }

    public void setIsDefault(boolean value) {
        isDefault = value;
        isAutomatic = isAutomatic || value;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsMandatory(boolean value) {
        isMandatory = value;
        isAutomatic = isAutomatic || value;
    }

    public boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsAutomatic(boolean value) {
        isAutomatic = value || isMandatory || isDefault;
    }

    public boolean getIsAutomatic() {
        return isAutomatic;
    }

    public void setIsOOC(boolean value) {
        isOOC = value;
    }

    public boolean getIsOOC() {
        return isOOC;
    }

    public void setIsSendOnly(boolean value) {
        isSendOnly = value;
    }

    public boolean getIsSendOnly() {
        return isSendOnly;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "distance=" + distance +
                ", permission='" + permission + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", colour=" + colour +
                ", isDefault=" + isDefault +
                ", isMandatory=" + isMandatory +
                ", isAutomatic=" + isAutomatic +
                ", isOOC=" + isOOC +
                ", isSendOnly=" + isSendOnly +
                '}';
    }
}
