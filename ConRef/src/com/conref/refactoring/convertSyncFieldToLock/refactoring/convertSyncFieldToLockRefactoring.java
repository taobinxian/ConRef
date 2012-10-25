<<<<<<< HEAD
package com.conref.refactoring.convertSyncFieldToLock.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class convertSyncFieldToLockRefactoring extends Refactoring {

	private Change _change;
	private final String name="convertSyncFieldToLock";

	public convertSyncFieldToLockRefactoring(Change change) {
		this._change=change;
	}

	@Override
	public String getName() {
		
		return name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// 
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// 
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		return _change;
	}

}
=======
package com.conref.refactoring.convertSyncFieldToLock.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class convertSyncFieldToLockRefactoring extends Refactoring {

	private Change _change;
	private final String name="convertSyncFieldToLock";

	public convertSyncFieldToLockRefactoring(Change change) {
		this._change=change;
	}

	@Override
	public String getName() {
		
		return name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// 
		return RefactoringStatus.createInfoStatus("Initial Condition is OK!");
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// 
		return RefactoringStatus.createInfoStatus("Final condition is OK!");
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		return _change;
	}

}
>>>>>>> 43afddf6fa6c20aaa2dd951f761dc9f4af511029
