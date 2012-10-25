package com.conref.refactoring.splitlock.core;


import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

public class FrameworkExpandListener implements Listener {
	String checkedItem;
public FrameworkExpandListener(String checkedItem){
	this.checkedItem=checkedItem;
}
	@Override
	public void handleEvent(Event event) {
		if (checkedItem==null) {  
			return;
}  
	  
	    //µ±Ç°µã»÷item  
	    TreeItem item = (TreeItem) event.item;  
	    TreeItem[] items = item.getItems();  
	  
	    for (TreeItem treeItem : items) {  
	        if(treeItem.getData().toString().equals(checkedItem)){
	            treeItem.setChecked(true);  
	        }
	    }  
	}

}
