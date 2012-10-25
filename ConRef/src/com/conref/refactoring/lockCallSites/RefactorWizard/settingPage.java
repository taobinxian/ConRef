package com.conref.refactoring.lockCallSites.RefactorWizard;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.conref.refactoring.lockCallSites.Refactor.lockCallSitesRefactoring;

public class settingPage extends UserInputWizardPage{

	private Button btnCheck;
	private lockCallSitesRefactoring refactoring;

	protected settingPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite parent) {
		refactoring = (lockCallSitesRefactoring) getRefactoring();
		 Composite composite = new Composite(parent, SWT.NONE); 
		 GridLayout lay = new GridLayout(); 
		 lay.numColumns = 2; 
		 composite.setLayout(lay); 
		 btnCheck = new Button(composite, SWT.CHECK); 
		 btnCheck.setText("refactor other class or not ? "); 
		 // add listener 
		 defineListener(); 
		 // 将 composite 纳入框架的控制
		 setControl(composite); 
		 Dialog.applyDialogFont(composite);		
	}
//		RenameRefactoring
	private void defineListener() {
		btnCheck.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				refactoring.setOtherClassNeedRefactoring(true);
			}
		});
	}

}
