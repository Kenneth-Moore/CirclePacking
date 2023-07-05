package graphbits;

public class PixRadMap {

    // this maps the FC world to your screen!

    // these are quite modifyable, because we need resizable screens.
    // also, these are in pixels, not radians!
    public int width;
    public int height;

    // the zoom of the screen. The larger this value is, the more zoomed in you are. So it's
    // pixels:radians.
    private double scale;

    // the translation of the screen. These are the radian coords of the center of the screen.
    public double transX;
    public double transY;

    public PixRadMap(final int width, final int height) {

        this.width = width;
        this.height = height;
        this.scale = 1;
        this.transX = 0;
        this.transY = 0;
    }

    public double getScale() {
    	return this.scale;
    }
    
    public void setScale(final double newScale) {
    	this.scale = newScale;
    }
    
    public double toRad(double pixValue) {
        return pixValue / scale;
    }

    public double toPix(double radValue) {
        return radValue * scale;
    }

    public double radianX(final double pixelX) {
        return toRad((int) (pixelX - (width / 2))) + this.transX;
    }

    public double radianY(final double pixelY) {
        return toRad((int) (pixelY - (height / 2))) + this.transY;
    }

    // rad = toRad(pix - w/2) + t
    // pix = toPix(rad - t) + w/2
    public double pixelX(final double radianX) {
        return toPix(radianX - this.transX) + width / 2;
    }

    public double pixelY(final double radianY) {
        return toPix(radianY - this.transY) + height / 2;
    }

    public void scaleBy(final double scaleFactor) {
        this.scale *= scaleFactor;
    }

    public void translateX(final double translate) {
        this.transX = translate;
    }

    public void translateY(final double translate) {
        this.transY = translate;
    }

    public void panX(final double translate) {
        this.transX += translate;
    }

    public void panY(final double translate) {
        this.transY += translate;
    }

    // the next two methods happen when we resize the screen.
    public void resizeX(final int newW) {

        this.width = newW;
    }
    public void resizeY(final int newH) {

        this.height = newH;
    }
}
