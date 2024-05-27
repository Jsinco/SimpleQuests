package dev.jsinco.simplequests;

import dev.jsinco.abstractjavafilelib.ConfigurationSection;
import dev.jsinco.abstractjavafilelib.schemas.SnakeYamlConfig;
import dev.jsinco.simplequests.enums.QuestAction;
import dev.jsinco.simplequests.enums.RewardType;
import dev.jsinco.simplequests.objects.ActiveQuest;
import dev.jsinco.simplequests.objects.Quest;
import dev.jsinco.simplequests.objects.QuestPlayer;
import dev.jsinco.simplequests.storage.DataManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class QuestManager {

    private static final ConcurrentLinkedQueue<Quest> quests = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<UUID, QuestPlayer> questPlayersCache = new ConcurrentHashMap<>();
    private static final DataManager dataManager = SimpleQuests.getDataManager();

    public static BukkitRunnable asyncCacheManager() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                for (QuestPlayer questPlayer : questPlayersCache.values()) {
                    dataManager.saveQuestPlayer(questPlayer);
                    if (questPlayer.getActiveQuests().isEmpty() || questPlayer.getPlayer() == null || !questPlayer.getPlayer().isOnline()) {
                        questPlayersCache.remove(questPlayer.getUuid());
                        Util.debugLog("Decaching QuestPlayer: " + questPlayer.getUuid());
                    }
                }
            }
        };
    }

    public static void loadQuests() { // async
        quests.clear();
        final SnakeYamlConfig questsFile = SimpleQuests.getQuestsFile();

        for (String category : questsFile.getKeys()) {
            final ConfigurationSection categorySection = questsFile.getConfigurationSection(category);
            for (String id : categorySection.getKeys()) {
                final ConfigurationSection questSection = categorySection.getConfigurationSection(id);
                final QuestAction questAction = QuestAction.valueOf(questSection.getString("action"));

                quests.add(new Quest(
                        category,
                        id,
                        questSection.getString("name"),
                        questSection.getString("type").toUpperCase(),
                        questAction,
                        questSection.getInt("amount"),
                        questSection.getString("reward.type"),
                        questSection.get("reward.value"),
                        questSection.getString("menu-item")
                ));
            }
        }
        Util.debugLog("Finished loading " + quests.size() + " quests");
    }

    @Nullable
    public static Quest getQuest(String category, String id) { // async?
        return quests.stream()
                .filter(quest -> quest.getCategory().equals(category) && quest.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    public static QuestPlayer getQuestPlayer(UUID uuid) {
        if (questPlayersCache.containsKey(uuid)) {
            return questPlayersCache.get(uuid);
        }

        final QuestPlayer questPlayer = dataManager.loadQuestPlayer(uuid);
        if (!questPlayer.getActiveQuests().isEmpty()) {
            cacheQuestPlayer(questPlayer);
        }
        return questPlayer;
    }

    public static void cacheQuestPlayer(QuestPlayer questPlayer) {
        if (!questPlayersCache.containsKey(questPlayer.getUuid())) {
            questPlayersCache.put(questPlayer.getUuid(), questPlayer);
            Util.debugLog("Cached QuestPlayer: " + questPlayer.getUuid());
        } else {
            Util.debugLog("QuestPlayer already cached: " + questPlayer.getUuid());
        }
    }

    public static List<QuestPlayer> getQuestPlayers() {
        return List.copyOf(questPlayersCache.values());
    }

    public static List<Quest> getQuests() {
        return List.copyOf(quests);
    }
}