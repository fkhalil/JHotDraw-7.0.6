/*
 * @(#)AbstractDrawing.java  2.1  2006-07-08
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

import org.jhotdraw.beans.*;
import org.jhotdraw.undo.*;
import org.jhotdraw.xml.*;
import java.awt.geom.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.util.*;
import java.io.*;

/**
 * AbstractDrawing.
 *
 * @author Werner Randelshofer
 * @version 2.1 2006-07-08 Extend AbstractBean. 
 * <br>2.0.1 2006-02-06 Did ugly dirty fix for IndexOutOfBoundsException when
 * undoing removal of Figures. 
 * <br>2.0 2006-01-14 Changed to support double precision coordinates.
 * <br>1.0 2003-12-01 Derived from JHotDraw 5.4b1.
 */
public abstract class AbstractDrawing extends AbstractBean implements Drawing {
    private final static Object lock = new JPanel().getTreeLock();
    protected EventListenerList listenerList = new EventListenerList();
    private FontRenderContext fontRenderContext;
    
    /** Creates a new instance. */
    public AbstractDrawing() {
    }
    
    @Override
	public void addDrawingListener(DrawingListener listener) {
        listenerList.add(DrawingListener.class, listener);
    }
    @Override
	public void removeDrawingListener(DrawingListener listener) {
        listenerList.remove(DrawingListener.class, listener);
    }
    @Override
	public void addUndoableEditListener(UndoableEditListener l) {
        listenerList.add(UndoableEditListener.class, l);
    }
    
    @Override
	public void removeUndoableEditListener(UndoableEditListener l) {
        listenerList.remove(UndoableEditListener.class, l);
    }
    @Override
	public void addAll(Collection<Figure> figures) {
        CompositeEdit edit = new CompositeEdit("Figuren hinzuf\u00fcgen");
        fireUndoableEditHappened(edit);
        for (Figure f : figures) {
            add(f);
        }
        fireUndoableEditHappened(edit);
    }
    
    /***
     * Removes all figures.
     */
    @Override
	public void clear() {
        removeAll(getFigures());
    }
    
    /**
     * Gets the number of figures.
     */
    @Override
	public int getFigureCount() {
        return getFigures().size();
    }
    
    @Override
	public void removeAll(Collection<Figure> toBeRemoved) {
        CompositeEdit edit = new CompositeEdit("Figuren entfernen");
        fireUndoableEditHappened(edit);
        
        for (Figure f : new ArrayList<Figure>(toBeRemoved)) {
            remove(f);
        }
        
        fireUndoableEditHappened(edit);
    }
    @Override
	public void basicAddAll(Collection<Figure> figures) {
        for (Figure f : figures) {
            basicAdd(f);
        }
    }
    @Override
	public void basicRemoveAll(Collection<Figure> toBeOrphaned) {
        // Implementation note: We create a new collection to
        // avoid problems that may be caused, if the collection
        // is somehow connected to our figures connection.
        for (Figure f : new ArrayList<Figure>(toBeOrphaned)) {
            basicRemove(f);
        }
    }
    
    /**
     * Calls basicAdd and then calls figure.addNotify and firesFigureAdded.
     */
    @Override
	public final void add(final Figure figure) {
        final int index = getFigureCount();
        basicAdd(index, figure);
        figure.addNotify(this);
        fireFigureAdded(figure);
        fireUndoableEditHappened(new AbstractUndoableEdit() {
            @Override
			public String getPresentationName() { return "Figur einf\u00fcgen"; }
            @Override
			public void undo()  throws CannotUndoException {
                super.undo();
                basicRemove(figure);
                figure.removeNotify(AbstractDrawing.this);
                fireFigureRemoved(figure);
            }
            @Override
			public void redo()  throws CannotUndoException {
                super.redo();
                basicAdd(index, figure);
                figure.addNotify(AbstractDrawing.this);
                fireFigureAdded(figure);
            }
        });
    }
    
        @Override
		public void basicAdd(Figure figure) {
        basicAdd(getFigureCount(), figure);
    }

    /**
     * Calls basicRemove and then calls figure.addNotify and firesFigureAdded.
     */
    @Override
	public final void remove(final Figure figure) {
        if (contains(figure)) {
            final int index = indexOf(figure);
            basicRemove(figure);
            figure.removeNotify(this);
            fireFigureRemoved(figure);
            fireUndoableEditHappened(new AbstractUndoableEdit() {
                @Override
				public String getPresentationName() { return "Figur entfernen"; }
                @Override
				public void redo()  throws CannotUndoException {
                    super.redo();
                    basicRemove(figure);
                    figure.removeNotify(AbstractDrawing.this);
                    fireFigureRemoved(figure);
                }
                @Override
				public void undo()  throws CannotUndoException {
                    super.undo();
                    basicAdd(index, figure);
                    figure.addNotify(AbstractDrawing.this);
                    fireFigureAdded(figure);
                }
            });
        } else {
            fireAreaInvalidated(figure.getDrawBounds());
        }
    }
    protected abstract int indexOf(Figure figure);
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    protected void fireAreaInvalidated(Rectangle2D.Double dirtyRegion) {
        DrawingEvent event = null;
        // Notify all listeners that have registered interest for
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == DrawingListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new DrawingEvent(this, null, dirtyRegion);
                ((DrawingListener)listeners[i+1]).areaInvalidated(event);
            }
        }
    }
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    @Override
	public void fireUndoableEditHappened(UndoableEdit edit) {
        UndoableEditEvent event = null;
        if (listenerList.getListenerCount() > 0) {
            // Notify all listeners that have registered interest for
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (event == null)
                    event = new UndoableEditEvent(this, edit);
                if (listeners[i] == UndoableEditListener.class) {
                    ((UndoableEditListener)listeners[i+1]).undoableEditHappened(event);
                }
            }
        }
    }
    
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    protected void fireFigureAdded(Figure f) {
        DrawingEvent event = null;
        // Notify all listeners that have registered interest for
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == DrawingListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new DrawingEvent(this, f, f.getDrawBounds());
                ((DrawingListener)listeners[i+1]).figureAdded(event);
            }
        }
    }
    
    /**
     *  Notify all listenerList that have registered interest for
     * notification on this event type.
     */
    protected void fireFigureRemoved(Figure f) {
        DrawingEvent event = null;
        // Notify all listeners that have registered interest for
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == DrawingListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new DrawingEvent(this, f, f.getDrawBounds());
                ((DrawingListener)listeners[i+1]).figureRemoved(event);
            }
        }
    }
    

    
    @Override
	public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }
    
    @Override
	public void setFontRenderContext(FontRenderContext frc) {
        fontRenderContext = frc;
    }
    
    @Override
	public void read(DOMInput in) throws IOException {
        in.openElement("figures");
        for (int i=0; i < in.getElementCount(); i++) {
            Figure f;
            add(f = (Figure) in.readObject(i));
        }
        in.closeElement();
    }
    
    @Override
	public void write(DOMOutput out) throws IOException {
        out.openElement("figures");
        for (Figure f : getFigures()) {
            out.writeObject(f);
        }
        out.closeElement();
    }
    /**
     * The drawing view synchronizes on the lock when drawing a drawing.
     */
    @Override
	public Object getLock() {
        return lock;
    }
}
