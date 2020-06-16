package ch.swisssmp.zvieriplausch;

import ch.swisssmp.utils.ConfigurationSection;
import ch.swisssmp.utils.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDataContainer {

    private final ZvieriArena arena;
    private final int LEVELS = 5;

    private File dataFile;
    private YamlConfiguration yamlConfiguration;
    private ConfigurationSection arenaSection;

    private List<List<String>> unlockedPlayers;
    private List<List<String>> highscorePlayers;
    private int[] highscoreScores;

    private PlayerDataContainer(ZvieriArena arena){
        this.arena = arena;

        dataFile = new File(arena.getWorld().getWorldFolder(), "plugindata/ZvieriGame/arenen.yml");
        if(dataFile == null) {
            Bukkit.getLogger().info(ZvieriGamePlugin.getPrefix() + " Konnte Spielerdaten nicht laden (dataFile null)");
            return;
        }
        yamlConfiguration = YamlConfiguration.loadConfiguration(dataFile);
        if(yamlConfiguration == null) {
            Bukkit.getLogger().info(ZvieriGamePlugin.getPrefix() + " Konnte Spielerdaten nicht laden (yaml null)");
            return;
        }
        arenaSection = getArenaSection();
        if(arenaSection == null) {
            Bukkit.getLogger().info(ZvieriGamePlugin.getPrefix() + " Konnte Spielerdaten nicht laden (arenaSection null)");
            return;
        }
        unlockedPlayers = new ArrayList<>();
        highscorePlayers = new ArrayList<>();
        highscoreScores = new int[LEVELS];
        readUnlockedPlayers();
        readHighscorePlayers();
        readHighscoreScores();
    }

    public static PlayerDataContainer initialize(ZvieriArena arena){
        return new PlayerDataContainer(arena);
    }

    public List<String> getUnlockedPlayers(int level){
        try {
            return unlockedPlayers.get(level - 2);
        } catch (IndexOutOfBoundsException e){
            return new ArrayList<String>();
        }
    }

    public List<String> getHighscorePlayers(int level){
        try {
            return highscorePlayers.get(level - 1);
        } catch (IndexOutOfBoundsException e){
            return new ArrayList<String>();
        }
    }

    public int getHighscoreScore(int level){
        try {
            return highscoreScores[level - 1];
        } catch (IndexOutOfBoundsException e){
            return 0;
        }
    }

    private ConfigurationSection getArenaSection(){
        ConfigurationSection arenaSection;
        if (yamlConfiguration.contains("arenen")) {
            ConfigurationSection arenenSection = yamlConfiguration.getConfigurationSection("arenen");
            for (String key : arenenSection.getKeys(false)) {
                arenaSection = arenenSection.getConfigurationSection(key);
                if (UUID.fromString(arenaSection.getString("id")).equals(arena.getId())) {
                    return arenaSection;
                }
            }
        }
        return null;
    }

    private void readUnlockedPlayers(){
        ConfigurationSection unlockedLevelsSection = arenaSection.getConfigurationSection("unlockedLevels");
        if (unlockedLevelsSection == null) return;
        for(int i = 1; i < LEVELS; i++) {
            if (unlockedLevelsSection.getStringList("level_" + (i+1)) == null){
                List<String> puffer = new ArrayList<>();
                puffer.add("");
//                unlockedPlayers.add(new ArrayList<>(LEVELS));
                unlockedPlayers.add(puffer);
                continue;
            }
            List<String> playersList = unlockedLevelsSection.getStringList("level_" + (i+1));
            unlockedPlayers.add(playersList);
        }
    }

    private void readHighscoreScores(){
        for(int i = 1; i <= LEVELS; i++) {
            int highscore;
            try {
                highscore = arenaSection.getConfigurationSection("highscores").getConfigurationSection("level_" + i).getInt("score");
            } catch (NullPointerException e) {
                highscore = 0;
            }
            highscoreScores[i-1] = highscore;
        }
    }

    private void readHighscorePlayers() {
        for(int i = 1; i <= LEVELS; i++) {
            List<String> players = new ArrayList<String>();
            try {
                players = arenaSection.getConfigurationSection("highscores").getConfigurationSection("level_" + (i)).getStringList("players");
            } catch (NullPointerException e) {
                players.add("");
            }
            highscorePlayers.add(players);
        }
    }

    public void updateLevelUnlocks(int level, List<String> playerIds){
        try {
            for (String id : playerIds) {
                if (!unlockedPlayers.get(level-1).contains(id)) unlockedPlayers.get(level-1).add(id);
            }
        } catch (IndexOutOfBoundsException e){
            unlockedPlayers.add(playerIds);
        }
        save();
    }

    public void updateHighscore(int level, int score, List<Player> participants) {
        List<String> participantsStrings = new ArrayList<>();
        for(Player player : participants){
            participantsStrings.add(player.getName());
        }
        try {
            highscorePlayers.remove(level - 1);
        } catch (IndexOutOfBoundsException e){}
        try {
            highscorePlayers.add(level - 1, participantsStrings);
        } catch (IndexOutOfBoundsException e){
            highscorePlayers.add(participantsStrings);
        }
        highscoreScores[level-1] = score;
        save();
    }

    private void save(){
        ConfigurationSection unlockedLevelsSection = arenaSection.getConfigurationSection("unlockedLevels");
        if(unlockedLevelsSection == null){
            unlockedLevelsSection = arenaSection.createSection("unlockedLevels");
        }
        for(int i = 0; i < unlockedPlayers.size(); i++) {
            unlockedLevelsSection.set("level_" + (i+2), unlockedPlayers.get(i));
        }

        ConfigurationSection highscoreSection = arenaSection.getConfigurationSection("highscores");
        if(highscoreSection == null){
            highscoreSection = arenaSection.createSection("highscores");
        }
        for(int i = 0; i < highscorePlayers.size(); i++){
            ConfigurationSection levelSection = highscoreSection.getConfigurationSection("level_" + (i+1));
            if(levelSection == null) {
                levelSection = highscoreSection.createSection("level_" + (i + 1));
            }
            try {
                levelSection.set("score", highscoreScores[i]);
                levelSection.set("players", highscorePlayers.get(i));
            } catch (IndexOutOfBoundsException e){
                continue;
            }
        }
        yamlConfiguration.save(dataFile);
    }
}