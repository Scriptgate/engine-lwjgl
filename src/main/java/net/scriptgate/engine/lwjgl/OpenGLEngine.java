package net.scriptgate.engine.lwjgl;

import net.scriptgate.engine.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libffi.Closure;

import static java.lang.System.exit;
import static net.scriptgate.engine.Engine.HEIGHT;
import static net.scriptgate.engine.Engine.WIDTH;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;


public class OpenGLEngine extends EngineBase {

    protected final Application application;
    protected final OpenGLRenderer renderer;

    private long window;

    private final GLFWWindowSizeCallback windowSizeCallback;
    private final GLFWFramebufferSizeCallback framebufferSizeCallback;

    private final GLFWKeyCallback keyCallback;

    private final GLFWMouseButtonCallback mouseButtonCallback;
    private final GLFWCursorPosCallback cursorPosCallback;

    private final GLFWErrorCallback errorCallback;
    private Closure debugCallback;


    public OpenGLEngine(Application application, InputComponent input) {
        super(input);
        this.application = application;
        this.renderer = new OpenGLRenderer();

//      TODO: Is it possible to redirect error callback to log?
        errorCallback = GLFWErrorCallback.createPrint(System.err);

        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int keyCode, int scancode, int action, int mods) {
                String keyName = GLFW.glfwGetKeyName(keyCode, scancode);
                boolean shiftPressed = (mods & GLFW_MOD_SHIFT) != 0;
                boolean ctrlPressed = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
                input.setShiftPressed(shiftPressed);
                input.setCtrlPressed(ctrlPressed);
                Key key = new Key(keyCode, keyName, shiftPressed, ctrlPressed);
                switch (action) {
                    case GLFW.GLFW_RELEASE:
                        input.keyReleased(key);
                        break;
                    case GLFW.GLFW_PRESS:
                        input.keyPressed(key);
                        application.onKeyDown(key);
                        break;
                    case GLFW.GLFW_REPEAT:
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Unsupported key action: 0x%X", action));
                }
                if (keyCode == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                    GLFW.glfwSetWindowShouldClose(window, GL11.GL_TRUE);
                }
            }
        };

        mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                switch (action) {
                    case GLFW.GLFW_RELEASE:
                        input.setMouseReleased();
                        application.onClick(input.getMouseX(), input.getMouseY());
                        break;
                    case GLFW.GLFW_PRESS:
                        input.setMousePressed();
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Unsupported mouse button action: 0x%X", action));
                }
            }
        };

        cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                input.mouseMoved((int) xpos, (int) ypos);
            }
        };

        windowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
//              TODO: Support resizing
//              Engine.WIDTH = width;
//              Engine.HEIGHT = height;

                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();

                GL11.glOrtho(0.0f, width, height, 0.0f, -1.0f, 1.0f);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
                GL11.glTranslatef(0.375f, 0.375f, 0.0f);
            }
        };

        framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
/*
                While the size of a window is measured in screen coordinates, OpenGL works with pixels.
                The size you pass into glViewport, for example, should be in pixels. On some machines screen
                coordinates and pixels are the same, but on others they will not be. There is a second
                set of functions to retrieve the size, in pixels, of the framebuffer of a window.

                If you wish to be notified when the framebuffer of a window is resized, whether by the user
                or the system, set a size callback.
*/
                GL11.glViewport(0, 0, width, height);
            }
        };
    }

    @Override
    protected void onTick(InputComponent inputComponent, double elapsedTime) {
        GLFW.glfwPollEvents();
        application.onTick(inputComponent, elapsedTime);
    }

    @Override
    protected void onUpdate(int ticks, int frames) {
        application.onUpdate(ticks, frames);
    }

    @Override
    protected boolean isRunning() {
        return GLFW.glfwWindowShouldClose(window) == GL11.GL_FALSE;
    }

    @Override
    public void start() {
        errorCallback.set();

        if (GLFW.glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);

        scheduler.execute(this);
    }

    @Override
    protected void initialize() {
        window = glfwCreateWindow(WIDTH, HEIGHT, Engine.TITLE, MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }

        windowSizeCallback.set(window);
        framebufferSizeCallback.set(window);
        keyCallback.set(window);
        mouseButtonCallback.set(window);
        cursorPosCallback.set(window);

//      Center window
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2);


//      Create context
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        debugCallback = GLUtil.setupDebugMessageCallback();

        GLFW.glfwSwapInterval(Engine.verticalSyncDisabled ? 0 : 1);

/*
        https://www.opengl.org/wiki/GLSL_:_common_mistakes#Enable_Or_Not_To_Enable
        Enable Or Not To Enable

        With fixed pipeline, you needed to call glEnable(GL_TEXTURE_2D) to enable 2D texturing.
        You also needed to call glEnable(GL_LIGHTING). Since shaders override these functionalities,
        you don't need to glEnable/glDisable. If you don't want texturing, you either need to write
        another shader that doesn't do texturing, or you can attach an all-white or all-black texture,
        depending on your needs. You can also write one shader that does lighting and one that doesn't.
        For things that are not overriden by shaders, like the alpha test, depth test, stencil test,
        calling glEnable/glDisable will have an effect.
*/

//      2D, in-order-rendering, disable depth test
        GL11.glDisable(GL11.GL_DEPTH_TEST);
//      We use transparency, so enable blending
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glClearColor(Engine.BG_COLOR.r, Engine.BG_COLOR.g, Engine.BG_COLOR.b, 1.0f);

        renderer.initialize();

        application.initialize();

        GLFW.glfwShowWindow(window);
        Callbacks.glfwInvoke(window, windowSizeCallback, framebufferSizeCallback);
    }

    @Override
    protected void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        application.render(renderer);

        GLFW.glfwSwapBuffers(window);
    }

    @Override
    public void destroy() {
        application.destroy();
        renderer.destroy();

        try {
            if (debugCallback != null) {
                debugCallback.release();
            }
            GLFW.glfwDestroyWindow(window);
            keyCallback.release();
            mouseButtonCallback.release();
            cursorPosCallback.release();
            framebufferSizeCallback.release();
            windowSizeCallback.release();
        } finally {
            GLFW.glfwTerminate();
            errorCallback.release();
        }
        exit(0);
    }

    @Override
    protected void initializeProperties() {
        super.initializeProperties();
        application.initializeProperties();
    }

}
