package org.jusecase.ui.opengl;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Injector;
import org.jusecase.scenegraph.render.PaintersAlgorithmRenderer;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.signals.Signal;
import org.jusecase.ui.opengl.render.VboRenderer;
import org.jusecase.ui.opengl.texture.atlas.StarlingTextureAtlasLoader;
import org.jusecase.ui.opengl.texture.stbi.StbiTextureLoader;
import org.jusecase.ui.opengl.touch.MouseInputProcessor;
import org.jusecase.ui.signal.OnResize;
import org.jusecase.ui.touch.TouchEvent;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LwjglApplicationBackend implements ApplicationBackend {

    private Class<? extends Application> applicationClass;
    private Application application;

    private long window;
    private int width;
    private int height;

    private MouseInputProcessor mouseInputProcessor;

    private final Signal<OnResize> onResize = new Signal<>();

    protected LwjglApplicationBackend(Class<? extends Application> applicationClass) {
        this.applicationClass = applicationClass;
    }

    private Renderer renderer;


    protected void render() {
        application.render(renderer);
    }

    protected void dispose() {
        renderer.dispose();
        application.dispose();
    }

    public void start() {
        initWindow();

        Injector.getInstance().add(this);
        Injector.getInstance().add(new StbiTextureLoader());
        Injector.getInstance().add(new StarlingTextureAtlasLoader());

        renderer = new PaintersAlgorithmRenderer(new VboRenderer());

        try {
            application = applicationClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        application.init();

        loop();

        dispose();
        disposeWindow();
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            update();
            render();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void update() {
        TouchEvent event = mouseInputProcessor.poll();
        if (event != null) {
            application.process(event);
        }
        application.update();
    }


    private void initWindow() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        //glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                exit();
            }
        });

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            this.width = width;
            this.height = height;
            glViewport(0, 0, this.width, this.height);

            onResize.dispatch(s -> s.onResize(this.width, this.height));
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            width = pWidth.get(0);
            height = pHeight.get(0);

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        mouseInputProcessor = new MouseInputProcessor(window);
    }

    private void disposeWindow() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    @Override
    public void exit() {
        glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Signal<OnResize> onResize() {
        return onResize;
    }
}
