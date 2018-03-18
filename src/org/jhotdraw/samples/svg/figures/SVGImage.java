/*
 * @(#)SVGImage.java  1.0  July 8, 2006
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

package org.jhotdraw.samples.svg.figures;

import java.awt.*;
import java.awt.geom.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.samples.svg.*;

/**
 * SVGImage.
 * <p>
 * FIXME - Implement me
 *
 * @author Werner Randelshofer
 * @version 1.0 July 8, 2006 Created.
 */
public class SVGImage extends AttributedFigure implements SVGFigure {
    private Rectangle2D.Double bounds = new Rectangle2D.Double();
    
    /** Creates a new instance. */
    public SVGImage() {
        SVGUtil.setDefaults(this);
    }

    @Override
	protected void drawFill(Graphics2D g) {
        g.fill(bounds);
    }

    @Override
	protected void drawStroke(Graphics2D g) {
        g.draw(bounds);
    }

    @Override
	public void basicTransform(AffineTransform ty) {
    }

    @Override
	public void basicSetBounds(Point2D.Double start, Point2D.Double end) {
    }

    @Override
	public Rectangle2D.Double getBounds() {
        return (Rectangle2D.Double) bounds.clone();
    }

    @Override
	public Object getRestoreData() {
        return getBounds();
    }

    @Override
	public void restoreTo(Object restoreData) {
        bounds = (Rectangle2D.Double) ((Rectangle2D.Double) restoreData).clone();
    }

    @Override
	public boolean contains(Point2D.Double p) {
        return bounds.contains(p);
    }
    
    @Override
	public SVGImage clone() {
        SVGImage that = (SVGImage) super.clone();
        that.bounds = (Rectangle2D.Double) bounds.clone();
        return that;
    }
    
    @Override
	public boolean isEmpty() {
        return true;
    }
}
