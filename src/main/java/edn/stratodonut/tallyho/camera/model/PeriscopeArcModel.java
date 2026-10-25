package edn.stratodonut.tallyho.camera.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PeriscopeArcModel<T extends CameraEntity2> extends EntityModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(TallyhoMod.asResource("periscope_arc_entity"), "main");
   private final ModelPart bone;

   public PeriscopeArcModel(ModelPart root) {
      this.bone = root.getChild("bone");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bone = partdefinition.addOrReplaceChild(
         "bone",
         CubeListBuilder.create()
            .texOffs(0, 12)
            .addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(16, 7)
            .addBox(-3.0F, -2.0F, -3.75F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      bone.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -1.0F, 1.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-8.0F, -1.0F, -2.0F, 0.0F, 0.7854F, 0.0F)
      );
      bone.addOrReplaceChild(
         "cube_r2",
         CubeListBuilder.create().texOffs(0, 6).addBox(-3.0F, -1.0F, 1.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(8.0F, -1.0F, -2.0F, 0.0F, -0.7854F, 0.0F)
      );
      bone.addOrReplaceChild(
         "inner_r1",
         CubeListBuilder.create().texOffs(16, 13).addBox(-1.95F, -1.25F, 2.25F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(8.0F, -0.75F, -3.5F, 0.0F, -0.7854F, 0.0F)
      );
      bone.addOrReplaceChild(
         "inner_r2",
         CubeListBuilder.create().texOffs(16, 1).addBox(-4.05F, -1.25F, 2.25F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-8.0F, -0.75F, -3.5F, 0.0F, 0.7854F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(
      PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha
   ) {
      this.bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
