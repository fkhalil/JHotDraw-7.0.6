/*
 * @(#)LabeledLineConnection.java  1.1  2006-02-14
 *
 * Copyright (c) 1996-2006 by the original authors of JHotDraw
 * and all its contributors ("JHotDraw.org")
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JHotDraw.org ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * JHotDraw.org.
 */

package org.jhotdraw.draw;

import org.jhotdraw.util.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.event.*;

/**
 * A LineConnection with labels.
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-02-14 Do not include labels in logical bounds.
 * <br>1.0 23. Januar 2006 Created.
 */
public class LabeledLineConnectionFigure extends LineConnectionFigure
        implements CompositeFigure {
    
    private Layouter layouter;
    private ArrayList<Figure> children = new ArrayList();
    //private Rectangle2D.Double bounds;
    private Rectangle2D.Double drawBounds;
    
    /**
     * Handles figure changes in the children.
     */
    private ChildHandler childHandler = new ChildHandler(this);
    private static class ChildHandler implements FigureListener, UndoableEditListener {
        private LabeledLineConnectionFigure owner;
        private ChildHandler(LabeledLineConnectionFigure owner) {
            this.owner = owner;
        }
        @Override
		public void figureRequestRemove(FigureEvent e) {
            owner.remove(e.getFigure());
        }
        
        @Override
		public void figureRemoved(FigureEvent evt) {
        }
        
        @Override
		public void figureChanged(FigureEvent e) {
            if (! owner.isChanging()) {
                owner.willChange();
                owner.fireFigureChanged(e);
                owner.changed();
            }
        }
        
        @Override
		public void figureAdded(FigureEvent e) {
        }
        
        @Override
		public void figureAttributeChanged(FigureEvent e) {
        }
        
        @Override
		public void figureAreaInvalidated(FigureEvent e) {
            if (! owner.isChanging()) {
                owner.fireAreaInvalidated(e.getInvalidatedArea());
            }
        }
        @Override
		public void undoableEditHappened(UndoableEditEvent e) {
            owner.fireUndoableEditHappened(e.getEdit());
        }
    };
    /** Creates a new instance. */
    public LabeledLineConnectionFigure() {
    }
    // DRAWING
    /**
     * Draw the figure. This method is delegated to the encapsulated presentation figure.
     */
    @Override
	public void drawFigure(Graphics2D g) {
        super.drawFigure(g);
        for (Figure child : children) {
            if (child.isVisible()) {
                child.draw(g);
            }
        }
    }
    
    // SHAPE AND BOUNDS
    /**
     * Transforms the figure.
     */
    @Override
	public void basicTransform(AffineTransform tx) {
        super.basicTransform(tx);
        for (Figure f : children) {
            f.basicTransform(tx);
        }
        invalidateBounds();
    }
    @Override
	public void basicSetBounds(Point2D.Double anchor, Point2D.Double lead) {
        super.basicSetBounds(anchor, lead);
        invalidate();
    }
    @Override
	public Rectangle2D.Double getBounds() {
        return super.getBounds();
        /*
        if (bounds == null) {
            bounds = super.getBounds();
            for (Figure child : getChildrenFrontToBack()) {
                if (child.isVisible()) {
                    bounds.add(child.getBounds());
                }
            }
        }
        return (Rectangle2D.Double) bounds.clone();
         */
    }
    @Override
	public Rectangle2D.Double getFigureDrawBounds() {
        if (drawBounds == null) {
            drawBounds = super.getFigureDrawBounds();
            for (Figure child : getChildrenFrontToBack()) {
                if (child.isVisible()) {
                    Rectangle2D.Double childBounds = child.getDrawBounds();
                    if (! childBounds.isEmpty()) {
                        drawBounds.add(childBounds);
                    }
                }
            }
        }
        return (Rectangle2D.Double) drawBounds.clone();
    }
    @Override
	public boolean contains(Point2D.Double p) {
        if (getDrawBounds().contains(p)) {
            for (Figure child : getChildrenFrontToBack()) {
                if (child.isVisible() && child.contains(p)) return true;
            }
            return super.contains(p);
        }
        return false;
    }
    protected void invalidateBounds() {
        //bounds = null;
        drawBounds = null;
    }
    // ATTRIBUTES
    /**
     * Sets an attribute of the figure.
     * AttributeKey name and semantics are defined by the class implementing
     * the figure interface.
     */
    @Override
	public void setAttribute(AttributeKey key, Object newValue) {
        willChange();
        super.setAttribute(key, newValue);
        if (isAttributeEnabled(key)) {
            if (children != null) {
                for (Figure child : children) {
                    child.setAttribute(key, newValue);
                }
            }
        }
        changed();
    }
    // EDITING
    @Override
	public Figure findFigureInside(Point2D.Double p) {
        if (getDrawBounds().contains(p)) {
            Figure found = null;
            for (Figure child : getChildrenFrontToBack()) {
                if (child.isVisible()) {
                    found = child.findFigureInside(p);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }
    // CONNECTING
    @Override
	public void updateConnection() {
        super.updateConnection();
        layout();
    }
    
    // COMPOSITE FIGURES
    @Override
	public java.util.List<Figure> getChildren() {
        return Collections.unmodifiableList(children);
    }
    @Override
	public int getChildCount() {
        return children.size();
    }
    @Override
	public Figure getChild(int index) {
        return children.get(index);
    }
    public void set(int index, Figure child) {
        children.set(index, child);
    }
    /**
     * Returns an iterator to iterate in
     * Z-order front to back over the children.
     */
    public java.util.List<Figure> getChildrenFrontToBack() {
        return children ==  null ?
            new LinkedList<Figure>() :
            new ReversedList<Figure>(children);
    }
    
    @Override
	public void add(Figure figure) {
        basicAdd(figure);
        if (getDrawing() != null) {
            figure.addNotify(getDrawing());
        }
    }
    @Override
	public void add(int index, Figure figure) {
        basicAdd(index, figure);
        if (getDrawing() != null) {
            figure.addNotify(getDrawing());
        }
    }
    @Override
	public void basicAdd(Figure figure) {
        basicAdd(children.size(), figure);
    }
    @Override
	public void basicAdd(int index, Figure figure) {
        children.add(index, figure);
        figure.addFigureListener(childHandler);
        figure.addUndoableEditListener(childHandler);
        invalidate();
    }
    @Override
	public boolean remove(final Figure figure) {
        int index = children.indexOf(figure);
        if (index == -1) {
            return false;
        } else {
            willChange();
            basicRemoveChild(index);
            if (getDrawing() != null) {
                figure.removeNotify(getDrawing());
            }
            changed();
            return true;
        }
    }
    @Override
	public Figure removeChild(int index) {
        willChange();
        Figure figure = basicRemoveChild(index);
        if (getDrawing() != null) {
            figure.removeNotify(getDrawing());
        }
        changed();
        return figure;
    }
    @Override
	public boolean basicRemove(final Figure figure) {
        int index = children.indexOf(figure);
        if (index == -1) {
            return false;
        } else {
            basicRemoveChild(index);
            return true;
        }
    }
    @Override
	public Figure basicRemoveChild(int index) {
        Figure figure = children.remove(index);
        figure.removeFigureListener(childHandler);
        figure.removeUndoableEditListener(childHandler);
        
        return figure;
    }
    
    @Override
	public void removeAllChildren() {
        willChange();
        while (children.size() > 0) {
            Figure figure = basicRemoveChild(children.size() - 1);
            if (getDrawing() != null) {
                figure.removeNotify(getDrawing());
            }
        }
        changed();
    }
    @Override
	public void basicRemoveAllChildren() {
        while (children.size() > 0) {
            basicRemoveChild(children.size() - 1);
        }
    }
    // LAYOUT
    /**
     * Get a Layouter object which encapsulated a layout
     * algorithm for this figure. Typically, a Layouter
     * accesses the child components of this figure and arranges
     * their graphical presentation.
     *
     *
     * @return layout strategy used by this figure
     */
    @Override
	public Layouter getLayouter() {
        return layouter;
    }
    @Override
	public void setLayouter(Layouter newLayouter) {
        this.layouter = newLayouter;
    }
    
    /**
     * A layout algorithm is used to define how the child components
     * should be laid out in relation to each other. The task for
     * layouting the child components for presentation is delegated
     * to a Layouter which can be plugged in at runtime.
     */
    @Override
	public void layout() {
        if (getLayouter() != null) {
            Rectangle2D.Double bounds = getBounds();
            Point2D.Double p = new Point2D.Double(bounds.x, bounds.y);
            Rectangle2D.Double r = getLayouter().layout(
                    this, p, p
                    );
            invalidateBounds();
        }
    }
    
// EVENT HANDLING
    
    @Override
	public void invalidate() {
        super.invalidate();
        invalidateBounds();
    }
    @Override
	public void validate() {
        super.validate();
        layout();
    }
    @Override
	public void addNotify(Drawing drawing) {
        for (Figure child : new LinkedList<Figure>(children)) {
            child.addNotify(drawing);
        }
        super.addNotify(drawing);
    }
    @Override
	public void removeNotify(Drawing drawing) {
        for (Figure child : new LinkedList<Figure>(children)) {
            child.removeNotify(drawing);
        }
        super.removeNotify(drawing);
    }
    /**
     * Informs that a figure changed the area of its display box.
     * /
     * public void changed() {
     * if (isChangingCount == 1) {
     * super.changed();
     * layout();
     * fireFigureChanged(getDrawBounds());
     * } else {
     * invalidateBounds();
     * }
     * isChangingCount--;
     * }*/
    @Override
	public LabeledLineConnectionFigure clone() {
        LabeledLineConnectionFigure that = (LabeledLineConnectionFigure) super.clone();
        that.childHandler = new ChildHandler(that);
        that.children = new ArrayList<Figure>();
        for (Figure thisChild : this.children) {
            Figure thatChild = (Figure) thisChild.clone();
            that.children.add(thatChild);
            thatChild.addFigureListener(that.childHandler);
            thatChild.addUndoableEditListener(that.childHandler);
        }
        return that;
    }
    @Override
	public void remap(HashMap<Figure,Figure> oldToNew) {
        super.remap(oldToNew);
        for (Figure child : children) {
            child.remap(oldToNew);
        }
    }
    /**
     * Informs that a figure changed the area of its display box.
     * /
     * public void changed() {
     * // FIXME - May break super implementation
     * if (isChangingCount == 1) {
     * layout();
     * fireFigureChanged(getDrawBounds());
     * } else {
     * invalidateBounds();
     * }
     * isChangingCount--;
     * }*/
    
}
