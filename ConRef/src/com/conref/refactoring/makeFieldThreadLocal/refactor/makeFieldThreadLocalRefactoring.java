package com.conref.refactoring.makeFieldThreadLocal.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class makeFieldThreadLocalRefactoring extends Refactoring {

	private Change _change;
	private final String name="makeFieldThreadLocal";

	public makeFieldThreadLocalRefactoring(Change change) {
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
