package com.conref.refactoring.lockCallSites.RefactorWizard;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.lockCallSites.Refactor.lockCallSitesRefactoring;

public class lockCallSitesRefactoringWizard extends RefactoringWizard {

	public lockCallSitesRefactoringWizard(lockCallSitesRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE );
		setDefaultPageTitle("lock CallSites");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
		settingPage sp = new settingPage("lockCallSite setting");
		addPage(sp);
	}

}
