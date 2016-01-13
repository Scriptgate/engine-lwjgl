package net.scriptgate.engine.lwjgl;

import net.scriptgate.engine.Application;
import net.scriptgate.engine.Renderer;


public class EasyFont implements Application {

    public static void main(String[] args) {
        new OpenGLApplicationHandler().start(new EasyFont());
    }

    private EasyFontRenderer fontRenderer;
    private String text;

    public EasyFont() {
        this.fontRenderer = new EasyFontRenderer();
    }

    @Override
    public void initialize() {
        fontRenderer.initialize();
        text = "1234567111";
        System.out.println(text.length());
    }

    @Override
    public void render(Renderer renderer) {
        fontRenderer.render(10, 10, text);
    }

    @Override
    public void destroy() {
        fontRenderer.destroy();
    }
}
