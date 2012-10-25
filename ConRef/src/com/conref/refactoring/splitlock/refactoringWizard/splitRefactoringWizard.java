package com.conref.refactoring.splitlock.refactoringWizard;

<<<<<<< HEAD
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.conref.refactoring.splitlock.refactor.splitRefactoring;
=======
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodInputPage;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard
;
>>>>>>> 43afddf6fa6c20aaa2dd951f761dc9f4af511029

import com.conref.refactoring.splitlock.refactor.splitRefactoring;

public class splitRefactoringWizard extends RefactoringWizard {

	public splitRefactoringWizard(splitRefactoring ref) {
//		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle("split lock automatically");
//		setDialogSettings(new DialogSettings("Workbench")); //$NON-NLS-1$

	}

	@Override
	protected void addUserInputPages() {
//		addPage(new splitlockInputPage());

	}

}
