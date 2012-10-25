package com.conref.refactoring.makeFieldAtomic.refactoringWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.makeFieldAtomic.refactor.makeAtomicRefactoring;

public class makeAtomicRefactoringWizard extends RefactoringWizard {

	public makeAtomicRefactoringWizard(makeAtomicRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("make the field atomic");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
