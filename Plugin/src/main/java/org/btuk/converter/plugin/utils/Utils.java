package org.btuk.converter.plugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * General utility class for miscellaneous stuff
 */
public class Utils {

    /**
     * Precision floor
     * @param num The double number to floor
     * @return The floored number
     */
    public static int floor(double num) {
        int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    /**
     * Convert double degrees in a JSON Array to Euler Angles
     * @param jsonArray JSON Array containing the x, y and z rotation in degrees
     * @return The converted XYZ rotation in degrees to Euler Angle, else null
     */
    @Nullable
    public static EulerAngle DegreesToEulerAngles(JSONArray jsonArray){
        Number xDeg = getNumericValue(jsonArray.get(0));
        Number yDeg = getNumericValue(jsonArray.get(1));
        Number zDeg = getNumericValue(jsonArray.get(2));

        if(xDeg == null || yDeg == null || zDeg == null)
            return null;

        double xRad = Math.toRadians(xDeg.doubleValue());
        double yRad = Math.toRadians(yDeg.doubleValue());
        double zRad = Math.toRadians(zDeg.doubleValue());

        return new EulerAngle(xRad, yRad, zRad);
    }

    /**
     * Flatten block states to a flat list
     * @param blockStates A JSON Object of block states
     * @return Flattened list of block states as a string
     */
    public static String flattenBlockState(JSONObject blockStates){
        String flatten = "";
        Set<String> blockStateKeys = blockStates.keySet();
        int c = 0;
        for(String key : blockStateKeys){
            flatten += key + "=" + blockStates.get(key).toString() + (c != blockStates.size() - 1 ? "," : "");
            c++;
        }

        return flatten;
    }

    /**
     * Prepare the basic entity functionalities based on the provided JSON properties
     * @param entity The entity to prepare
     * @param properties The basic properties to apply to the entity
     */
    public static void prepEntity(Entity entity, JSONObject properties){
        Integer noGravity = readInteger(properties, "NoGravity");
        entity.setGravity(noGravity == null || noGravity != 1);
        if(properties.containsKey("Rotation")){
            JSONArray entityRotationArray = (JSONArray) properties.get("Rotation");
            Number rotX = getNumericValue(entityRotationArray.get(0));
            Number rotY = getNumericValue(entityRotationArray.get(1));
            if(rotX != null && rotY != null)
                entity.setRotation( rotX.floatValue(), rotY.floatValue());
        }

        if(properties.containsKey("CustomName")) {
            Component customNameComponent = getTextComponent((String) properties.get("CustomName"));
            entity.customName(customNameComponent);
        }

        Integer customNameVisible = readInteger(properties, "CustomNameVisible");
        if(customNameVisible != null)
            entity.setCustomNameVisible(customNameVisible == 1);
    }

    /**
     * Get an Integer list from a JSON object
     * @param properties The JSON object that contains a key presenting an integer list
     * @param key The name of the key which holds the Integer list
     * @return A converted Integer list
     */
    public static List<Integer> getIntegerListFromJson(JSONObject properties, String key){
        List<Integer> list = new ArrayList<>();
        JSONArray rawArray = (JSONArray) properties.get(key);
        for(Object item : rawArray) {
            Number val = getNumericValue(item);
            if(val != null)
                list.add(val.intValue());
        }
        return list;
    }

    /**
     * Get the Pattern Type based on It's pattern ID
     * @param p Pattern ID
     * @return The PatterType of the Pattern ID
     */
    public static PatternType getPatternType(String p) {

        switch (p) {

            case "bs" -> {
                return PatternType.STRIPE_BOTTOM;
            }

            case "ts" -> {
                return PatternType.STRIPE_TOP;
            }

            case "ls" -> {
                return PatternType.STRIPE_LEFT;
            }

            case "rs" -> {
                return PatternType.STRIPE_RIGHT;
            }

            case "cs" -> {
                return PatternType.STRIPE_CENTER;
            }

            case "ms" -> {
                return PatternType.STRIPE_MIDDLE;
            }

            case "drs" -> {
                return PatternType.STRIPE_DOWNRIGHT;
            }

            case "dls" -> {
                return PatternType.STRIPE_DOWNLEFT;
            }

            case "ss" -> {
                return PatternType.STRIPE_SMALL;
            }

            case "cr" -> {
                return PatternType.CROSS;
            }

            case "sc" -> {
                return PatternType.STRAIGHT_CROSS;
            }

            case "ld" -> {
                return PatternType.DIAGONAL_LEFT;
            }

            case "rud" -> {
                return PatternType.DIAGONAL_RIGHT_MIRROR;
            }

            case "lud" -> {
                return PatternType.DIAGONAL_LEFT_MIRROR;
            }

            case "rd" -> {
                return PatternType.DIAGONAL_RIGHT;
            }

            case "vh" -> {
                return PatternType.HALF_VERTICAL;
            }

            case "vhr" -> {
                return PatternType.HALF_VERTICAL_MIRROR;
            }

            case "hh" -> {
                return PatternType.HALF_HORIZONTAL;
            }

            case "hhb" -> {
                return PatternType.HALF_HORIZONTAL_MIRROR;
            }

            case "bl" -> {
                return PatternType.SQUARE_BOTTOM_LEFT;
            }

            case "br" -> {
                return PatternType.SQUARE_BOTTOM_RIGHT;
            }

            case "tl" -> {
                return PatternType.SQUARE_TOP_LEFT;
            }

            case "tr" -> {
                return PatternType.SQUARE_TOP_RIGHT;
            }

            case "bt" -> {
                return PatternType.TRIANGLE_BOTTOM;
            }

            case "tt" -> {
                return PatternType.TRIANGLE_TOP;
            }

            case "bts" -> {
                return PatternType.TRIANGLES_BOTTOM;
            }

            case "tts" -> {
                return PatternType.TRIANGLES_TOP;
            }

            case "mc" -> {
                return PatternType.CIRCLE_MIDDLE;
            }

            case "mr" -> {
                return PatternType.RHOMBUS_MIDDLE;
            }
            case "bo" -> {
                return PatternType.BORDER;
            }

            case "cbo" -> {
                return PatternType.CURLY_BORDER;
            }

            case "bri" -> {
                return PatternType.BRICKS;
            }

            case "gra" -> {
                return PatternType.GRADIENT;
            }

            case "gru" -> {
                return PatternType.GRADIENT_UP;
            }

            case "cre" -> {
                return PatternType.CREEPER;
            }

            case "sku" -> {
                return PatternType.SKULL;
            }

            case "flo" -> {
                return PatternType.FLOWER;
            }

            case "moj" -> {
                return PatternType.MOJANG;
            }

            case "glb" -> {
                return PatternType.GLOBE;
            }

            case "pig" -> {
                return PatternType.PIGLIN;
            }

            default -> {
                return PatternType.BASE;
            }
        }
    }

    /**
     * Get the dye color based on the name of the color
     * @param c Name of the color, ex. orange
     * @return The DyeColor from the name of the color
     */
    public static DyeColor getDyeColour(String c) {

        switch (c) {

            case "orange" -> {
                return DyeColor.ORANGE;
            }

            case "magenta" -> {
                return DyeColor.MAGENTA;
            }

            case "light_blue" -> {
                return DyeColor.LIGHT_BLUE;
            }

            case "yellow" -> {
                return DyeColor.YELLOW;
            }

            case "lime" -> {
                return DyeColor.LIME;
            }

            case "pink" -> {
                return DyeColor.PINK;
            }

            case "gray" -> {
                return DyeColor.GRAY;
            }

            case "light_gray" -> {
                return DyeColor.LIGHT_GRAY;
            }

            case "cyan" -> {
                return DyeColor.CYAN;
            }

            case "purple" -> {
                return DyeColor.PURPLE;
            }

            case "blue" -> {
                return DyeColor.BLUE;
            }

            case "brown" -> {
                return DyeColor.BROWN;
            }

            case "green" -> {
                return DyeColor.GREEN;
            }

            case "red" -> {
                return DyeColor.RED;
            }

            case "black" -> {
                return DyeColor.BLACK;
            }

            default -> {
                return DyeColor.WHITE;
            }
        }
    }

    /**
     * Get the Attribute from the legacy attribute name
     * @param legacyAttributeName Legacy attribute modifier name
     * @return The Attribute of the name, else null if none was found
     */
    public static Attribute getAttribute(String legacyAttributeName) {
        return switch (legacyAttributeName) {
            case "generic.maxHealth" -> Attribute.GENERIC_MAX_HEALTH;
            case "zombie.spawnReinforcements" -> Attribute.ZOMBIE_SPAWN_REINFORCEMENTS;
            case "horse.jumpStrength" -> Attribute.HORSE_JUMP_STRENGTH;
            case "generic.followRange" -> Attribute.GENERIC_FOLLOW_RANGE;
            case "generic.knockbackResistance" -> Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case "generic.movementSpeed" -> Attribute.GENERIC_MOVEMENT_SPEED;
            case "generic.flyingSpeed" -> Attribute.GENERIC_FLYING_SPEED;
            case "generic.attackDamage" -> Attribute.GENERIC_ATTACK_DAMAGE;
            case "generic.attackKnockback" -> Attribute.GENERIC_ATTACK_KNOCKBACK;
            case "generic.attackSpeed" -> Attribute.GENERIC_ATTACK_SPEED;
            case "generic.armorToughness" -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "generic.armor" -> Attribute.GENERIC_ARMOR;
            case "generic.luck" -> Attribute.GENERIC_LUCK;
            default -> null;
        };
    }

    /**
     * Get a list of colors based on each color integer value inside the JSON array
     * @param colors JSON array containing integers representing colors
     * @return List of colors
     */
    public static List<Color> getColors(JSONArray colors){
        List<Color> cols = new ArrayList<>();
        for(int c = 0; c < colors.size(); c++){
            Number col = getNumericValue(colors.get(c));
            if(col != null)
                cols.add(Color.fromRGB(col.intValue()));
        }
        return cols;
    }

    /**
     * Get the number from the raw value
     * @param rawValue Raw value
     * @return A number if the value is a number, else null
     */
    @Nullable
    public static Number getNumericValue(Object rawValue) {
        if(rawValue instanceof Integer intValue)
            return intValue;
        if(rawValue instanceof Long longValue)
            return longValue;
        if(rawValue instanceof Byte byteValue)
            return byteValue;
        if(rawValue instanceof Float floatValue)
            return floatValue;
        if(rawValue instanceof Double doubleValue)
            return doubleValue;

        return null;
    }

    /**
     * Ensure a double value gets returned from the key in a json object, else return the default value
     * @param jsonObject The JSON object containing the key
     * @param key Name of the key of the double value
     * @param defaultValue The default value to return if the key is not found
     * @return The double value at the key, else default value
     */
    public static double ensureDouble(JSONObject jsonObject, String key, double defaultValue) {
        Object rawValue = readJSON(jsonObject, key);
        if(rawValue != null) {
            Number number = getNumericValue(rawValue);
            if(number != null)
                return number.doubleValue();
        }

        return defaultValue;
    }

    /**
     * Read a double value gets returned from the key in a json object, else null  if It does not exist
     * @param jsonObject The JSON object containing the key
     * @param key Name of the key of the double value
     * @return The double value at the key, else null
     */
    @Nullable
    public static Double readDouble(JSONObject jsonObject, String key) {
        Object rawValue = readJSON(jsonObject ,key);
        if(rawValue == null) return null;
        Number number = getNumericValue(rawValue);
        if(number != null)
            return number.doubleValue();

        return null;
    }

    /**
     * Read an Integer value gets returned from the json object, else null if It does not exist
     * @param jsonObject The JSON object containing the key
     * @param key Name of the key of the int value
     * @return The int value at the key, else null
     */
    @Nullable
    public static Integer readInteger(JSONObject jsonObject, String key) {
        Object rawValue = readJSON(jsonObject, key);
        if(rawValue == null) return  null;
        Number number = getNumericValue(rawValue);
        if(number != null) return number.intValue();

        return null;
    }

    /**
     * Read a Long value gets returned from the json object, else null if It does not exist
     * @param jsonObject The JSON object containing the key
     * @param key Name of the key of the long value
     * @return The long value at the key, else null
     */
    @Nullable
    public static Long readLong(JSONObject jsonObject, String key) {
        Object rawValue = readJSON(jsonObject, key);
        if(rawValue == null) return  null;
        Number number = getNumericValue(rawValue);
        if(number != null) return number.longValue();

        return null;
    }

    /**
     * Read a Byte value gets returned from the json object, else null if It does not exist
     * @param jsonObject The JSON object containing the key
     * @param key Name of the key of the byte value
     * @return The byte value at the key, else null
     */
    @Nullable
    public static Byte readByte(JSONObject jsonObject, String key) {
        Object rawValue = readJSON(jsonObject, key);
        if(rawValue == null) return  null;
        Number number = getNumericValue(rawValue);
        if(number != null) return number.byteValue();

        return null;
    }

    /**
     * Read the Object at a key in the JSON Object if it exists, else return null
     * @param jsonObject The JSON Object that should contain the key
     * @param key The name of the key
     * @return The Object, else null
     */
    @Nullable
    public static Object readJSON(JSONObject jsonObject, String key) {
        if(jsonObject.containsKey(key))
            return jsonObject.get(key);

        return null;
    }

    private static final Map<String, Material> ID_TO_MATERIAL = new ConcurrentHashMap<>();

    /**
     * Get the Material for the given ID, else null
     * @param id The ID of the resource
     * @return The Material, else null
     */
    @Nullable
    public static Material getMaterial(String id) {
        return ID_TO_MATERIAL.computeIfAbsent(id, (_id) -> {
            if(_id == null || _id.isEmpty()) return null;

            _id = (_id.contains(":")) ? _id.split(":", 2)[1] : _id;

            Material mat = Material.matchMaterial(_id);
            if(mat == null) {
                mat = switch (_id) {
                    case "grass" -> Material.matchMaterial("short_grass");
                    case "tallgrass" -> Material.matchMaterial("tall_grass");
                    default -> null;
                };
            }

            return mat;
        });
    }

    private static final Map<String, Enchantment> ID_TO_ENCHANTMENT = new ConcurrentHashMap<>();

    /**
     * Get the enchantment from the string ID
     * @param id The ID of the enchantment, ex minecraft:aqua_affinity or aqua_affinity
     * @return The Enchantment, else null
     */
    @Nullable
    public static Enchantment getEnchantment(String id) {
        return ID_TO_ENCHANTMENT.computeIfAbsent(id, (_id) -> {
            if(_id == null || _id.isEmpty()) return null;

            _id = _id.contains(":") ? _id.split(":", 2)[1] : _id;

            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(_id));

            if(enchantment == null && _id.equals("sweeping"))
                enchantment = Enchantment.getByKey(NamespacedKey.minecraft("sweeping_edge"));

            return enchantment;
        });
    }

    /**
     * Get the text component from the string, ex. if It's a regular string, or JSON encoded test,
     * while also handling any legacy code (§)
     * @param rawString The raw string to display
     * @return The component to display
     */
    public static Component getTextComponent(String rawString) {
        Component component;
        if(rawString.startsWith("{") && rawString.endsWith("}") && rawString.contains(":")) {
            boolean hasLegacyCodes = rawString.contains("§");
            try {
                component = GsonComponentSerializer.gson().deserialize(rawString);
                if(hasLegacyCodes)
                    component = fixLegacyCodesInComponent(component);
            }catch (Exception ex) {
                component = Component.text(rawString);
            }
        }else {
            component = Component.text(rawString);
        }

        return component;
    }

    /**
     * Fix the component and It's children content from legacy code (§)
     * @param component The component to fix
     * @return The fixed component and It's fixed children, if it has any
     */
    public static Component fixLegacyCodesInComponent(Component component) {
        List<Component> fixedChildren = component.children().stream().map(Utils::fixLegacyCodesInComponent).toList();
        Component _component = component;

        if(component instanceof TextComponent textComponent) {
            TextComponent fixedTextComponent = LegacyComponentSerializer.legacySection().deserialize(textComponent.content());
            _component = fixedTextComponent
                    .style(textComponent.style().merge(fixedTextComponent.style(), Style.Merge.Strategy.ALWAYS))
                    .clickEvent(textComponent.clickEvent())
                    .hoverEvent(textComponent.hoverEvent())
                    .insertion(textComponent.insertion());
        }

        List<Component> children = new ArrayList<>(_component.children());
        children.addAll(fixedChildren);
        return _component.children(children);
    }
}
