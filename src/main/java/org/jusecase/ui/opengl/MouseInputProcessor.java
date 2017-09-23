package org.jusecase.ui.opengl;

import org.jusecase.ui.touch.TouchEvent;
import org.jusecase.ui.touch.TouchPhase;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInputProcessor {
    private final long window;
    private final TouchEvent currentEvent = new TouchEvent();
    private boolean hasChanges;

    public MouseInputProcessor(long window) {
        this.window = window;
        init();
    }

    public void init() {
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    currentEvent.phase = TouchPhase.Begin;
                } else {
                    currentEvent.phase = TouchPhase.End;
                }
                hasChanges = true;
            }
        });

        glfwSetCursorPosCallback(window, (window, x, y) -> {
            if (!hasChanges) {
                if (currentEvent.phase == TouchPhase.Begin) {
                    currentEvent.phase = TouchPhase.Move;
                }

                if (currentEvent.phase == TouchPhase.End) {
                    currentEvent.phase = TouchPhase.Hover;
                }
            }

            if (!hasChanges) {
                currentEvent.deltaX = (float)x - currentEvent.x;
                currentEvent.deltaY = (float)y - currentEvent.y;
            } else {
                currentEvent.deltaX += (float)x - currentEvent.x;
                currentEvent.deltaY += (float)y - currentEvent.y;
            }

            currentEvent.x = (float)x;
            currentEvent.y = (float)y;
            hasChanges = true;
        });
    }

    public TouchEvent poll() {
        if (hasChanges) {
            hasChanges = false;
            return currentEvent.clone();
        }
        return null;
    }
}
