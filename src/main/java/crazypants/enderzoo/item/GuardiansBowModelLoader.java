package crazypants.enderzoo.item;

import crazypants.enderzoo.EnderZoo;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GuardiansBowModelLoader {

  public static final String[] NAMES = new String[] { EnderZoo.MODID + ":" + ItemGuardiansBow.NAME,
      EnderZoo.MODID + ":" + ItemGuardiansBow.NAME + "_pulling_0",
      EnderZoo.MODID + ":" + ItemGuardiansBow.NAME + "_pulling_1",
      EnderZoo.MODID + ":" + ItemGuardiansBow.NAME + "_pulling_2" };

  public static final ModelResourceLocation[] MODELS;

  static {
    MODELS = new ModelResourceLocation[NAMES.length];
    for (int i = 0; i < NAMES.length; i++) {
      MODELS[i] = new ModelResourceLocation(NAMES[i], "inventory");
    }
  }

  public static ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining) {
    ModelResourceLocation modelresourcelocation = MODELS[0];
    if(player.getItemInUse() != null) {
      int useTime = stack.getMaxItemUseDuration() - useRemaining;
      int drawTime = EnderZoo.itemGuardiansBow.getDrawTime();
      if(useTime >= drawTime - 2) {
        modelresourcelocation = MODELS[3];
      } else if(useTime > drawTime * 2 / 3f) {
        modelresourcelocation = MODELS[2];
      } else if(useTime > 0) {
        modelresourcelocation = MODELS[1];
      }
    }
    return modelresourcelocation;
  }

  public static void registerVariants() {
    ModelBakery.addVariantName(EnderZoo.itemGuardiansBow, GuardiansBowModelLoader.NAMES);
  }

}
