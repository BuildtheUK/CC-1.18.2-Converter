package me.bteuk.converterplugin.utils;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Entity;
import org.bukkit.util.EulerAngle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
     * Convert double degrees to Euler Angles
     * @param x X rotation in degrees
     * @param y Y rotation in degrees
     * @param z Z rotation in degrees
     * @return The converted XYZ rotation in degrees to Euler Angle
     */
    public static EulerAngle DegreesToEulerAngles(double x, double y, double z){
        double xRad = Math.toRadians(x);
        double yRad = Math.toRadians(y);
        double zRad = Math.toRadians(z);

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
        entity.setGravity(properties.containsKey("NoGravity") ? Utils.ensureInt( properties, "NoGravity") == 1 : false);
        if(properties.containsKey("Rotation")){
            JSONArray entityRotationArray = (JSONArray) properties.get("Rotation");
            entity.setRotation( (float) (double)entityRotationArray.get(0), (float) (double)entityRotationArray.get(1));
        }
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
        for(Object item : rawArray)
            list.add((int) (long) item);
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
            int col = (int) (long) colors.get(c);
            cols.add(Color.fromRGB(col));
        }
        return cols;
    }

    /**
     * Ensure a double value gets returned from the key in a json object, else return the default value
     * @param jsonObject The JSON object containing the key
     * @param key Name of the key of the double value
     * @param defaultValue The default value to return if the key is not found
     * @return The double value at the key, else default value
     */
    public static double ensureDouble(JSONObject jsonObject, String key, double defaultValue) {
        Object rawValue = jsonObject.getOrDefault(key, null);
        if(rawValue != null) {
            if(rawValue instanceof Double doubleValue) return doubleValue;
            else if(rawValue instanceof Integer integerValue) return integerValue.doubleValue();
            else if(rawValue instanceof Long longValue) return longValue.doubleValue();
        }

        return defaultValue;
    }

    /**
     * Ensure an int value gets returned from the json object
     * @param jsonObject The JSON object containing the key
     * @param key Name of the key of the int value
     * @return The int value at the key
     */
    public static int ensureInt(JSONObject jsonObject, String key) {
        Object rawValue = jsonObject.get(key);
        if(rawValue instanceof Long longValue) return longValue.intValue();

        return (Integer)rawValue;
    }
}
