package com.conref.refactoring.makeClassThreadSafe.refactorWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.makeClassThreadSafe.refactor.makeClassThreadSafeRefactoring;

public class makeClassThreadSafeRefactoringWizard extends RefactoringWizard {

	public makeClassThreadSafeRefactoringWizard(makeClassThreadSafeRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("make Class ThreadSafe");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
