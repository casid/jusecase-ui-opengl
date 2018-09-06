package org.jusecase.ui.opengl.cards;

import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.tween.Tweens;
import org.jusecase.scenegraph.tween.animations.QuadraticOut;
import org.jusecase.scenegraph.tween.properties.FloatProperty;
import org.jusecase.ui.elements.Element;
import org.jusecase.ui.signal.OnHover;
import org.jusecase.ui.signal.OnTouch;
import org.jusecase.ui.touch.TouchEvent;
import org.jusecase.ui.touch.TouchPhase;

import javax.inject.Inject;


@Component
public class Cards extends Node2d implements OnHover, OnTouch {

    @Inject
    private ApplicationBackend applicationBackend;
    @Inject
    private Tweens tweens;

    public void addCard() {
        Card card = new Card();
        card.setPosition(applicationBackend.getWidth(), 0);
        card.setRotation(-60);
        card.setSize(getCardWidth(), getCardHeight());
        card.setPivot(0.5f, 1.0f);
        card.setColor(new Color().randomHue());
        card.onHover.add(this);
        card.onTouch.add(this);
        add(card);

        layout();
    }

    private float getCardWidth() {
        return 150;
    }

    private float getCardHeight() {
        return 200;
    }

    public void layout() {
        float distanceX = 0.8f * getCardWidth();
        float startX = -0.5f * distanceX * getChildCount(Card.class) + 0.5f * distanceX;

        visitAllDirectChildren(Card.class, (c, index) -> layoutCard(c, index, startX, distanceX));
    }

    private void layoutCard(Card card, int index, float startX, float distanceX) {
        if (card.isGrabbed()) {
            return;
        }

        float duration = 1.0f;
        float alpha = 1.0f;
        float scale = 1.0f;

        float targetX = startX + distanceX * index;

        float normalizedX = 2.0f * targetX / applicationBackend.getWidth();
        float targetY = normalizedX * normalizedX * 100; // TODO constant

        float targetRotation = -normalizedX * 20;

        if (card.isSelected()) {
            targetY -= 20; // TODO constant
            targetRotation *= 0.4f;
            duration = 0.2f;
            scale = 1.2f;
        }

        if (card.isJustDeselected()) {
            duration = 0.4f;
        }

        Card sibling = getSibling(card, -1, Card.class);
        if (sibling != null && sibling.isSelected()) {
            alpha = 0.2f;
        }

        tweens.tween(card)
                .duration(duration)
                .animation(QuadraticOut.animation)
                .property(new FloatProperty(card.getX(), targetX, card::animateX))
                .property(new FloatProperty(card.getY(), targetY, card::animateY))
                .property(new FloatProperty(card.getRotation(), targetRotation, card::setRotation))
                .property(new FloatProperty(card.getScaleX(), scale, card::setScale))
                .property(new FloatProperty(card.getAlpha(), alpha, card::setAlpha))
        ;
    }

    @Override
    public void onHover(Element element, boolean started) {
        Card card = (Card) element;
        card.setSelected(started);
        card.setJustDeselected(!started);
        layout();
    }

    @Override
    public void onTouch(TouchEvent touchEvent) {
        Card card = (Card) touchEvent.element;

        if (touchEvent.phase == TouchPhase.Begin) {
            card.setGrabbed(true);
        } else if (touchEvent.phase == TouchPhase.End) {
            card.setGrabbed(false);
            card.setSelected(false);
            card.setJustDeselected(true);
            layout();
        } else if (touchEvent.phase == TouchPhase.Move) {
            card.setX(card.getX() + touchEvent.deltaX);
            card.setY(card.getY() + touchEvent.deltaY);
        }
    }
}
