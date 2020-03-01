package ch.swisssmp.utils.nbt;

public class NBTTagFloat extends NBTNumber {
	
	net.minecraft.server.v1_15_R1.NBTTagFloat value;
	
	protected NBTTagFloat(net.minecraft.server.v1_15_R1.NBTTagFloat value){
		super(value);
		this.value = value;
	}

	@Override
	protected net.minecraft.server.v1_15_R1.NBTTagFloat asNMS() {
		return value;
	}
}
