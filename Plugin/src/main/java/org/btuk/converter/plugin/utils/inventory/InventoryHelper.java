package org.btuk.converter.plugin.utils.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.btuk.converter.plugin.utils.Utils;
import org.btuk.converter.plugin.utils.items.ItemsHelper;

/**
 * Helper class to set items of Inventory holders, like Minecarts, and set loot tables for Lootable Entity Inventories like Minecarts
 */
public class InventoryHelper {

    /**
     * Set the items in an Inventory Holder like a Minecart
     * @param inventoryHolder The entity holding inventory
     * @param props JSON object containing the Items key
     * @throws Exception If these was an error while setting the items
     */
    public static void prepInventoryChest(InventoryHolder inventoryHolder, JSONObject props) throws Exception {
        if(props.containsKey("items")){
            JSONArray minecartItems = (JSONArray) props.get("items");
            Inventory inventory = inventoryHolder.getInventory();
            ItemsHelper.setItems(inventory, minecartItems);
        }
    }

    /**
     * Set the loot table of ex, Minecart
     * @param lootableEntityInventory The entity containing a lootable inventory
     * @param props JSON Props to set the loot table of the entity
     */
    public static void prepLootableChest(com. destroystokyo. paper. loottable. LootableEntityInventory lootableEntityInventory, JSONObject props){
        if(props.containsKey("loot_table")){
            String _lootTable = (String) props.get("loot_table");
            LootTable lootTable = getLootTable(_lootTable);
            if(lootTable != null) {
                Long loot_table_seed = Utils.readLong(props, "loot_table_seed");
                if (loot_table_seed != null)
                    lootableEntityInventory.setLootTable(lootTable, loot_table_seed);
                else
                    lootableEntityInventory.setLootTable(lootTable);
            }
        }
    }

    /**
     * Get the LootTable based on the string ID of the loot table
     * @param lootTable The string ID of the loot table
     * @return New LootTable based on It's string ID
     */
    public static LootTable getLootTable(String lootTable){
        if(lootTable.startsWith("minecraft:chests/"))
            return LootTables.valueOf(lootTable.substring(17).toUpperCase()).getLootTable();

        if(lootTable.startsWith("minecraft:entities"))
            return LootTables.valueOf(lootTable.substring(19).toUpperCase()).getLootTable();

        return null;
    }
}
