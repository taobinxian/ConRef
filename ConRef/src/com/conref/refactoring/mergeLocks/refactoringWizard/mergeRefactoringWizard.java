package com.conref.refactoring.mergeLocks.refactoringWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.splitlock.refactor.splitRefactoring;

public class mergeRefactoringWizard extends RefactoringWizard {

	public mergeRefactoringWizard(splitRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle("Merge Locks ");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
