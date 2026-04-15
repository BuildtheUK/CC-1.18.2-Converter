package me.bteuk.converterplugin.utils.entities;

import me.bteuk.converterplugin.utils.Utils;
import me.bteuk.converterplugin.utils.inventory.InventoryHelper;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.*;
import org.json.simple.JSONObject;

/**
 * Helper class for prepping various types of Minecarts
 */
public class MinecartHelper {

    /**
     * Set the properties that all Minecart use
     * @param minecart Minecart to set
     * @param props JSON object containing the basic properties of minecarts
     */
    public static void setCommonMinecartProps(Minecart minecart, JSONObject props){
        Utils.prepEntity(minecart, props);

        if(props.containsKey("display_tile") && Utils.ensureInt(props, "display_tile") == 1){
            String displayTileBlock = ((String)props.get("display_tile_block")).substring(10);
            Material displayTileMaterial = Material.getMaterial(displayTileBlock.toUpperCase());
            if(props.containsKey("display_tile_block_states")){
                JSONObject displayBlockStates = (JSONObject) props.get("display_tile_block_states");
                String _blockData = "minecraft:" + displayTileBlock + "[" + Utils.flattenBlockState(displayBlockStates) + "]";
                BlockData displayBlockData = displayTileMaterial.createBlockData(_blockData);
                minecart.setDisplayBlockData(displayBlockData);
            }else
                minecart.setDisplayBlockData(displayTileMaterial.createBlockData());

            if(props.containsKey("display_tile_offset"))
                minecart.setDisplayBlockOffset((int)props.get("display_tile_offset"));
        }
    }

    /**
     * Prepare a chest (storage) minecart
     * @param storageMinecart Storage Minecart, like a Chest Minecart to prep
     * @param props JSON object containing the properties of chest minecart
     * @throws Exception An exception if the there was a problem setting the inventory of minecart
     */
    public static void prepChestMinecart(StorageMinecart storageMinecart, JSONObject props) throws Exception {
        InventoryHelper.prepInventoryChest(storageMinecart, props);
        InventoryHelper.prepLootableChest(storageMinecart, props);
    }

    /**
     * Prepare a hopper minecart
     * @param hopperMinecart The hopper minecart to prep
     * @param props JSON object containing the properties of the minecart
     * @throws Exception Exception An exception if the there was a problem setting the inventory of minecart
     */
    public static void prepHopperMinecart(HopperMinecart hopperMinecart, JSONObject props) throws Exception {
        InventoryHelper.prepInventoryChest(hopperMinecart, props);
        InventoryHelper.prepLootableChest(hopperMinecart, props);
        if(props.containsKey("enabled"))
            hopperMinecart.setEnabled(Utils.ensureInt(props, "enabled") == 1);
    }

    /**
     * Prepare a furnace minecart
     * @param furnaceMinecart The furnace minecart to prepare
     * @param props JSON object containing the properties of the minecart
     */
    public static void prepFurnaceMinecart(PoweredMinecart furnaceMinecart, JSONObject props){
        if(props.containsKey("fuel"))
            furnaceMinecart.setFuel(Utils.ensureInt(props, "fuel"));
        if(props.containsKey("push_x"))
            furnaceMinecart.setPushX((double) props.get("push_x"));
        if(props.containsKey("push_z"))
            furnaceMinecart.setPushZ((double) props.get("push_z"));
    }

    /**
     * Prepare a command minecart
     * @param commandMinecart The command minecart to prepare
     * @param props JSON object containing the properties of the minecarts
     */
    public static void prepCommandMinecart(CommandMinecart commandMinecart, JSONObject props){
        if(props.containsKey("command"))
            commandMinecart.setCommand((String) props.get("command"));
    }

    /**
     * Stand in for once updated to 1.20.4+, as it will be able to se explosiveMinecart.setFuseTicks
     */
    public static void prepExplosiveMinecart(ExplosiveMinecart explosiveMinecart, JSONObject props){
        if(props.containsKey("tnt_fuse")){
            //ToDo: Once updated to 1.20.4+ Use explosiveMinecart.setFuseTicks((int)props.get("tnt_fuse"))
        }
    }
}
