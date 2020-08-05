package ch.swisssmp.city;

import ch.swisssmp.utils.JsonUtil;
import ch.swisssmp.utils.URLEncoder;
import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Techtree {
    private final String id;
    private String name;
    private List<String> description;
    private String addonDetailUrl;
    private List<CityLevel> levels;
    private Set<AddonType> addonTypes;

    private Techtree(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public List<String> getDescription(){
        return description;
    }

    public String getAddonDetailUrl(){
        return addonDetailUrl;
    }

    public List<CityLevel> getLevels(){
        return Collections.unmodifiableList(levels);
    }

    public CityLevel getLevel(int index){
        return index>=0 && index<levels.size() ? levels.get(index) : null;
    }

    public Optional<CityLevel> getLevel(String id){
        return levels.stream().filter(l->l.getId().equalsIgnoreCase(id)).findAny();
    }

    public boolean hasLevel(String levelId){
        return getLevel(levelId).isPresent();
    }

    public int getLevelIndex(String levelId){
        CityLevel level = getLevel(levelId).orElse(null);
        return level!=null ? getLevelIndex(level) : -1;
    }

    public int getLevelIndex(CityLevel level){
        return levels.indexOf(level);
    }

    public Set<AddonType> getAddonTypes(){return Collections.unmodifiableSet(addonTypes);}

    public Set<AddonType> getAddonTypes(CityLevel level){
        int index = this.levels.indexOf(level);
        if(index<0) return Collections.emptySet();
        return addonTypes.stream().filter(t->t.getCityLevel()==index).collect(Collectors.toSet());}

    public Optional<AddonType> getAddonType(String key){
        return addonTypes.stream().filter(a->a.getAddonId().equalsIgnoreCase(key) || a.getName().equalsIgnoreCase(key)).findAny();
    }

    public Optional<AddonType> findAddonType(String key){
        Optional<AddonType> exactResult = getAddonType(key);
        if(exactResult.isPresent()) return exactResult;
        for(AddonType addonType : addonTypes){
            for(String synonym : addonType.getSynonyms()){
                if(synonym.toLowerCase().contains(key.toLowerCase())) return Optional.of(addonType);
            }
        }
        return Optional.empty();
    }

    public void updateAddonState(Addon addon){
        if(addon.getState()==AddonState.ACCEPTED || addon.getState()==AddonState.ACTIVATED || addon.getState()==AddonState.BLOCKED) return;
        City city = addon.getCity();
        AddonType type = addon.getType();
        CityLevel requiredLevel = this.getLevel(type.getCityLevel());
        if(requiredLevel==null){
            addon.setAddonState(AddonState.UNAVAILABLE, AddonStateReason.NONE);
            return;
        }
        if(!city.hasLevel(requiredLevel)){
            addon.setAddonState(AddonState.UNAVAILABLE, AddonStateReason.CITY_LEVEL);
            return;
        }

        String[] requiredAddons = type.getRequiredAddons();
        boolean requiredAddonsPresent = true;
        for(String requiredAddonId : requiredAddons){
            Addon requiredAddon = city.getAddon(requiredAddonId).orElse(null);
            if(requiredAddon!=null && (requiredAddon.getState()==AddonState.ACCEPTED || requiredAddon.getState()==AddonState.ACTIVATED)){
                continue;
            }

            requiredAddonsPresent = false;
            break;
        }

        if(!requiredAddonsPresent){
            addon.setAddonState(AddonState.UNAVAILABLE, AddonStateReason.REQUIRED_ADDONS);
            return;
        }

        addon.setAddonState(AddonState.AVAILABLE);
    }

    public void loadIcons(){
        for(AddonType addonType : addonTypes){
            LivemapInterface.updateAddonIcon(addonType);
        }
    }

    public void reload(){
        reload(null);
    }

    public void reload(Consumer<Boolean> callback){
        HTTPRequest request = DataSource.getResponse(CitySystemPlugin.getInstance(), CitySystemUrl.GET_TECHTREE, new String[]{
                "techtree_id="+ URLEncoder.encode(id)
        });
        request.onFinish(()->{
            JsonObject json = request.getJsonResponse();
            boolean success = json!=null && JsonUtil.getBool("success", json);
            String message = json!=null ? JsonUtil.getString("message", json) : null;
            if(message!=null){
                Bukkit.getLogger().info(CitySystemPlugin.getPrefix()+" "+message);
            }
            if(success){
                loadData(json.getAsJsonObject("techtree"));
            }
            if(callback!=null) callback.accept(success);
        });
    }

    private void loadData(JsonObject json){
        this.name = JsonUtil.getString("name", json);
        this.description = JsonUtil.getStringList("description", json);
        this.addonDetailUrl = JsonUtil.getString("addon_detail_url", json);
        List<CityLevel> levels = new ArrayList<>();
        if(json.has("levels")){
            for(JsonElement element : json.getAsJsonArray("levels")){
                if(!element.isJsonObject()) continue;
                CityLevel level = CityLevel.load(this, element.getAsJsonObject()).orElse(null);
                if(level==null) continue;
                levels.add(level);
            }
        }
        this.levels = levels;
        Set<AddonType> addons = new HashSet<>();
        JsonArray addonsSection = json.has("addons") ? json.getAsJsonArray("addons") : null;
        if(addonsSection!=null){
            for(JsonElement element : addonsSection){
                if(!element.isJsonObject()) continue;
                JsonObject addonSection = element.getAsJsonObject();
                AddonType addonType = AddonType.load(addonSection).orElse(null);
                if(addonType==null){
                    Bukkit.getLogger().warning(CitySystemPlugin.getPrefix()+" Konnte AddonType vom Techtree "+id+" nicht laden:\n"+element.toString());
                    continue;
                }
                addons.add(addonType);
            }
        }
        this.addonTypes = addons;

        loadIcons();
    }

    protected static Optional<Techtree> load(JsonObject json){
        if(json==null) return Optional.empty();
        String id = JsonUtil.getString("techtree_id", json);
        if(id==null) return Optional.empty();
        Techtree techtree = new Techtree(id);
        techtree.loadData(json);
        return Optional.of(techtree);
    }
}
