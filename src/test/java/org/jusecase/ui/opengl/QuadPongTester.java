package org.jusecase.ui.opengl;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.time.Timer;
import org.jusecase.ui.touch.TouchEvent;

import javax.inject.Inject;

@Component
public class QuadPongTester implements Application {

    @Inject
    private ApplicationBackend applicationBackend;
    @Inject
    private Timer timer;

    private Node2d scene = new Node2d();
    private Ball ball;

    public static void main(String[] args) {
        new LwjglApplicationBackend(QuadPongTester.class).start();
    }

    @Override
    public void init() {
        ball = new Ball();
        ball.setWidth(20.0f);
        ball.setHeight(20.0f);
        ball.speedX = ball.speedY = 500.0f;
        moveBallToCenter();
        scene.add(ball);
    }

    private void moveBallToCenter() {
        ball.setX((applicationBackend.getWidth() - ball.getWidth()) / 2);
        ball.setY((applicationBackend.getHeight() - ball.getHeight()) / 2);
    }

    @Override
    public void process(TouchEvent touchEvent) {

    }

    @Override
    public void update() {
        ball.setX(ball.getX() + ball.speedX * timer.dt());
        ball.setY(ball.getY() + ball.speedY * timer.dt());

        if (ball.getX() < 0.0f || ball.getX() + ball.getWidth() > applicationBackend.getWidth()) {
            ball.reflectX();
        }

        if (ball.getY() < 0.0f || ball.getY() + ball.getHeight() > applicationBackend.getHeight()) {
            ball.reflectY();
        }
    }

    @Override
    public void render(Renderer renderer) {
        renderer.render(scene);
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    static class Ball extends Quad {
        private float speedX;
        private float speedY;

        public void reflectX() {
            speedX = -speedX;
            changeColor();
        }

        public void reflectY() {
            speedY = -speedY;
            changeColor();
        }

        public void changeColor() {
            getColor().randomHue();
        }
    }
}
