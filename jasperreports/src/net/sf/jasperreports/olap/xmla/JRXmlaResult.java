/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2009 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.olap.xmla;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.olap.result.JROlapCell;
import net.sf.jasperreports.olap.result.JROlapResult;
import net.sf.jasperreports.olap.result.JROlapResultAxis;


/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class JRXmlaResult implements JROlapResult
{

	private final static Log log = LogFactory.getLog(JRXmlaResult.class);
	
	private List axesList = new ArrayList();
	private JRXmlaResultAxis[] axes;
	private int[] cellOrdinalFactors;
	private final List cells = new ArrayList();

	public JROlapResultAxis[] getAxes()
	{
		return ensureAxisArray();
	}

	public JROlapCell getCell(int[] axisPositions)
	{
		int cellOrdinal = getCellOrdinal(axisPositions);
		JROlapCell cell;
		if (cellOrdinal < cells.size())
		{
			cell = (JROlapCell) cells.get(cellOrdinal);
		}
		else
		{
			// no cell with that ordinal
			cell = null;
		}
		return cell;
	}
	
	protected int getCellOrdinal(int[] axisPositions)
	{
		ensureCellOrdinalFactors();

		if (axisPositions.length != axes.length)
		{
			throw new JRRuntimeException("Number of axis positions (" + axisPositions.length 
					+ ") doesn't match the number of axes (" + axes.length + ")");
		}
		
		int ordinal = 0;
		for (int i = 0; i < axisPositions.length; ++i)
		{
			ordinal += cellOrdinalFactors[i] * axisPositions[i];
		}
		return ordinal;
	}

	public void addAxis(JRXmlaResultAxis axis)
	{
		axesList.add(axis);
		resetAxisArray();
	}

	public JRXmlaResultAxis getAxisByName(String name)
	{
		JRXmlaResultAxis axis = null;
		for (Iterator iter = axesList.iterator(); axis == null && iter.hasNext();)
		{
			JRXmlaResultAxis ax = (JRXmlaResultAxis) iter.next();
			if (ax.getAxisName().equals(name))
			{
				axis = ax;
			}
		}
		return axis;
	}

	protected JRXmlaResultAxis[] ensureAxisArray()
	{
		if (axes == null)
		{
			axes = new JRXmlaResultAxis[axesList.size()];
			axes = (JRXmlaResultAxis[]) axesList.toArray(axes);
		}
		return axes;
	}

	protected void ensureCellOrdinalFactors()
	{
		ensureAxisArray();
		
		if (cellOrdinalFactors == null)
		{
			int axisCount = axes.length;
			cellOrdinalFactors = new int[axisCount];
			
			cellOrdinalFactors[0] = 1;
			for (int i = 1; i < axisCount; ++i)
			{
				cellOrdinalFactors[i] = cellOrdinalFactors[i - 1] * axes[i - 1].getTupleCount();
			}
		}
	}

	protected void resetAxisArray()
	{
		axes = null;
		cellOrdinalFactors = null;
	}
	
	public void setCell(JRXmlaCell cell, int cellOrdinal)
	{
		int cellsCount = cells.size();
		if (cellOrdinal == -1 || cellOrdinal == cellsCount)
		{
			cells.add(cell);
		}
		else if (cellOrdinal > cellsCount)
		{
			for (int i = cellsCount; i < cellOrdinal; ++i)
			{
				cells.add(null);
			}
			cells.add(cell);
		}
		else
		{
			if (log.isWarnEnabled())
			{
				log.warn("Overwriting cell at ordinal " + cellOrdinal);
			}
			
			cells.set(cellOrdinal, cell);
		}
	}
}
