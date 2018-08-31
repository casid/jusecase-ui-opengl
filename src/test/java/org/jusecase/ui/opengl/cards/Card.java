package org.jusecase.ui.opengl.cards;

import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.ui.elements.Element;


public class Card extends Element {
    private Quad    quad;
    private boolean selected;
    private boolean justDeselected;
    private boolean grabbed;

    public Card() {
        quad = new Quad();
        add(quad);
    }

    public void animateX( float x ) {
        if (!grabbed) {
            setX(x);
        }
    }

    public void animateY( float y ) {
        if (!grabbed) {
            setY(y);
        }
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    public void setColor( Color color) {
        quad.setColor(color);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setGrabbed( boolean grabbed ) {
        this.grabbed = grabbed;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public Node2d setSize(float width, float height) {
        quad.setSize(width, height);
        return super.setSize(width, height);
    }

    public boolean isJustDeselected() {
        return justDeselected;
    }

    public void setJustDeselected(boolean justDeselected) {
        this.justDeselected = justDeselected;
    }

    public float getAlpha() {
        return quad.getColor().a;
    }

    public void setAlpha(float alpha) {
        quad.getColor().a = alpha;
    }
}


