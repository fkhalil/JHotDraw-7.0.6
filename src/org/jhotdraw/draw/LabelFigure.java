/*
 * @(#)LabelFigure.java  2.0  2006-01-14
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

import java.awt.geom.*;
import java.util.*;
/**
 * LabelFigure.
 *
 * @author  Werner Randelshofer
 * @version 2.0 2006-01-14 Changed to support double precison coordinates.
 * <br>1.0 8. March 2004  Created.
 */
public class LabelFigure extends TextFigure implements FigureListener {
    private TextHolder target;
    
    /** Creates a new instance. */
    public LabelFigure() {
        this("Label");
    }
    public LabelFigure(String text) {
        setText(text);
        setEditable(false);
    }
    
    public void setLabelFor(TextHolder target) {
        if (this.target != null) {
            this.target.removeFigureListener(this);
        }
        this.target = target;
        if (this.target != null) {
            this.target.addFigureListener(this);
        }
    }
    @Override
	public TextHolder getLabelFor() {
        return (target == null) ? this : target;
    }
    
    /**
     * Returns a specialized tool for the given coordinate.
     * <p>Returns null, if no specialized tool is available.
     */
    @Override
	public Tool getTool(Point2D.Double p) {
        return (target != null && contains(p)) ? new TextTool(target) : null;
    }
    
    
    @Override
	public void figureAreaInvalidated(FigureEvent e) {
    }
    
    @Override
	public void figureAttributeChanged(FigureEvent e) {
    }
    
    @Override
	public void figureAdded(FigureEvent e) {
    }
    
    @Override
	public void figureChanged(FigureEvent e) {
    }
    
    @Override
	public void figureRemoved(FigureEvent e) {
        if (e.getFigure() == target) {
            target.removeFigureListener(this);
            target = null;
        }
    }
    
    @Override
	public void figureRequestRemove(FigureEvent e) {
    }
    
    @Override
	public void remap(HashMap oldToNew) {
        super.remap(oldToNew);
        if (target != null) {
            Figure newTarget = (Figure) oldToNew.get(target);
            if (newTarget != null) {
                target.removeFigureListener(this);
                target = (TextHolder) newTarget;
                newTarget.addFigureListener(this);
            }
        }
    }
}
