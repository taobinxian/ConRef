package com.conref.refactoring.makeFieldThreadLocal.refactorWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.makeFieldThreadLocal.refactor.makeFieldThreadLocalRefactoring;

public class makeFieldThreadLocalRefactoringWizard extends RefactoringWizard {

	public makeFieldThreadLocalRefactoringWizard(makeFieldThreadLocalRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("make Field Thread Local");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
