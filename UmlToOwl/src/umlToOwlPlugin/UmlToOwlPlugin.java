package umlToOwlPlugin;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.MDAction;
//import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;

public class UmlToOwlPlugin extends Plugin {
	public void init() {
		ActionsConfiguratorsManager manager = ActionsConfiguratorsManager.getInstance();
		MDAction action = new UmlToOwlAction("UmlToOwlAction", "UmlToOwl");
		manager.addMainToolbarConfigurator( new MainToolbarConfigurator( action ) );
		//Application.getInstance().getGUILog().showMessage("Labas Ryèka! Plugin for MagicDraw 18.2");
	}
	
	public boolean close() {
		//Application.getInstance().getGUILog().showMessage("Cya!");
		return true;
	}
	
	public boolean isSupported() {
		return true;
	}
}
