package edn.stratodonut.tallyho.camera.entity;

import javax.annotation.Nonnull;

public interface ILaserDesignator {
   void designate();

   boolean isDesignatorActive();

   @Nonnull
   String getCode();
}
