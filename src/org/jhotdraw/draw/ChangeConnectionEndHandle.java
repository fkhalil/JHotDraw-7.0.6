/*
 * @(#)ChangeConnectionEndHandle.java  2.0  2006-01-14
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

import java.awt.geom.*;
/**
 * Handle to reconnect the
 * end of a connection to another figure.
 *
 * @author Werner Randelshofer
 * @version 2.0 2006-01-14 Changed to support double precision coordinates.
 * <br>1.0 2003-12-01 Derived from JHotDraw 5.4b1.
 */
public class ChangeConnectionEndHandle extends ChangeConnectionHandle {
    
    /**
     * Constructs the connection handle for the given start figure.
     */
    public ChangeConnectionEndHandle(Figure owner) {
        super(owner);
    }
    
    /**
     * Sets the start of the connection.
     */
    @Override
	protected void connect(Connector c) {
        getConnection().setEndConnector(c);
    }
    
    /**
     * Disconnects the start figure.
     */
    @Override
	protected void disconnect() {
        getConnection().setEndConnector(null);
    }
    
    
    @Override
	protected Connector getTarget() {
        return getConnection().getEndConnector();
    }
    
    /**
     * Sets the start point of the connection.
     */
    @Override
	protected void setLocation(Point2D.Double p) {
        // XXX - Fire UndoableEdit
        getConnection().willChange();
        getConnection().setEndPoint(p);
        getConnection().changed();
    }
    
    /**
     * Returns the start point of the connection.
     */
    @Override
	protected Point2D.Double getLocation() {
        return getConnection().getEndPoint();
    }
    
    @Override
	protected boolean canConnect(Figure existingEnd, Figure targetEnd) {
        return getConnection().canConnect(existingEnd, targetEnd);
    }
    
}
