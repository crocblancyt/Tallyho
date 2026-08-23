package edn.stratodonut.tallyho.camera;

public final class AngleLimits {
    private final float lowerX;

    private final float upperX;

    private final float Y;

    public AngleLimits(float lowerX, float upperX, float Y) {
        this.lowerX = lowerX;
        this.upperX = upperX;
        this.Y = Y;
    }

    public float lowerX() {
      return this.lowerX;
    }

    public float upperX() {
      return this.upperX;
    }

    public float Y() {
      return this.Y;
    }
}
