package ch.swisssmp.city.ceremony.promotion;

import ch.swisssmp.city.City;
import ch.swisssmp.city.CitySystemPlugin;
import ch.swisssmp.customitems.CustomItemBuilder;
import ch.swisssmp.customitems.CustomItems;
import ch.swisssmp.utils.JsonUtil;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PromotionCeremonyData {

    private String levelId;

    private final int promotionPlayercount;
    private final int promotionHaybalecount;

    private final int climaxExplosionCycles;
    private final int fireworkCycles;

    private List<ItemStack> tribute;

    private PromotionCeremonyData(JsonObject json){
        levelId = JsonUtil.getString("level_id", json);
        promotionPlayercount = JsonUtil.getInt("promotion_playercount", json);
        promotionHaybalecount = JsonUtil.getInt("promotion_haybalecount", json);
        climaxExplosionCycles = JsonUtil.getInt("climax_explosion_cycles", json);
        fireworkCycles = JsonUtil.getInt("firework_cycles", json);
        JsonArray itemsSection = json.getAsJsonArray("items");
        if(itemsSection!=null){
            for(JsonElement element : itemsSection){
                if(!element.isJsonObject()) continue;
                try{
                    ItemStack itemStack = getItem(element.getAsJsonObject());
                    if(itemStack==null) continue;
                    tribute.add(itemStack);
                }
                catch(Exception e){
                    continue;
                }
            }
        }
    }

    public String getLevelId(){
        return levelId;
    }

    private static ItemStack getItem(JsonObject json){
        CustomItemBuilder itemBuilder = CustomItems.getCustomItemBuilder(json);
        if(itemBuilder==null) return null;
        return itemBuilder.build();
    }

    public int getPromotionPlayercount(){
        return promotionPlayercount;
    }

    public int getPromotionHaybalecount(){
        return promotionHaybalecount;
    }

    public int getClimaxExplosionCycles(){
        return climaxExplosionCycles;
    }

    public int getFireworkCycles(){
        return fireworkCycles;
    }

    public String getLevelId(){
        return levelId;
    }

    public List<ItemStack> getTribute(){
        return tribute;
    }

    public static PromotionCeremonyData load(City city){
        List<PromotionCeremonyData> dataContainer = new ArrayList<>();
        String levelId = city.getLevelId();

        HTTPRequest request = DataSource.getResponse(CitySystemPlugin.getInstance(), "get_promotion_ceremony.php", new String[]{
                "level_id="+levelId});
        request.onFinish(() ->{
            JsonObject json = request.getJsonResponse();
            if(json == null || !json.get("success").getAsBoolean()){
                Bukkit.getLogger().info(CitySystemPlugin.getPrefix() + " Couldn't load promotionceremony data for level: " + levelId);
                return;
            }
            dataContainer.add(new PromotionCeremonyData(json));
            return;
        });
        return dataContainer.get(0);
    }
}
