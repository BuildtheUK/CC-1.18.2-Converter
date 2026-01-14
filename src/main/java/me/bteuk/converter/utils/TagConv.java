package org.btuk.converter.utils;

import net.querz.nbt.tag.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Converter class for NBT tags to JSON
 * @author DavixDevelop
 */
public class TagConv {
    /**
     * Convert a ListTag of FloatTag to a Float List and insert it into a JSONObject
     * @param listKey The key name of the ListTag
     * @param tag The CompoundTag containing the key
     * @param json The JSON object to insert the Float List into at the key name
     */
    public static void floatTagListToJson(String listKey, CompoundTag tag, JSONObject json){
        if(tag.containsKey(listKey)){
            ListTag<FloatTag> floatTags = tag.getListTag(listKey).asFloatTagList();
            List<Float> floatList = new ArrayList<>();
            for (FloatTag val : floatTags) {
                floatList.add(val.asFloat());
            }
            json.put(listKey, floatList);
        }
    }

    /**
     * Convert a ListTag of StringTag inside the tag with the tagName
     * to a List String and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the ListTag of StringTag
     * @param tagName The key name of the ListTag
     * @param propName The key name to insert the List String at in the JSON object
     * @param properties A JSON object to which to write the List String into
     */
    public static void getStringTagListProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName)){
            ListTag<StringTag> stringTags = tag.getListTag(tagName).asStringTagList();
            List<String> stringList = new ArrayList<>();
            for(StringTag stringTag : stringTags)
                stringList.add(stringTag.getValue());

            if(!stringList.isEmpty())
                properties.put(propName, stringList);
        }
    }

    /**
     * Convert a IntArrayTag inside the tag with the tagName
     * to a List Integer and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the IntArrayTag
     * @param tagName The key name of the IntArrayTag
     * @param propName The key name to insert the List Integer at in the JSON object
     * @param properties A JSON object to which to write the List Integer into
     */
    public static void getIntArrayTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName)){
            int[] intArray = tag.getIntArrayTag(tagName).getValue();
            List<Integer> intList = new ArrayList<>();
            for(int c = 0; c < intArray.length; c++)
                intList.add(intArray[c]);
            properties.put(propName, intList);
        }
    }

    /**
     * Convert a ByteArrayTag inside the tag with the tagName
     * to a List Byte and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the ByteArrayTag
     * @param tagName The key name of the ByteArrayTag
     * @param propName The key name to insert the List Byte at in the JSON object
     * @param properties A JSON object to which to write the List Byte into
     */
    public static void getByteArrayTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName)){
            byte[] byteArray = tag.getByteArrayTag(tagName).getValue();
            List<Byte> byteList = new ArrayList<>();
            for(int c = 0; c < byteArray.length; c++)
                byteList.add(byteArray[c]);

            properties.put(propName, byteList);
        }
    }

    /**
     * Get the ByteTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the ByteTag
     * @param tagName The key name of the ByteTag
     * @param propName The key name to insert the ByteTag value in the JSON object
     * @param properties A JSON object to which to write the ByteTag value into
     */
    public static void getByteTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getByte(tagName));
    }

    /**
     * Get the StringTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the StringTag
     * @param tagName The key name of the StringTag
     * @param propName The key name to insert the StringTag value in the JSON object
     * @param properties A JSON object to which to write the StringTag value into
     */
    public static void getStringTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getString(tagName));
    }

    /**
     * Get the DoubleTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the DoubleTag
     * @param tagName The key name of the DoubleTag
     * @param propName The key name to insert the DoubleTag value in the JSON object
     * @param properties A JSON object to which to write the DoubleTag value into
     */
    public static void getDoubleTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getDouble(tagName));
    }

    /**
     * Get the BooleanTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the BooleanTag
     * @param tagName The key name of the BooleanTag
     * @param propName The key name to insert the BooleanTag value in the JSON object
     * @param properties A JSON object to which to write the BooleanTag value into
     */
    public static void getBooleanTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getBoolean(tagName));
    }

    /**
     * Get the IntTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the IntTag
     * @param tagName The key name of the IntTag
     * @param propName The key name to insert the IntTag value in the JSON object
     * @param properties A JSON object to which to write the IntTag value into
     */
    public static void getIntTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getInt(tagName));
    }

    /**
     * Get the LongTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the LongTag
     * @param tagName The key name of the LongTag
     * @param propName The key name to insert the LongTag value in the JSON object
     * @param properties A JSON object to which to write the LongTag value into
     */
    public static void getLongTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getLong(tagName));
    }

    /**
     * Get the ShortTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the ShortTag
     * @param tagName The key name of the ShortTag
     * @param propName The key name to insert the ShortTag value in the JSON object
     * @param properties A JSON object to which to write the ShortTag value into
     */
    public static void getShortTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getShort(tagName));
    }

    /**
     * Get the FloatTag value inside the tag with the tagName
     * and insert it into a JSON object at the prop name
     * @param tag The CompoundTag containing the FloatTag
     * @param tagName The key name of the FloatTag
     * @param propName The key name to insert the FloatTag value in the JSON object
     * @param properties A JSON object to which to write the FloatTag value into
     */
    public static void getFloatTagProperty(CompoundTag tag, String tagName, String propName, JSONObject properties){
        if(tag.containsKey(tagName))
            properties.put(propName, tag.getFloat(tagName));
    }

    /**
     * Ger call NBT tags values inside the CompoundTag and write it to a JSON object using the same key names
     * @param tag The CompoundTag to parse through
     * @param properties The JSON object to insert the NBT tags into at the original key names
     */
    public static void getCompoundTagProperties(CompoundTag tag, JSONObject properties){
        Set<String> keySet = tag.keySet();
        for(String key : keySet){
            Tag<?> tag1 = tag.get(key);
            if(tag1 instanceof IntTag)
                properties.put(key, ((IntTag)tag1).asInt());
            if(tag1 instanceof StringTag)
                properties.put(key, ((StringTag)tag1).getValue());
            if(tag1 instanceof ByteTag)
                properties.put(key, ((ByteTag)tag1).asByte());
            if(tag1 instanceof DoubleTag)
                properties.put(key, ((DoubleTag)tag1).asDouble());
            if(tag1 instanceof FloatTag)
                properties.put(key, ((FloatTag)tag1).asFloat());
            if(tag1 instanceof ListTag<?>){
                ListTag<?> listTag = ((ListTag<?>) tag1);
                if(listTag.getTypeClass() == ByteTag.class){
                    ListTag<StringTag> tags = listTag.asStringTagList();
                    List<String> list = new ArrayList<>();
                    for (StringTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == FloatTag.class){
                    ListTag<FloatTag> tags = listTag.asFloatTagList();
                    List<Float> list = new ArrayList<>();
                    for (FloatTag val : tags) {
                        list.add(val.asFloat());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == ByteTag.class){
                    ListTag<ByteTag> tags = listTag.asByteTagList();
                    List<Byte> list = new ArrayList<>();
                    for (ByteTag val : tags) {
                        list.add(val.asByte());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == IntTag.class){
                    ListTag<IntTag> tags = listTag.asIntTagList();
                    List<Integer> list = new ArrayList<>();
                    for (IntTag val : tags) {
                        list.add(val.asInt());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == DoubleTag.class){
                    ListTag<DoubleTag> tags = listTag.asDoubleTagList();
                    List<Byte> list = new ArrayList<>();
                    for (DoubleTag val : tags) {
                        list.add(val.asByte());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == ShortTag.class){
                    ListTag<ShortTag> tags = listTag.asShortTagList();
                    List<Short> list = new ArrayList<>();
                    for (ShortTag val : tags) {
                        list.add(val.asShort());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == LongTag.class){
                    ListTag<LongTag> tags = listTag.asLongTagList();
                    List<Long> list = new ArrayList<>();
                    for (LongTag val : tags) {
                        list.add(val.asLong());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == ByteArrayTag.class){
                    ListTag<ByteArrayTag> tags = listTag.asByteArrayTagList();
                    List<byte[]> list = new ArrayList<>();
                    for (ByteArrayTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == IntArrayTag.class){
                    ListTag<IntArrayTag> tags = listTag.asIntArrayTagList();
                    List<int[]> list = new ArrayList<>();
                    for (IntArrayTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }else if(listTag.getTypeClass() == LongArrayTag.class){
                    ListTag<LongArrayTag> tags = listTag.asLongArrayTagList();
                    List<long[]> list = new ArrayList<>();
                    for (LongArrayTag val : tags) {
                        list.add(val.getValue());
                    }
                    properties.put(key, list);
                }
            }

        }
    }

    /**
     * Flatten a CompoundTag containing StringTag's to a String in the format tagName1=tagValue1,tagName2=tagValue2...
     * @param tag The CompoundTag containing exclusively StringTag's
     * @return String of flattened list of StringTag's key-value pairs
     */
    public static String flattenStringsCompoundTag(CompoundTag tag){
        StringBuilder flattened = new StringBuilder();
        Set<String> tagKeys =  tag.keySet();
        int c = 0;
        for(String state : tagKeys){
            flattened.append(String.format("%1$s=%2$s", state, tag.getString(state) + (c + 1 == tagKeys.size() ? "" : ",")));
            c++;
        }

        return flattened.toString();
    }

    /**
     * Parse a flattened list of StringTag's key-value pairs into a HashMap
     * @param flattenedTags String of a flattened list of StringTag's key-value pairs in the format tagName1=tagValue1,tagName2=tagValue2...
     * @param tags The HashMap to insert the parsed key-value parsed into
     */
    public static void parseFlattenedTags(String flattenedTags, HashMap<String, String> tags){
        if(flattenedTags.contains("=")) {
            if (!flattenedTags.contains(",")) {
                int index = flattenedTags.indexOf("=");
                tags.put(flattenedTags.substring(0, index), flattenedTags.substring(index + 1));
            }else {
                String[] argStates = flattenedTags.split(",");
                for (String argState : argStates){
                    int index = argState.indexOf("=");
                    tags.put(argState.substring(0, index), argState.substring(index + 1));
                }
            }
        }
    }
}
