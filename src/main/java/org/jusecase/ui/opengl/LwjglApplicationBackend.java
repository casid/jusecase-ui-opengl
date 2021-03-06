package org.jusecase.ui.opengl;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Injector;
import org.jusecase.scenegraph.render.PaintersAlgorithmRenderer;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.time.CurrentTime;
import org.jusecase.scenegraph.time.NextFrameScheduler;
import org.jusecase.scenegraph.time.StopWatch;
import org.jusecase.signals.Signal;
import org.jusecase.ui.font.BitmapFontLoader;
import org.jusecase.ui.input.Event;
import org.jusecase.ui.input.InputProcessor;
import org.jusecase.ui.opengl.input.MouseScrollProcessor;
import org.jusecase.ui.opengl.input.MouseTouchProcessor;
import org.jusecase.ui.opengl.render.VboRenderer;
import org.jusecase.ui.opengl.texture.atlas.StarlingTextureAtlasLoader;
import org.jusecase.ui.opengl.texture.stbi.StbiTextureLoader;
import org.jusecase.ui.opengl.util.ScreenConverter;
import org.jusecase.ui.signal.OnResize;
import org.jusecase.ui.signal.OnResizeListener;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LwjglApplicationBackend implements ApplicationBackend {

    private final OnResize onResize = new OnResize();
    private Class<? extends Application> applicationClass;
    private Application application;
    private long window;
    private List<InputProcessor> inputProcessors = new ArrayList<>();
    private StopWatch stopWatch;
    private NextFrameScheduler nextFrame;
    private ScreenConverter screenConverter;
    private Renderer renderer;

    public LwjglApplicationBackend(Class<? extends Application> applicationClass) {
        this.applicationClass = applicationClass;
    }

    protected void render() {
        application.render(renderer);
    }

    protected void dispose() {
        renderer.dispose();
        application.dispose();
    }

    public void start() {
        screenConverter = new ScreenConverter();
        initWindow();

        Injector injector = Injector.getInstance();
        injector.add(this);
        injector.add(new StbiTextureLoader());
        injector.add(new StarlingTextureAtlasLoader());
        injector.add(new BitmapFontLoader());
        injector.add(new CurrentTime());
        injector.add(stopWatch = new StopWatch());
        injector.add(nextFrame = new NextFrameScheduler());
        injector.add(screenConverter);

        inputProcessors.add(new MouseTouchProcessor(window));
        inputProcessors.add(new MouseScrollProcessor(window));

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
            stopWatch.start();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            update();
            render();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            stopWatch.stop();
        }
    }

    private void update() {
        nextFrame.run();

        for (InputProcessor inputProcessor : inputProcessors) {
            Event event = inputProcessor.poll();
            if (event != null) {
                application.process(event);
            }
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
            screenConverter.setNative(width, height);
            glViewport(0, 0, width, height);
            onResize.dispatch(width, height);
        });

        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            screenConverter.setWindow(width, height);
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
            screenConverter.setWindow(pWidth.get(0), pHeight.get(0));

            glfwGetFramebufferSize(window, pWidth, pHeight);
            screenConverter.setNative(pWidth.get(0), pHeight.get(0));

        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
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
        return screenConverter.getNativeWidth();
    }

    @Override
    public int getHeight() {
        return screenConverter.getNativeHeight();
    }

    @Override
    public Signal<OnResizeListener> onResize() {
        return onResize;
    }
}
