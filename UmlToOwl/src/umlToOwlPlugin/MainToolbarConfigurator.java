package umlToOwlPlugin;

/**
 * $Id: MainToolbarConfigurator.java 115454 2011-11-11 11:46:25Z vainar $
 *
 * Copyright (c) 2002 NoMagic, Inc. All Rights Reserved.
 */

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsID;
import com.nomagic.magicdraw.actions.MDAction;

import java.util.Iterator;

/**
 * Class for configuring main toolbar. It adds action to File toolbar.
 *
 * @version $Date: 2011-11-11 05:46:25 -0600 (Pn, 11 Lap 2011) $ $Revision: 115454 $
 * @author Donatas Simkunas
 */
public class MainToolbarConfigurator implements AMConfigurator
{

	/**
	 * Action which will be added to main toolbar.
	 */
	private MDAction action;

	/**
	 * Creates configurator with given action.
	 * @param action action to be added to main toolbar.
	 */
	public MainToolbarConfigurator(MDAction action)
	{
		this.action = action;
	}

	/**
	 * @see com.nomagic.actions.AMConfigurator#configure(ActionsManager)
	 * Method adds action to File category.
	 */
	@Override
	public void configure(ActionsManager manager)
	{
		// searching for Help action category
		for (Iterator<ActionsCategory> iterator = manager.getCategories().iterator(); iterator.hasNext();)
		{
			ActionsCategory category = iterator.next();
			// adding action to found category.
			if (category.getID().equals(ActionsID.FILE))
			{
				category.addAction(action);
			}
		}
	}
	@Override
	public int getPriority()
	{
		return AMConfigurator.MEDIUM_PRIORITY;
	}

}
