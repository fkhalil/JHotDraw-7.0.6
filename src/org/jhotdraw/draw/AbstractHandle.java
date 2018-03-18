/*
 * @(#)AbstractHandle.java  1.0  14. November 2003
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
�
 */

package org.jhotdraw.draw;

import java.util.Collection;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.undo.*;
import java.util.*;
/**
 * AbstractHandle.
 *
 * @author Werner Randelshofer
 * @version 1.0 2003-12-01 Derived from JHotDraw 5.4b1.
 */
public abstract class AbstractHandle implements Handle, FigureListener {
    private Figure owner;
    protected DrawingView view;
    protected EventListenerList listenerList = new EventListenerList();
    
    /**
     * The bounds of the abstract handle.
     */
    private Rectangle bounds;
    
    /** Creates a new instance. */
    public AbstractHandle(Figure owner) {
        this.owner = owner;
        owner.addFigureListener(this);
    }
    
    /** FIXME - Get this form the drawing view. */
    protected int getHandlesize() {
        return 7;
    }
    
    /**
     * Adds a listener for this handle.
     */
    @Override
	public void addHandleListener(HandleListener l) {
        listenerList.add(HandleListener.class, l);
    }
    /**
     * Removes a listener for this handle.
     */
    @Override
	public void removeHandleListener(HandleListener l) {
        listenerList.remove(HandleListener.class, l);
    }
    @Override
	public Figure getOwner() {
        return owner;
        }
    @Override
	public void setView(DrawingView view) {
        this.view = view;
    }
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    protected void fireAreaInvalidated(Rectangle invalidatedArea) {
        HandleEvent event = null;
        // Notify all listeners that have registered interest for
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == HandleListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new HandleEvent(this, invalidatedArea);
                ((HandleListener)listeners[i+1]).areaInvalidated(event);
            }
        }
    }
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    protected void fireUndoableEditHappened(UndoableEdit edit) {
        view.getDrawing().fireUndoableEditHappened(edit);
        /*
        UndoableEditEvent event = null;
        // Notify all listeners that have registered interest for
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == UndoableEditListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new UndoableEditEvent(this, edit);
                ((UndoableEditListener)listeners[i+1]).undoableEditHappened(event);
            }
        }*/
    }
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    protected void fireHandleRequestRemove(Rectangle invalidatedArea) {
        HandleEvent event = null;
        // Notify all listeners that have registered interest for
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == HandleListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new HandleEvent(this, invalidatedArea);
                ((HandleListener)listeners[i+1]).handleRequestRemove(event);
            }
        }
    }
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    protected void fireHandleRequestSecondaryHandles() {
        HandleEvent event = null;
        // Notify all listeners that have registered interest for
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == HandleListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new HandleEvent(this, null);
                ((HandleListener)listeners[i+1]).handleRequestSecondaryHandles(event);
            }
        }
    }
    
    /**
     * Draws this handle.
     */
    @Override
	public void draw(Graphics2D g) {
        drawCircle(g, Color.white, Color.black);
    }
    protected void drawCircle(Graphics2D g, Color fill, Color stroke) {
        Rectangle r = getBounds();
        
        g.setColor(fill);
        g.fillOval(r.x, r.y, r.width, r.height);
        g.setStroke(new BasicStroke());
        g.setColor(stroke);
        g.drawOval(r.x, r.y, r.width, r.height);
    }
    protected void drawRectangle(Graphics2D g, Color fill, Color stroke) {
        Rectangle r = getBounds();
        
        g.setColor(fill);
        g.fill(r);
        g.setStroke(new BasicStroke());
        g.setColor(stroke);
        g.draw(r);
    }
    protected void drawDiamond(Graphics2D g, Color fill, Color stroke) {
        Rectangle r = getBounds();
        r.grow(1,1);
        
        Polygon p = new Polygon();
        p.addPoint(r.x + r.width / 2, r.y);
        p.addPoint(r.x + r.width, r.y + r.height / 2);
        p.addPoint(r.x + r.width / 2, r.y + r.height);
        p.addPoint(r.x, r.y + r.height / 2);
        p.addPoint(r.x + r.width / 2, r.y);
       
        g.setColor(fill);
        g.fill(p);
        g.setStroke(new BasicStroke());
        g.setColor(stroke);
        g.draw(p);
    }
    
    @Override
	public boolean contains(Point p) {
        return getBounds().contains(p);
    }
    
    @Override
	public void invalidate() {
        fireAreaInvalidated(getDrawBounds());
    }
    
    @Override
	public void dispose() {
        owner.removeFigureListener(this);
        owner = null;
    }
    
    /**
     * Sent when a region used by the figure needs to be repainted.
     * The implementation of this method assumes that the handle
     * is located on the bounds of the figure or inside the figure.
     * If the handle is located elsewhere this method must be reimpleted
     * by the subclass.
     */
    @Override
	public void figureAreaInvalidated(FigureEvent evt) {
        updateBounds();
    }
    
    /**
     * Sent when a figure was added.
     */
    @Override
	public void figureAdded(FigureEvent e) {
        // Empty
    }
    /**
     * Sent when a figure was removed.
     */
    @Override
	public void figureRemoved(FigureEvent e) {
        // Empty
    }
    /**
     * Sent when a figure requests to be removed.
     */
    @Override
	public void figureRequestRemove(FigureEvent e) {
        // Empty
    }
    /**
     * Sent when the bounds or shape of a figure has changed.
     */
    @Override
	public void figureChanged(FigureEvent evt) {
        updateBounds();
    }
    
    /**
     * Returns a cursor for the handle.
     */
    @Override
	public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
    
    /**
     * Returns true, if the given handle is an instance of the same
     * class or of a subclass of this handle,.
     */
    @Override
	public boolean isCombinableWith(Handle handle) {
        return getClass().isAssignableFrom(handle.getClass());
    }
    @Override
	public void keyTyped(KeyEvent evt) {
    }
    @Override
	public void keyReleased(KeyEvent evt) {
    }
    @Override
	public void keyPressed(KeyEvent evt) {
    }
    
    @Override
	public final Rectangle getBounds() {
        if (bounds == null) {
            bounds = basicGetBounds();
        }
        return (Rectangle) bounds.clone();
    }
    @Override
	public Rectangle getDrawBounds() {
        Rectangle r = getBounds();
        r.grow(2,2); // grow by two pixels to take antialiasing into account
        return r;
    }
    protected abstract Rectangle basicGetBounds();
    protected void updateBounds() {
        Rectangle newBounds = basicGetBounds();
        if (bounds == null || ! newBounds.equals(bounds)) {
            if (bounds != null) fireAreaInvalidated(getDrawBounds());
            bounds = newBounds;
            fireAreaInvalidated(getDrawBounds());
        }
    }
    /**
     * Tracks a double click.
     */
    @Override
	public void trackDoubleClick(Point p, int modifiersEx) {
    }
    
    @Override
	public void figureAttributeChanged(FigureEvent e) {
    }

    @Override
	public void viewTransformChanged() {
        bounds = null;
    }

    @Override
	public Collection<Handle> createSecondaryHandles() {
        return Collections.emptyList();
    }
}
