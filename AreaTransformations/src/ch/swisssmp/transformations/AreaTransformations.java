package ch.swisssmp.transformations;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class AreaTransformations extends JavaPlugin implements Listener{
	private static Logger logger;
	protected static PluginDescriptionFile pdfFile;
	protected static AreaTransformations plugin;
	public static WorldEditPlugin worldEditPlugin;
	protected static boolean debug = false;
	
	@Override
	public void onEnable() {
		plugin = this;
		pdfFile = getDescription();
		logger = Logger.getLogger("Minecraft");
		
		this.getCommand("transformation").setExecutor(new PlayerCommand());
		Bukkit.getPluginManager().registerEvents(this, this);
		
		Plugin worldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if(worldEdit instanceof WorldEditPlugin){
			worldEditPlugin = (WorldEditPlugin) worldEdit;
		}
		else{
			new NullPointerException("WorldEdit missing");
		}
		
		TransformationArea.loadTransformations();
		
		logger.info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((JavaPlugin)this);
		PluginDescriptionFile pdfFile = getDescription();
		logger.info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
    public static void info(String info){
    	if(debug){
        	logger.info(info);
    	}
    }
}
