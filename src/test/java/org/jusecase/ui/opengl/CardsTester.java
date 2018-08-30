package org.jusecase.ui.opengl;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.time.Timer;
import org.jusecase.scenegraph.tween.Tweens;
import org.jusecase.scenegraph.tween.animations.QuadraticOut;
import org.jusecase.scenegraph.tween.properties.FloatProperty;
import org.jusecase.ui.Ui;
import org.jusecase.ui.elements.Button;
import org.jusecase.ui.signal.OnResize;
import org.jusecase.ui.style.ButtonStyle;
import org.jusecase.ui.touch.TouchEvent;

import javax.inject.Inject;

@Component
public class CardsTester implements Application, OnResize {

    @Inject ApplicationBackend applicationBackend;
    @Inject Timer timer;

    private Ui ui = new Ui();
    private Tweens tweens = new Tweens();
    private Cards cards;

    public static void main(String[] args) {
        new LwjglApplicationBackend(CardsTester.class).start();
    }

    @Override
    public void init() {
        initStyles();

        cards = new Cards();
        ui.add(cards);

        Button button = new Button();
        button.setSize(200, 50);
        button.onClick.add(e -> cards.addCard());
        ui.add(button);

        applicationBackend.onResize().add(this);
        onResize(applicationBackend.getWidth(), applicationBackend.getHeight());
    }

    private void initStyles() {
        ButtonStyle buttonStyle = new ButtonStyle();
        buttonStyle.active.color = new Color("#fff", 0.5f);
        buttonStyle.hovered.color = new Color("#f0f", 0.5f);
        buttonStyle.pressed.color = new Color("#fff", 0.5f);
        ui.setDefaultStyle(Button.class, buttonStyle);
    }

    @Override
    public void process(TouchEvent touchEvent) {
        ui.process(touchEvent);
    }

    @Override
    public void update() {
        tweens.update(timer.dt());
    }

    @Override
    public void render(Renderer renderer) {
        renderer.render(ui);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void onResize(int width, int height) {
        cards.setX(width * 0.5f);
        cards.setY(height * 0.95f);
    }

    private class Card extends Quad {
    }

    private class Cards extends Node2d {

        public void addCard() {
            CardsTester.Card card = new CardsTester.Card();
            card.setSize(150, 200);
            card.setPivot(0.5f, 1.0f);
            card.setColor(new Color().randomHue());
            add(card);

            float startX = -0.5f * card.getWidth() * getChildCount(CardsTester.Card.class);
            visitAllDirectChildren(CardsTester.Card.class, (c, index) -> layoutCard(c, index, startX));
        }

        private void layoutCard(CardsTester.Card card, int index, float startX) {
            float targetX = startX + card.getWidth() * index;
            float targetY = 0; // TODO
            float targetRotation = 0;//180 * (float)Math.random(); // TODO
            tweens.tween(card)
                    .duration(1.0f)
                    .animation(QuadraticOut.animation)
                    .property(new FloatProperty(card.getX(), targetX, card::setX))
                    .property(new FloatProperty(card.getY(), targetY, card::setY))
                    .property(new FloatProperty(card.getRotation(), targetRotation, card::setRotation))
            ;
        }
    }
}
