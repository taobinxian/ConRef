package com.conref.refactoring.convertRWLockToReentrantLock.RefactorWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.convertRWLockToReentrantLock.Refactor.convertRWLockToReentrantLockRefactoring;

public class convertRWLockToReentrantLockWizard extends RefactoringWizard {

	public convertRWLockToReentrantLockWizard(convertRWLockToReentrantLockRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("convert RWLock To ReentrantLock");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
