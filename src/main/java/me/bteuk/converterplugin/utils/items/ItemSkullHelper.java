package me.bteuk.converterplugin.utils.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

/**
 * Special helper class to convert custom skull items inside post-processing json files to in-game skull ItemStack with custom textures
 * @author DavixDevelop
 */
public class ItemSkullHelper {

    private static JSONParser textureParser = new JSONParser();

    /**
     * Create a ItemStack skull from the username of a player
     * @param username Username of a player
     * @return Skull ItemStack containing the head of an offline player
     */
    public static ItemStack fromUsername(String username){
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(username));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    /**
     * Create a ItemStack skull from the UUID of a player
     * @param uuid UUID of the player
     * @return Skull ItemStack containing the head of an offline player
     */
    public static ItemStack fromUUID(UUID uuid){
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    /**
     * Create a ItemStack skull from an encoded base64 string that contains the JSON properties,
     * ex. the url to the skin/texture
     * @param id The ID of the player
     * @param base64 The base64 encoded value of the "texture"
     * @return The player head item stack with the custom player skin
     * @throws ParseException
     * @throws IOException
     */
    public static ItemStack fromBase64(String id, String base64) throws ParseException, IOException {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
        String rawJson = new String(Base64.getDecoder().decode(base64));
        //If the keys aren't in double quotes in the raw JSON, fix the string
        if(!rawJson.startsWith("{\""))
            rawJson = rawJson.replaceAll("([\\{,])\\s*([A-Za-z0-9_]+)\\s*:", "$1\"$2\":");

        StringReader reader = new StringReader(rawJson);
        JSONObject playerTexturesJsonObject = (JSONObject) textureParser.parse(reader);

        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();

        JSONObject playerTexturesJson = (JSONObject) playerTexturesJsonObject.getOrDefault("textures", new JSONObject());
        if(!playerTexturesJson.isEmpty()){
            JSONObject playerTextureSkin = (JSONObject) playerTexturesJson.getOrDefault("SKIN", new JSONObject());
            if(!playerTextureSkin.isEmpty()){
                String skinUrl = (String) playerTextureSkin.getOrDefault("url", "");

                if(!skinUrl.isEmpty()) {
                    PlayerProfile customPlayerProfile = Bukkit.createProfile(id.isEmpty() ? UUID.randomUUID() : UUID.fromString(id));
                    //customPlayerProfile.setProperty(new ProfileProperty("textures", base64));
                    PlayerTextures playerTextures = customPlayerProfile.getTextures();

                    String skinModel = "";

                    JSONObject playerTextureMeta = (JSONObject) playerTextureSkin.getOrDefault("metadata", new JSONObject());
                    if (!playerTextureMeta.isEmpty())
                        skinModel = (String) playerTextureMeta.getOrDefault("model", "");


                    if (!skinModel.isEmpty())
                        playerTextures.setSkin(new URL(skinUrl), (skinModel.equals("classic")) ? PlayerTextures.SkinModel.CLASSIC : PlayerTextures.SkinModel.SLIM);
                    else
                        playerTextures.setSkin(new URL(skinUrl));

                    customPlayerProfile.setTextures(playerTextures);
                    customPlayerProfile.complete();

                    skullMeta.setPlayerProfile(customPlayerProfile);
                }
            }
        }

        skullItem.setItemMeta(skullMeta);

        return  skullItem;

    }
}
