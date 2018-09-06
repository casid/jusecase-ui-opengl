package org.jusecase.ui.opengl.input;

import org.jusecase.ui.input.InputProcessor;
import org.jusecase.ui.input.ScrollEvent;

import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

public class MouseScrollProcessor implements InputProcessor {
    private final long window;
    private final ScrollEvent currentEvent = new ScrollEvent();
    private final ScrollEvent polledEvent = new ScrollEvent();
    private boolean hasChanges;

    public MouseScrollProcessor(long window) {
        this.window = window;
        init();
    }

    public void init() {
        glfwSetScrollCallback(window, (window, x, y) -> {
            if (!hasChanges) {
                currentEvent.deltaX = (float) x;
                currentEvent.deltaY = (float) y;
            } else {
                currentEvent.deltaX += (float) x;
                currentEvent.deltaY += (float) y;
            }

            hasChanges = true;
        });
    }

    @Override
    public ScrollEvent poll() {
        if (hasChanges) {
            hasChanges = false;
            return currentEvent.copyTo(polledEvent);
        }
        return null;
    }
}
