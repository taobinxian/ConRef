<<<<<<< HEAD
package com.conref.refactoring.convertSyncFieldToLock.refactoringWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.convertSyncFieldToLock.refactoring.convertSyncFieldToLockRefactoring;

public class convertSyncFieldToLockWizard extends RefactoringWizard {

	public convertSyncFieldToLockWizard(convertSyncFieldToLockRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("convert SyncField To Lock");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
=======
package com.conref.refactoring.convertSyncFieldToLock.refactoringWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.convertSyncFieldToLock.refactoring.convertSyncFieldToLockRefactoring;
import com.conref.refactoring.makeFieldAtomic.refactor.makeAtomicRefactoring;

public class convertSyncFieldToLockWizard extends RefactoringWizard {

	public convertSyncFieldToLockWizard(convertSyncFieldToLockRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("convert SyncField To Lock");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
>>>>>>> 43afddf6fa6c20aaa2dd951f761dc9f4af511029
