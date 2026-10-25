package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientChunkCache.Storage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent.Load;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientChunkCache.class, priority = 1300)
public abstract class ClientChunkCacheMixin {
   @Shadow
   volatile Storage storage;
   @Shadow
   @Final
   ClientLevel level;

   private Storage newStorage(int viewDistance) {
      ClientChunkCache cache = (ClientChunkCache) (Object) this;
      if (cache != null) {
         return cache.new Storage(viewDistance);
      }
      return null;
   }

   @Shadow
   private static boolean isValidChunk(LevelChunk chunk, int x, int z) {
      throw new IllegalStateException("Shadowing isValidChunk did not work!");
   }

   @Inject(method = "<init>", at = @At("TAIL"))
   public void tyh$onInit(ClientLevel level, int viewDistance, CallbackInfo ci) {
      CameraPossessionController.setCameraStorage(this.newStorage(Math.max(2, viewDistance) + 3));
   }

   @Inject(
      method = "updateViewRadius",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;<init>(Lnet/minecraft/client/multiplayer/ClientChunkCache;I)V"
      )
   )
   public void tyh$onUpdateViewRadius(int viewDistance, CallbackInfo ci) {
      Storage oldStorage = CameraPossessionController.getCameraStorage();
      Storage newStorage = this.newStorage(Math.max(2, viewDistance) + 3);
      newStorage.viewCenterX = oldStorage.viewCenterX;
      newStorage.viewCenterZ = oldStorage.viewCenterZ;

      for (int i = 0; i < oldStorage.chunks.length(); i++) {
         LevelChunk chunk = (LevelChunk)oldStorage.chunks.get(i);
         if (chunk != null) {
            ChunkPos pos = chunk.getPos();
            if (newStorage.inRange(pos.x, pos.z)) {
               newStorage.replace(newStorage.getIndex(pos.x, pos.z), chunk);
            }
         }
      }

      CameraPossessionController.setCameraStorage(newStorage);
   }

   @Inject(method = "drop", at = @At("HEAD"))
   public void tyh$onDrop(int x, int z, CallbackInfo ci) {
      Storage cameraStorage = CameraPossessionController.getCameraStorage();
      if (cameraStorage.inRange(x, z)) {
         int i = cameraStorage.getIndex(x, z);
         LevelChunk chunk = cameraStorage.getChunk(i);
         if (chunk != null && chunk.getPos().x == x && chunk.getPos().z == z) {
            MinecraftForge.EVENT_BUS.post(new Load(chunk, false));
            cameraStorage.replace(i, chunk, null);
         }
      }
   }

   @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
   private void tyh$onReplace(
      int x, int z, FriendlyByteBuf buffer, CompoundTag chunkTag, Consumer<BlockEntityTagOutput> tagOutputConsumer, CallbackInfoReturnable<LevelChunk> callback
   ) {
      Storage cameraStorage = CameraPossessionController.getCameraStorage();
      if (CameraClientHandler.isPlayerMountedOnRemote() && cameraStorage.inRange(x, z)) {
         int index = cameraStorage.getIndex(x, z);
         LevelChunk chunk = cameraStorage.getChunk(index);
         ChunkPos chunkPos = new ChunkPos(x, z);
         if (!isValidChunk(chunk, x, z)) {
            chunk = new LevelChunk(this.level, chunkPos);
            chunk.replaceWithPacketData(buffer, chunkTag, tagOutputConsumer);
            cameraStorage.replace(index, chunk);
         } else {
            chunk.replaceWithPacketData(buffer, chunkTag, tagOutputConsumer);
         }

         this.level.onChunkLoaded(chunkPos);
         MinecraftForge.EVENT_BUS.post(new Load(chunk, true));
         callback.setReturnValue(chunk);
      }
   }

   @Inject(
      method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;",
      slice = @Slice(from = @At(value = "RETURN", ordinal = 1)),
      at = @At("RETURN"),
      cancellable = true
   )
   private void tyh$onGetChunk(int x, int z, ChunkStatus requiredStatus, boolean load, CallbackInfoReturnable<LevelChunk> callback) {
      if (CameraClientHandler.isPlayerMountedOnRemote() && CameraPossessionController.getCameraStorage().inRange(x, z)) {
         LevelChunk chunk = CameraPossessionController.getCameraStorage().getChunk(CameraPossessionController.getCameraStorage().getIndex(x, z));
         if (chunk != null && chunk.getPos().x == x && chunk.getPos().z == z) {
            callback.setReturnValue(chunk);
         }
      }
   }
}
