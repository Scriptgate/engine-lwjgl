package net.scriptgate.engine.lwjgl;

import net.scriptgate.engine.Application;
import net.scriptgate.engine.Engine;
import net.scriptgate.engine.Renderer;

import static net.scriptgate.common.Color3f.RED;

public class OpenGLRendererTest implements Application {

    public static void main(String[] args) {
        new OpenGLApplicationHandler().start(new OpenGLRendererTest());
    }

    @Override
    public void initializeProperties() {
        Engine.WIDTH = 512;
        Engine.HEIGHT = 512;
    }

    @Override
    public void render(Renderer renderer) {
        renderer.setColor(RED);
        renderer.drawLine(0, 0, 0, 512);
    }
}
