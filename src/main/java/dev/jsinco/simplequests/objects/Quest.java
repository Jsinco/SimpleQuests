package dev.jsinco.simplequests.objects;

import dev.jsinco.simplequests.enums.QuestAction;
import dev.jsinco.simplequests.enums.RewardType;
import dev.jsinco.simplequests.hooks.VaultHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public class Quest {

    private final String category;
    private final String id;
    private final String name;
    private final String type;
    private final QuestAction questAction;
    private final int amount;
    @Nullable private final RewardType rewardType;
    @Nullable private final Object rewardValue;
    @Nullable private final Material menuItem;

    public Quest(String category, String id, String name, String type, QuestAction questAction, int amount, @Nullable RewardType rewardType, @Nullable Object rewardValue, @Nullable Material menuItem) {
        this.category = category;
        this.id = id;
        this.name = name;
        this.type = type;
        this.questAction = questAction;
        this.amount = amount;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.menuItem = menuItem;
    }

    public Quest(String category, String id, String name, String type, QuestAction questAction, int amount, @Nullable String rewardTypeStr, @Nullable Object rewardValue, @Nullable String menuItemStr) {
        this.category = category;
        this.id = id;
        this.name = name;
        this.type = type;
        this.questAction = questAction;
        this.amount = amount;
        this.rewardValue = rewardValue;
        this.rewardType = rewardTypeStr != null ? RewardType.valueOf(rewardTypeStr) : null;
        this.menuItem = menuItemStr != null ? Material.getMaterial(menuItemStr) : null;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public QuestAction getQuestAction() {
        return questAction;
    }

    public int getAmount() {
        return amount;
    }

    @Nullable
    public RewardType getRewardType() {
        return rewardType;
    }

    @Nullable
    public Object getRewardValue() {
        return rewardValue;
    }

    @Nullable
    public Material getMenuItem() {
        return menuItem;
    }

    public void executeReward(OfflinePlayer player) {
        if (rewardType == null || rewardValue == null) return;
        switch (rewardType) {
            case MONEY -> {
                final Economy econ = VaultHook.getEconomy();
                econ.depositPlayer(player, (double) rewardValue);
            }
            case COMMAND -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardValue.toString().replace("%player%", player.getName()));
        }
    }
}