package org.btuk.converter.plugin.utils.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.parser.JSONParser;

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
     * @param base64 The base64 encoded value of the "textures"
     * @return The player head item stack with the custom player skin
     */
    public static ItemStack fromBase64(String id, String base64) throws Exception {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
        String rawJson = new String(Base64.getDecoder().decode(base64));
        if(!rawJson.contains("textures.minecraft.net")) throw new Exception("Expected host 'textures.minecraft.net', skipping");

        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        PlayerProfile customPlayerProfile = Bukkit.createProfile(id.isEmpty() ? UUID.randomUUID() : UUID.fromString(id));
        customPlayerProfile.setProperty(new ProfileProperty("textures", base64));

        skullMeta.setPlayerProfile(customPlayerProfile);
        skullItem.setItemMeta(skullMeta);

        return  skullItem;

    }
}
