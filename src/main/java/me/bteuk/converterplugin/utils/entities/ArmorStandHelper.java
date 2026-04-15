package me.bteuk.converterplugin.utils.entities;

import me.bteuk.converterplugin.Converter;
import me.bteuk.converterplugin.utils.Utils;
import me.bteuk.converterplugin.utils.items.ItemsHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

/**
 * Helper class to set Armor Stands
 * @author DavixDevelop
 */
public class ArmorStandHelper {

    /**
     * Prepare the provided ArmorStand based on the JSON properties
     * @param armorStand Armor Stand to prepare
     * @param properties JSON object containing the properties of Armor Stands
     * @throws Exception Catch any exception that may happen while propping the armor stand
     */
    public static void propArmorStand(ArmorStand armorStand, JSONObject properties) throws Exception {
        Utils.prepEntity(armorStand, properties);
        armorStand.setArms(Utils.ensureInt(properties, "ShowArms") == 1);
        armorStand.setInvisible(Utils.ensureInt(properties, "Invisible") == 1);
        armorStand.setSmall(Utils.ensureInt(properties, "Small") == 1);
        armorStand.setBasePlate(Utils.ensureInt(properties, "NoBasePlate") == 0);

        if(properties.containsKey("Pose")) {
            JSONObject poseObject = (JSONObject) properties.get("Pose");

            if (poseObject.containsKey("Body")) {
                JSONArray bodyPoseArray = (JSONArray) poseObject.get("Body");
                EulerAngle bodyPose = Utils.DegreesToEulerAngles((double) bodyPoseArray.get(0), (double) bodyPoseArray.get(1), (double) bodyPoseArray.get(2));

                armorStand.setBodyPose(bodyPose);
            }

            if (poseObject.containsKey("Head")) {
                JSONArray headPoseArray = (JSONArray) poseObject.get("Head");
                EulerAngle headPose = Utils.DegreesToEulerAngles((double) headPoseArray.get(0), (double) headPoseArray.get(1), (double) headPoseArray.get(2));
                armorStand.setHeadPose(headPose);
            }

            if (poseObject.containsKey("LeftArm")) {
                JSONArray leftArmPoseArray = (JSONArray) poseObject.get("LeftArm");
                EulerAngle leftArmPose = Utils.DegreesToEulerAngles((double) leftArmPoseArray.get(0), (double) leftArmPoseArray.get(1), (double) leftArmPoseArray.get(2));
                armorStand.setLeftArmPose(leftArmPose);
            }

            if (poseObject.containsKey("RightArm")) {
                JSONArray rightArmPoseArray = (JSONArray) poseObject.get("RightArm");
                EulerAngle rightArmPose = Utils.DegreesToEulerAngles((double) rightArmPoseArray.get(0), (double) rightArmPoseArray.get(1), (double) rightArmPoseArray.get(2));
                armorStand.setRightArmPose(rightArmPose);
            }

            if (poseObject.containsKey("LeftLeg")) {
                JSONArray leftLegPoseArray = (JSONArray) poseObject.get("LeftLeg");
                EulerAngle leftLegPose = Utils.DegreesToEulerAngles((double) leftLegPoseArray.get(0), (double) leftLegPoseArray.get(1), (double) leftLegPoseArray.get(2));
                armorStand.setLeftLegPose(leftLegPose);
            }

            if (poseObject.containsKey("RightLeg")) {
                JSONArray rightLegPoseArray = (JSONArray) poseObject.get("RightLeg");
                EulerAngle rightLegPose = Utils.DegreesToEulerAngles((double) rightLegPoseArray.get(0), (double) rightLegPoseArray.get(1), (double) rightLegPoseArray.get(2));
                armorStand.setRightLegPose(rightLegPose);
            }
        }

        JSONArray armorItemsArray = (JSONArray) properties.get("ArmorItems");
        EntityEquipment armorEquipment = armorStand.getEquipment();
        for(int c = 0; c < 4; c++) {
            JSONObject armorItemObject = (JSONObject) armorItemsArray.get(c);
            if (armorItemObject == null || armorItemObject.isEmpty())
                continue;

            String armorItemID = ((String) armorItemObject.get("id")).substring(10);
            JSONObject armorItemProps = (JSONObject) armorItemObject.getOrDefault("Properties", new JSONObject());

            ItemStack armorItem = ItemsHelper.getItem(armorItemID, armorItemProps);

            /*if (armorItemObject.containsKey("DisplayProps")) {
                JSONObject displayProps = (JSONObject) armorItemObject.get("DisplayProps");
                if(displayProps.containsKey("display_name")) {
                    SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
                    skullMeta.displayName(Component.text((String) displayProps.get("display_name")));
                    skullItem.setItemMeta(skullMeta);
                }
            }*/

            switch (c){
                case 0 -> armorEquipment.setBoots(armorItem);
                case 1 -> armorEquipment.setLeggings(armorItem);
                case 2 -> armorEquipment.setChestplate(armorItem);
                case 3 -> armorEquipment.setHelmet(armorItem);
            }
        }

        JSONArray handItemsArray = (JSONArray) properties.get("HandItems");
        for(int c = 0; c < 2; c++ ){
            String handItemID = (String) handItemsArray.get(c);
            if(handItemID.isEmpty())
                continue;

            handItemID = handItemID.substring(10);

            ItemStack handItem = ItemsHelper.getItem(handItemID, new JSONObject());

            if (c == 0)
                armorEquipment.setItemInMainHand(handItem);
            else
                armorEquipment.setItemInOffHand(handItem);

        }
    }
}
