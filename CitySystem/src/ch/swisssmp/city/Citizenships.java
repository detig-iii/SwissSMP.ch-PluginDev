package ch.swisssmp.city;

import ch.swisssmp.webcore.DataSource;
import ch.swisssmp.webcore.HTTPRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

class Citizenships {
    private static final Set<Citizenship> citizenships = new HashSet<>();

    protected static Optional<Citizenship> getCitizenship(UUID cityId, String name){
        return citizenships.stream().filter(c->c.getCityId().equals(cityId) && c.getName().equalsIgnoreCase(name)).findAny();
    }

    protected static Optional<Citizenship> getCitizenship(UUID cityId, UUID playerUid){
        return citizenships.stream().filter(c->c.getCityId().equals(cityId) && c.getUniqueId().equals(playerUid)).findAny();
    }

    protected static Collection<Citizenship> getPlayerCitizenships(UUID playerUid){
        return citizenships.stream().filter(c->c.getUniqueId().equals(playerUid)).collect(Collectors.toList());
    }

    protected static Collection<Citizenship> getAllCitizenships(UUID cityId){
        return citizenships.stream().filter(c->c.getCityId().equals(cityId)).collect(Collectors.toList());
    }

    protected static void add(Citizenship citizenship){
        citizenships.remove(citizenship);
    }

    protected static void remove(Citizenship citizenship){
        citizenships.remove(citizenship);
    }

    protected static void loadAll(){
        loadAll((Runnable) null);
    }

    protected static void loadAll(Runnable callback){
        HTTPRequest request = DataSource.getResponse(CitySystemPlugin.getInstance(), "get_citizenships.php", new String[]{

        });

        request.onFinish(()->{
            loadAll(request.getJsonResponse());
            if(callback!=null) callback.run();
        });
    }

    private static void loadAll(JsonObject json){
        citizenships.clear();
        if(json==null || !json.has("citizenships")){
            return;
        }
        Collection<Citizenship> citizenships = new Stack<>();
        for(JsonElement element : json.getAsJsonArray("citizenships")){
            if(!element.isJsonObject()) continue;
            Citizenship citizenship = Citizenship.get(element.getAsJsonObject()).orElse(null);
            if(citizenship==null) continue;
            citizenships.add(citizenship);
        }

        Citizenships.citizenships.addAll(citizenships);
    }

    protected static void unloadAll(){
        citizenships.clear();
    }
}