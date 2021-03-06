package net.scriptgate.engine.lwjgl.texture;

import net.scriptgate.engine.image.ImageLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static java.awt.Transparency.OPAQUE;
import static java.awt.Transparency.TRANSLUCENT;
import static java.awt.color.ColorSpace.CS_sRGB;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.awt.image.Raster.createInterleavedRaster;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.ByteOrder.nativeOrder;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_BASE_LEVEL;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;

public class TextureLoader extends ImageLoader<Texture> {

    private final ColorModel glAlphaColorModel;
    private final ColorModel glColorModel;
    private final IntBuffer textureIDBuffer;

    public TextureLoader() {
        super();
        glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(CS_sRGB), new int[]{8, 8, 8, 8}, true, false, TRANSLUCENT, TYPE_BYTE);
        glColorModel = new ComponentColorModel(ColorSpace.getInstance(CS_sRGB), new int[]{8, 8, 8, 0}, false, false, OPAQUE, TYPE_BYTE);
        textureIDBuffer = createIntBuffer(1);
    }

    @Override
    public Texture loadTexture(String path) {
        glEnable(GL_TEXTURE_2D);

        int textureID = createTextureID();
        Texture texture = new Texture(textureID);
        texture.bind();

        BufferedImage bufferedImage = loadImage(path);

        int srcPixelFormat;
        if (bufferedImage.getColorModel().hasAlpha()) {
            srcPixelFormat = GL_RGBA;
        } else {
            srcPixelFormat = GL_RGB;
        }

        ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);


        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
                texture.getBinaryWidth(), texture.getBinaryHeight(), 0,
                srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);

        //The texture magnification function is used when the pixel being textured maps to an area less than or equal to one texture element
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        //The texture minifying function is used whenever the pixel being textured maps to an area greater than one texture element
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glDisable(GL_TEXTURE_2D);

        return texture;
    }

    private int createTextureID() {
        glGenTextures(textureIDBuffer);
        return textureIDBuffer.get(0);
    }

    private ByteBuffer convertImageData(BufferedImage bufferedImage, Texture texture) {
        texture.setWidth(bufferedImage.getWidth());
        texture.setHeight(bufferedImage.getHeight());

        BufferedImage textureImage;
        if (bufferedImage.getColorModel().hasAlpha()) {
            WritableRaster raster = createInterleavedRaster(TYPE_BYTE, texture.getBinaryWidth(), texture.getBinaryHeight(), 4, null);
            textureImage = new BufferedImage(glAlphaColorModel, raster, false, null);
        } else {
            WritableRaster raster = createInterleavedRaster(TYPE_BYTE, texture.getBinaryWidth(), texture.getBinaryHeight(), 3, null);
            textureImage = new BufferedImage(glColorModel, raster, false, null);
        }

        Graphics g = textureImage.getGraphics();
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, texture.getBinaryWidth(), texture.getBinaryHeight());
        g.drawImage(bufferedImage, 0, 0, null);

        byte[] data = ((DataBufferByte) textureImage.getRaster().getDataBuffer()).getData();

        ByteBuffer imageBuffer = allocateDirect(data.length);
        imageBuffer.order(nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();

        return imageBuffer;
    }

}
