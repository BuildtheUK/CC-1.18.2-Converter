package org.btuk.converter.plugin.utils.entities;

import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.btuk.converter.plugin.Plugin;
import org.btuk.converter.plugin.utils.Utils;
import org.btuk.converter.plugin.utils.items.ItemsHelper;

/**
 * Helper class to set Armor Stands
 * @author DavixDevelop
 */
public class ArmorStandHelper {

    private static Plugin instance;

    public static void setHelper(Plugin instance) {
        ArmorStandHelper.instance = instance;
    }

    /**
     * Prepare the provided ArmorStand based on the JSON properties
     * @param armorStand Armor Stand to prepare
     * @param properties JSON object containing the properties of Armor Stands
     * @throws Exception Catch any exception that may happen while propping the armor stand
     */
    public static void propArmorStand(ArmorStand armorStand, JSONObject properties) throws Exception {
        Utils.prepEntity(armorStand, properties);
        Integer showArms = Utils.readInteger(properties, "ShowArms");
        Integer invisible = Utils.readInteger(properties, "Invisible");
        Integer small = Utils.readInteger(properties, "Small");
        Integer noBasePlate = Utils.readInteger(properties, "NoBasePlate");
        armorStand.setArms(showArms != null && showArms == 1);
        armorStand.setInvisible(invisible != null && invisible == 1);
        armorStand.setSmall(small != null && small == 1);
        armorStand.setBasePlate(noBasePlate == null || noBasePlate == 0);

        if(properties.containsKey("Pose")) {
            JSONObject poseObject = (JSONObject) properties.get("Pose");

            if (poseObject.containsKey("Body")) {
                JSONArray bodyPoseArray = (JSONArray) poseObject.get("Body");
                EulerAngle bodyPose = Utils.DegreesToEulerAngles(bodyPoseArray);
                if(bodyPose != null)
                    armorStand.setBodyPose(bodyPose);
            }

            if (poseObject.containsKey("Head")) {
                JSONArray headPoseArray = (JSONArray) poseObject.get("Head");
                EulerAngle headPose = Utils.DegreesToEulerAngles(headPoseArray);
                if(headPose != null)
                    armorStand.setHeadPose(headPose);
            }

            if (poseObject.containsKey("LeftArm")) {
                JSONArray leftArmPoseArray = (JSONArray) poseObject.get("LeftArm");
                EulerAngle leftArmPose = Utils.DegreesToEulerAngles(leftArmPoseArray);
                if(leftArmPose != null)
                    armorStand.setLeftArmPose(leftArmPose);
            }

            if (poseObject.containsKey("RightArm")) {
                JSONArray rightArmPoseArray = (JSONArray) poseObject.get("RightArm");
                EulerAngle rightArmPose = Utils.DegreesToEulerAngles(rightArmPoseArray);
                if(rightArmPose != null)
                    armorStand.setRightArmPose(rightArmPose);
            }

            if (poseObject.containsKey("LeftLeg")) {
                JSONArray leftLegPoseArray = (JSONArray) poseObject.get("LeftLeg");
                EulerAngle leftLegPose = Utils.DegreesToEulerAngles(leftLegPoseArray);
                if(leftLegPose != null)
                    armorStand.setLeftLegPose(leftLegPose);
            }

            if (poseObject.containsKey("RightLeg")) {
                JSONArray rightLegPoseArray = (JSONArray) poseObject.get("RightLeg");
                EulerAngle rightLegPose = Utils.DegreesToEulerAngles(rightLegPoseArray);
                if(rightLegPose != null)
                    armorStand.setRightLegPose(rightLegPose);
            }
        }

        JSONArray armorItemsArray = (JSONArray) properties.get("ArmorItems");
        EntityEquipment armorEquipment = armorStand.getEquipment();
        for(int c = 0; c < 4; c++) {
            JSONObject armorItemObject = (JSONObject) armorItemsArray.get(c);
            if (armorItemObject == null || armorItemObject.isEmpty())
                continue;

            String armorItemID = ((String) armorItemObject.get("id"));
            JSONObject armorItemProps = (JSONObject) armorItemObject.getOrDefault("Properties", new JSONObject());

            try {

                ItemStack armorItem = ItemsHelper.getItem(armorItemID, armorItemProps);

            /*if (armorItemObject.containsKey("DisplayProps")) {
                JSONObject displayProps = (JSONObject) armorItemObject.get("DisplayProps");
                if(displayProps.containsKey("display_name")) {
                    SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
                    skullMeta.displayName(Component.text((String) displayProps.get("display_name")));
                    skullItem.setItemMeta(skullMeta);
                }
            }*/

                if (armorItem != null) {
                    switch (c) {
                        case 0 -> armorEquipment.setBoots(armorItem);
                        case 1 -> armorEquipment.setLeggings(armorItem);
                        case 2 -> armorEquipment.setChestplate(armorItem);
                        case 3 -> armorEquipment.setHelmet(armorItem);
                    }
                }
            }catch (Exception ex) {
                instance.getLogger().warning("Error while setting armor items for Armor Stand at " + armorStand.getLocation() + ": " + ex.getMessage());
            }
        }

        JSONArray handItemsArray = (JSONArray) properties.get("HandItems");
        for(int c = 0; c < 2; c++ ){
            String handItemID = (String) handItemsArray.get(c);
            if(handItemID.isEmpty())
                continue;

            try {
                ItemStack handItem = ItemsHelper.getItem(handItemID, new JSONObject());

                if (handItem != null) {
                    if (c == 0)
                        armorEquipment.setItemInMainHand(handItem);
                    else
                        armorEquipment.setItemInOffHand(handItem);
                }
            }catch (Exception ex) {
                instance.getLogger().warning("Error while setting hand items for Armor Stand at " + armorStand.getLocation() + ": " + ex.getMessage());
            }

        }
    }
}
