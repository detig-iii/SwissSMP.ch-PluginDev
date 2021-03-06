package ch.swisssmp.commandscheduler;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ch.swisssmp.utils.ConfigurationSection;
import ch.swisssmp.utils.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;

public class CommandScheduler extends JavaPlugin implements Listener{
	private static Logger logger;
	protected static PluginDescriptionFile pdfFile;
	protected static CommandScheduler plugin;
	
	private BukkitTask refreshTask;
	protected static boolean disable = false;
	private static int timeoutSeconds = 5;
	
	@Override
	public void onEnable() {
		plugin = this;
		pdfFile = getDescription();
		logger = Logger.getLogger("Minecraft");
		
		PlayerCommand playerCommand = new PlayerCommand();
		this.getCommand("commandscheduler").setExecutor(playerCommand);
		refresh();
		
		Bukkit.getPluginManager().registerEvents(this, this);
		logger.info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((JavaPlugin)this);
		if(this.refreshTask!=null) this.refreshTask.cancel();
		PluginDescriptionFile pdfFile = getDescription();
		logger.info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event){
		Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			public void run(){
				runScheduledPlayerJoinCommands(event.getPlayer());
			}
		}, 2L);
	}
	
	private void runScheduledPlayerJoinCommands(Player player){
		if(!player.isOnline()) return;
		HTTPRequest request = DataSource.getResponse(plugin, "get_player_join.php", new String[]{
				"player_uuid="+player.getUniqueId()
		});
		request.onFinish(()->{
			runScheduledPlayerJoinCommands(player, request.getYamlResponse());
		});
	}
	
	private void runScheduledPlayerJoinCommands(Player player, YamlConfiguration yamlConfiguration){
		if(yamlConfiguration==null || !yamlConfiguration.contains("commands")) return;
		for(String command : yamlConfiguration.getStringList("commands")){
			Bukkit.getLogger().info("Performing command "+command);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		}
	}
	
	private void refresh(){
		try{
			if(!disable){
				runCommands();
			}
			else{
				Bukkit.getLogger().info("Skipped scheduled command execution. Retrying in "+timeoutSeconds+" seconds.");
			}
		}
		catch(Exception e){
			Bukkit.getLogger().info("Error executing remote commands! Continuing anyways, but please do check the log!");
			e.printStackTrace();
		}
		this.refreshTask = Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			public void run(){
				refresh();
			}
		}, 20L*timeoutSeconds);
	}
	
	public static void schedulePlayerJoin(UUID player_uuid, String... commands){
		String[] args = new String[commands.length+1];
		args[0] = "player_uuid="+player_uuid.toString();
		for(int i = 0; i < commands.length; i++){
			args[i+1] = commands[i];
		}
		DataSource.getResponse(plugin, "schedule_player_join.php", args);
	}
	
	public static void removePlayerJoin(int schedule_id){
		DataSource.getResponse(plugin, "remove_player_join.php", new String[]{
				"schedule_id="+schedule_id	
			});
	}
	
	public static void runCommands(){
		HTTPRequest request = DataSource.getResponse(plugin, "commands.php");
		request.onFinish(()->{
			runCommands(request.getYamlResponse());
		});
	}
	
	private static void runCommands(YamlConfiguration yamlConfiguration){
		if(yamlConfiguration==null){
			Bukkit.getLogger().info("Commands couldn't be fetched. Trying again in "+timeoutSeconds+" seconds.");
			return;
		}
		for(String key : yamlConfiguration.getKeys(false)){
			ConfigurationSection commandSection = yamlConfiguration.getConfigurationSection(key);
			String commandLine = commandSection.getString("command");
			String sender = commandSection.getString("sender");
			CommandSender commandSender;
			if(sender.equals("console")){
				commandSender = Bukkit.getConsoleSender();
			}
			else{
				Player player = Bukkit.getPlayer(UUID.fromString(sender));
				if(player==null) continue;
				commandSender = player;
			}
			try{
				Bukkit.dispatchCommand(commandSender, commandLine);
			}
			catch(Exception e){
				Bukkit.getLogger().info("Error executing command '"+commandLine+"' for "+sender);
				e.printStackTrace();
			}
		}
		if(yamlConfiguration.getKeys(false).size()>0){
			Bukkit.getLogger().info(yamlConfiguration.getKeys(false).size()+" commands executed.");
		}
	}
}
