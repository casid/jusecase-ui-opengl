package org.jusecase.ui.opengl;

import org.jusecase.scenegraph.Image;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureLoader;
import org.jusecase.ui.Ui;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.ui.elements.Button;
import org.jusecase.ui.opengl.render.SimpleRenderer;
import org.jusecase.scenegraph.render.PaintersAlgorithmRenderer;
import org.jusecase.ui.opengl.render.VboRenderer;
import org.jusecase.ui.opengl.texture.stbi.StbiTextureLoader;
import org.jusecase.ui.style.ImageButtonStyle;
import org.jusecase.ui.style.QuadButtonStyle;
import org.jusecase.ui.touch.TouchEvent;
import org.jusecase.ui.touch.TouchPhase;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Playground {

    private Ui ui = new Ui();
    private Button button = new Button();

    private MouseInputProcessor mouseInputProcessor;
    private Renderer simpleRenderer = new VboRenderer();
    private Renderer paintersAlgorithmRenderer = new PaintersAlgorithmRenderer(simpleRenderer);

    private long window;

    public static void main(String[] args) {
        new Playground().run();
    }

    public void run() {
        initWindow();
        init();
        loop();
        disposeWindow();
    }

    private void init() {
        mouseInputProcessor = new MouseInputProcessor(window);

        ui.setDefaultStyle(Button.class, new QuadButtonStyle());

        button.setX(100).setY(100).setWidth(200).setHeight(50);
        button.onClick.add(e -> {
            QuadButtonStyle style = (QuadButtonStyle) button.getStyle();
            Color color = new Color(Math.random(), Math.random(), Math.random());
            style.active.setColor(color);
            style.hovered.setColor(color);
            System.out.println("Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

            Button randomButton = new Button();
            button.add(randomButton.setPosition(800 * Math.random(), 600 * Math.random()).setSize(20, 20).setRotation(90 * Math.random()));
        });
        ui.add(button);

        Button moveableButton = new Button();
        ui.add(moveableButton.setX(120).setY(120).setWidth(200).setHeight(200).setRotation(20));
        moveableButton.onTouch.add(touchEvent -> {
            if (moveableButton.isPressed() && touchEvent.phase == TouchPhase.Move) {
                moveableButton.setX(moveableButton.getX() + touchEvent.deltaX);
                moveableButton.setY(moveableButton.getY() + touchEvent.deltaY);
            }
        });

        Button moveableButtonChild = new Button();
        moveableButton.add(moveableButtonChild.setX(120).setY(120).setWidth(20).setHeight(20).setRotation(45).setScaleX(10).setScaleY(2));

        Button moveableButtonChild2 = new Button();
        moveableButton.add(moveableButtonChild2.setX(120).setY(120).setWidth(20).setHeight(20));

        TextureLoader textureLoader = new StbiTextureLoader();
        try {
            Texture texture = textureLoader.load("images/0000_poisondagger_128.jpg");
            Image image = new Image();
            image.setTexture(texture);
            ui.addFirst(image.setX(200).setY(200));

            ImageButtonStyle style = new ImageButtonStyle();
            style.active.setTexture(texture);
            style.hovered.setTexture(texture).setColor(new Color("#0f0"));
            style.pressed.setTexture(texture).setColor(new Color("#f00"));

            for (int i = 0; i < 10; ++i) {
                Button textureButton = new Button();

                textureButton.setStyle(style);
                ui.addFirst(textureButton.setX(10 + Math.min(i, 400)).setY(300));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void update() {
        TouchEvent event = mouseInputProcessor.poll();
        if (event != null) {
            ui.process(event);
        }
    }

    private void draw() {
        paintersAlgorithmRenderer.render(ui);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            update();
            draw();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void initWindow() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
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
}
