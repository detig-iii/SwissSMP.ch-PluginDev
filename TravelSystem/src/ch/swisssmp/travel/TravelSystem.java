package ch.swisssmp.travel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class TravelSystem extends JavaPlugin {

	protected static PluginDescriptionFile pdfFile;
	protected static TravelSystem plugin;
	
	protected static boolean debug = false;
	
	@Override
	public void onEnable() {
		plugin = this;
		pdfFile = getDescription();
		
		Bukkit.getPluginManager().registerEvents(new EventListener(), plugin);
		if(Bukkit.getPluginManager().getPlugin("ResourcepackManager")!=null){
			Bukkit.getPluginManager().registerEvents(new ResourcepackListener(), this);
		}

		Bukkit.getPluginCommand("travelstation").setExecutor(new TravelStationCommand());
		Bukkit.getPluginCommand("travelstations").setExecutor(new TravelStationsCommand());
		Bukkit.getPluginCommand("travelworld").setExecutor(new TravelWorldCommand());
		
		for(World world : Bukkit.getWorlds()){
			TravelStations.load(world);
		}
		
		Bukkit.getLogger().info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
		PluginDescriptionFile pdfFile = getDescription();
		Bukkit.getLogger().info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	public static String getPrefix(){
		return "["+ChatColor.LIGHT_PURPLE+"Fernreisen"+ChatColor.RESET+"] ";
	}
	
	public static TravelSystem getInstance(){
		return plugin;
	}
}
