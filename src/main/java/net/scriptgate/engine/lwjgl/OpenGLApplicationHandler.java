package net.scriptgate.engine.lwjgl;

import net.scriptgate.engine.Application;
import net.scriptgate.engine.ApplicationHandler;

public class OpenGLApplicationHandler implements ApplicationHandler {
    @Override
    public void start(Application app) {
        new OpenGLEngine(app, new OpenGLInputComponent()).start();
    }
}
