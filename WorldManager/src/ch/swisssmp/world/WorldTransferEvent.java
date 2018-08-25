package ch.swisssmp.world;

import java.io.File;

import org.bukkit.event.Event;

public abstract class WorldTransferEvent extends Event{
    protected final String worldName;
    protected final File packedDirectory;
    
    protected WorldTransferEvent(String worldName, File packedDirectory){
    	this.worldName = worldName;
    	this.packedDirectory = packedDirectory;
    }
    
    public String getWorldName(){
    	return this.worldName;
    }
    
    public File getPackedDirectory(){
    	return this.packedDirectory;
    }
}