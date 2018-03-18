/*
 * @(#)QuadTreeDrawing.java  2.0  2006-01-14
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

import org.jhotdraw.geom.QuadTree2DDouble;
import org.jhotdraw.util.ReversedList;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.event.*;
import java.util.*;
/**
 * QuadTreeDrawing uses a QuadTree2DDouble to improve responsiveness of drawings which
 * contain many figures.
 *
 * @author Werner Randelshofer
 * @version 2.0 2006-01-14 Changed to support double precision coordinates.
 * <br>1.0 2003-12-01 Derived from JHotDraw 5.4b1.
 */
public class QuadTreeDrawing extends AbstractDrawing
implements FigureListener, UndoableEditListener {
    private ArrayList<Figure> figures = new ArrayList<Figure>();
    private QuadTree2DDouble<Figure> quadTree = new QuadTree2DDouble<Figure>();
    private boolean needsSorting = false;
    
    /** Creates a new instance. */
    public QuadTreeDrawing() {
    }
    
    @Override
	protected int indexOf(Figure figure) {
        return figures.indexOf(figure);
    }
    
    @Override
	public void basicAdd(int index, Figure figure) {
        figures.add(index, figure);
        quadTree.add(figure, figure.getDrawBounds());
        figure.addFigureListener(this);
        figure.addUndoableEditListener(this);
        needsSorting = true;
    }
    @Override
	public void basicRemove(Figure figure) {
        figures.remove(figure);
        quadTree.remove(figure);
        figure.removeFigureListener(this);
        figure.removeUndoableEditListener(this);
        needsSorting = true;
    }
    
    @Override
	public void draw(Graphics2D g) {
        Collection<Figure> c = quadTree.findIntersects(g.getClipBounds().getBounds2D());
        Collection<Figure> toDraw = sort(c);
        draw(g, toDraw);
    }
    
    /**
     * Implementation note: Sorting can not be done for orphaned figures.
     */
    @Override
	public Collection<Figure> sort(Collection<Figure> c) {
        ensureSorted();
        ArrayList<Figure> sorted = new ArrayList<Figure>(c.size());
        for (Figure f : figures) {
            if (c.contains(f)) {
                sorted.add(f);
            }
        }
        return sorted;
    }
    
    public void draw(Graphics2D g, Collection<Figure> c) {
        for (Figure f : c) {
            f.draw(g);
        }
    }
    
    
    @Override
	public void figureAreaInvalidated(FigureEvent e) {
        fireAreaInvalidated(e.getInvalidatedArea());
    }
    @Override
	public void figureChanged(FigureEvent e) {
        quadTree.remove(e.getFigure());
        quadTree.add(e.getFigure(), e.getFigure().getDrawBounds());
        needsSorting = true;
        fireAreaInvalidated(e.getInvalidatedArea());
    }
    
    @Override
	public void figureAdded(FigureEvent e) {
    }
    @Override
	public void figureRemoved(FigureEvent e) {
    }
    @Override
	public void figureRequestRemove(FigureEvent e) {
        remove(e.getFigure());
    }
    
    public Collection<Figure> getFigures(Rectangle2D.Double bounds) {
        return quadTree.findInside(bounds);
    }
    
    @Override
	public Collection<Figure> getFigures() {
        return Collections.unmodifiableCollection(figures);
    }
    
    @Override
	public Figure findFigureInside(Point2D.Double p) {
        Collection<Figure> c = quadTree.findContains(p);
        for (Figure f : getFiguresFrontToBack()) {
            if (c.contains(f) && f.contains(p)){
                return f.findFigureInside(p);
            }
        }
        return null;
        
    }
    
    /**
     * Returns an iterator to iterate in
     * Z-order front to back over the figures.
     */
    @Override
	public java.util.List<Figure> getFiguresFrontToBack() {
        ensureSorted();
        return new ReversedList<Figure>(figures);
    }
    
    @Override
	public Figure findFigure(Point2D.Double p) {
        Collection<Figure> c = quadTree.findContains(p);
        switch (c.size()) {
            case 0 :
                return null;
            case 1: {
                Figure f = c.iterator().next();
                return (f.contains(p)) ? f : null;
            }
            default : {
                for (Figure f : getFiguresFrontToBack()) {
                    if (c.contains(f) && f.contains(p)) return f;
                }
                return null;
            }
        }
    }
    @Override
	public Figure findFigureExcept(Point2D.Double p, Figure ignore) {
        Collection<Figure> c = quadTree.findContains(p);
        switch (c.size()) {
            case 0 : {
                return null;
            }
            case 1: {
                Figure f = c.iterator().next();
                return (f == ignore || ! f.contains(p)) ? null : f;
            }
            default : {
                for (Figure f : getFiguresFrontToBack()) {
                    if (f != ignore && f.contains(p)) return f;
                }
                return null;
            }
        }
    }
    @Override
	public Figure findFigureExcept(Point2D.Double p, Collection ignore) {
        Collection<Figure> c = quadTree.findContains(p);
        switch (c.size()) {
            case 0 : {
                return null;
            }
            case 1: {
                Figure f = c.iterator().next();
                return (! ignore.contains(f) || ! f.contains(p)) ? null : f;
            }
            default : {
                for (Figure f : getFiguresFrontToBack()) {
                    if (! ignore.contains(f) && f.contains(p)) return f;
                }
                return null;
            }
        }
    }
    
    @Override
	public Collection<Figure> findFigures(Rectangle2D.Double r) {
        Collection<Figure> c = quadTree.findIntersects(r);
        switch (c.size()) {
            case 0 :
                // fall through
            case 1:
                return c;
            default :
                return sort(c);
        }
    }
    @Override
	public Collection<Figure> findFiguresWithin(Rectangle2D.Double r) {
        Collection<Figure> c = findFigures(r);
        ArrayList<Figure> result = new ArrayList<Figure>(c.size());
        for (Figure f : c) {
            if (r.contains(f.getBounds())) {
                result.add(f);
            }
        }
        return result;
    }
    
    @Override
	public void bringToFront(Figure figure) {
        if (figures.remove(figure)) {
            figures.add(figure);
            needsSorting = true;
            fireAreaInvalidated(figure.getDrawBounds());
        }
    }
    @Override
	public void sendToBack(Figure figure) {
        if (figures.remove(figure)) {
            figures.add(0, figure);
            needsSorting = true;
            fireAreaInvalidated(figure.getDrawBounds());
        }
    }
    
    /**
     * We propagate all edit events from our figures to
     * undoable edit listeners, which have registered with us.
     */
    @Override
	public void undoableEditHappened(UndoableEditEvent e) {
        fireUndoableEditHappened(e.getEdit());
    }
    
    @Override
	public void figureAttributeChanged(FigureEvent e) {
    }
    
    @Override
	public boolean contains(Figure f) {
        return figures.contains(f);
    }
    
    /**
     * Ensures that the figures are sorted in z-order sequence.
     */
    private void ensureSorted() {
        if (needsSorting) {
            Collections.sort(figures, FigureLayerComparator.INSTANCE);
            needsSorting = false;
        }
    }
}
