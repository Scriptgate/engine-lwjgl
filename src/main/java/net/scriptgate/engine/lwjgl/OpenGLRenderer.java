package net.scriptgate.engine.lwjgl;

import net.scriptgate.common.Color4f;
import net.scriptgate.common.Point;
import net.scriptgate.common.Rectangle;
import net.scriptgate.engine.Renderer;
import net.scriptgate.engine.image.ImageLoader;
import net.scriptgate.engine.lwjgl.texture.Texture;
import net.scriptgate.engine.lwjgl.texture.TextureLoader;
import org.lwjgl.opengl.GL11;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class OpenGLRenderer implements Renderer {

    private final Color4f color;
    private static final float DEG2RAD = 3.14159f / 180;
    private final ImageLoader<Texture> imageLoader;
    private static final OpenGLTTFRenderer fontRenderer = new OpenGLTTFRenderer();

    public OpenGLRenderer() {
        imageLoader = new TextureLoader();
        color = new Color4f(1, 1, 1, 1);
    }

    @Override
    public void drawImage(int x, int y, String path) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        Texture texture = imageLoader.getTexture(path);
        int width = texture.getWidth();
        int height = texture.getHeight();
        texture.bind();

        GL11.glPushMatrix();
//      middle center
//      glTranslatef(x - width / 2, y - height / 2, 0);
//      top middle
        GL11.glTranslatef(x - width / 2, y, 0);
//      top left
//      glTranslatef(x, y, 0);

        GL11.glBegin(GL11.GL_QUADS);
        {
            drawBoxedTexCoords(
                    0, 0, width, height,
                    0, 0, texture.s1(), texture.t1());
        }
        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void drawImage(String imagePath, Point position, Point offset, Point size) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        Texture texture = imageLoader.getTexture(imagePath);
        texture.bind();

        GL11.glPushMatrix();

        int width = texture.getWidth();

//      int height = texture.getHeight();
//      middle center
//      glTranslatef(position.x - width / 2, position.y - height / 2, 0);
//      top left
//      glTranslatef(position.x, position.y, 0);
        GL11.glTranslatef(position.x - width / 2, position.y, 0);

        GL11.glBegin(GL11.GL_QUADS);
        {
            float s0 = texture.getPercentageOfWidth(offset.x);
            float t0 = texture.getPercentageOfHeight(offset.y);
            float s1 = texture.getPercentageOfWidth(offset.x + size.x);
            float t1 = texture.getPercentageOfHeight(offset.y + size.y);

            drawBoxedTexCoords(
                    0, 0, size.x, size.y,
                    s0, t0, s1, t1);
        }
        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    /**
     * @param x0 the x coordinate of the first corner of the destination rectangle.
     * @param y0 the y coordinate of the first corner of the destination rectangle.
     * @param x1 the x coordinate of the second corner of the destination rectangle.
     * @param y1 the y coordinate of the second corner of the destination rectangle.
     * @param s0 the x component of the first corner of the source rectangle.
     *           This is a percentage of the width of the texture.
     * @param t0 the y component of the first corner of the source rectangle.
     *           This is a percentage of the height of the texture.
     * @param s1 the x component of the second corner of the source rectangle.
     *           This is a percentage of the width of the texture.
     * @param t1 the y component of the second corner of the source rectangle.
     *           This is a percentage of the height of the texture.
     */
    //@formatter:off
    public void drawBoxedTexCoords(float x0, float y0, float x1, float y1, float s0, float t0, float s1, float t1) {
        GL11.glTexCoord2f(s0, t0);   GL11.glVertex2f(x0, y0);
        GL11.glTexCoord2f(s1, t0);   GL11.glVertex2f(x1, y0);
        GL11.glTexCoord2f(s1, t1);   GL11.glVertex2f(x1, y1);
        GL11.glTexCoord2f(s0, t1);   GL11.glVertex2f(x0, y1);
    }
    //@formatter:on

    @Override
    public void drawRect(int x, int y, int width, int height) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        int adjustedWidthToBorder = width - 1;
        int adjustedHeightToBorder = height - 1;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        {
            GL11.glVertex2f(0, 0);
            GL11.glVertex2f(adjustedWidthToBorder, 0);
            GL11.glVertex2f(adjustedWidthToBorder, adjustedHeightToBorder);
            GL11.glVertex2f(0, adjustedHeightToBorder);
        }
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public Rectangle drawText(int x, int y, String text) {
        return fontRenderer.render(this, x, y, text);
    }

    @Override
    public void fillCircle(int x, int y, int radius) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPushMatrix();

        GL11.glVertex2f(x, y);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        {
            for (int i = 0; i < 360; i++) {
                float degInRad = i * DEG2RAD;
                GL11.glVertex2d(
                        x + radius * cos(degInRad),
                        y + radius * sin(degInRad));
            }
        }
        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPushMatrix();

        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glVertex2f(x1, y1);
            GL11.glVertex2f(x2, y2);
        }
        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void drawCircle(int x, int y, int radius) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPushMatrix();

        GL11.glVertex2f(x, y);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        {
            for (int i = 0; i < 360; i++) {
                float degInRad = i * DEG2RAD;
                GL11.glVertex2d(
                        x + radius * cos(degInRad),
                        y + radius * sin(degInRad));
            }
        }
        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPushMatrix();

        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glVertex2f(x, y);
            GL11.glVertex2f(x + width, y);
            GL11.glVertex2f(x + width, y + height);
            GL11.glVertex2f(x, y + height);
        }
        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public Rectangle getBounds(int x, int y, String text) {
        return fontRenderer.getBounds(x, y, text);
    }

    /**
     * Unsafe version of {@link #getBounds(int, int, String) getBounds}
     * @param x x-coordinate to calculate bounds at
     * @param y y-coordinate to calculate bounds at
     * @param text The text to calculate the bounds of
     * @return The bounds of rendering text at (x,y)
     */
    public static Rectangle _getBounds(int x, int y, String text) {
        return fontRenderer.getBounds(x,y,text);
    }

    @Override
    public void setColor(float r, float g, float b) {
        color.r = r;
        color.g = g;
        color.b = b;
        GL11.glColor4f(color.r, color.g, color.b, color.a);
    }

    @Override
    public void setColor(float a, float r, float g, float b) {
        color.a = a;
        color.r = r;
        color.g = g;
        color.b = b;
        GL11.glColor4f(color.r, color.g, color.b, color.a);
    }

    @Override
    public void setOpacity(float a) {
        color.a = a;
        GL11.glColor4f(color.r, color.g, color.b, color.a);
    }

    @Override
    public void translate(int x, int y) {
        GL11.glTranslatef(x, y, 0);
    }

    public void destroy() {
        fontRenderer.destroy();
    }

    public void initialize() {
        fontRenderer.initialize();
    }
}
