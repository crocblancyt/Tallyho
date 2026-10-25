package edn.stratodonut.tallyho.camera;

import edn.stratodonut.tallyho.camera.entity.CameraEntity2;

public class MultiOpticType {
   private CameraEntity2.PostFXType[] types;
   private int currentOpticIdx = 0;

   public MultiOpticType(CameraEntity2.PostFXType... types) {
      this.types = types;
   }

   public void nextType() {
      this.currentOpticIdx = (this.currentOpticIdx + 1) % this.types.length;
   }

   public CameraEntity2.PostFXType getCurrentOptic() {
      return this.types[this.currentOpticIdx];
   }
}
