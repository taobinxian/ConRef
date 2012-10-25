package com.conref.refactoring.convertReentrantLockToReadWriteLock.RefactoringWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.convertReentrantLockToReadWriteLock.Refactor.convertReentrantLockToRWLockRefactoring;

public class convertReentrantLockToRWLockWizard extends RefactoringWizard {

	public convertReentrantLockToRWLockWizard(convertReentrantLockToRWLockRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("convert ReentrantLock To RWLock");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
