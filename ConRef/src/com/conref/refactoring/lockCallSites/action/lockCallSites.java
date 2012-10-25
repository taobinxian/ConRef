package com.conref.refactoring.lockCallSites.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.lockCallSites.Refactor.lockCallSitesRefactoring;
import com.conref.refactoring.lockCallSites.RefactorWizard.lockCallSitesRefactoringWizard;

public class lockCallSites implements IEditorActionDelegate{
	private ICompilationUnit select;
	private static IFile _file;
	private ITextEditor _editor;
	ASTNode selectedNode;
	ASTRewrite rewriter; 
	Document document ;
	private int selectionStart;
	private int selectionLength;
	@Override
	public void run(IAction action) {
		IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        Shell shell = window.getShell();
		if (selectionLength == 0) {
			MessageBox mb = new MessageBox(shell);
			mb.setMessage("please select the method needed to lock");
			mb.open();
			return;
		}
	        lockCallSitesRefactoring refactor= new lockCallSitesRefactoring(select,selectionStart,selectionLength);
	        lockCallSitesRefactoringWizard wizard = new lockCallSitesRefactoringWizard(refactor);
					RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
					try {
						op.run(shell, "lock CallSites");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			
	
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection.isEmpty())
			select = null;
		else if (selection instanceof IStructuredSelection) {
			IStructuredSelection strut = ((IStructuredSelection) selection);
			if (strut.size() != 1)
				select = null;
			if (strut.getFirstElement() instanceof IJavaElement)
				select = (ICompilationUnit) strut.getFirstElement();
		} else if (selection instanceof ITextSelection) {
			select = (ICompilationUnit) JavaCore.createCompilationUnitFrom(_file);
			
		 selectionStart=((ITextSelection)selection).getOffset();
		 selectionLength=((ITextSelection)selection).getLength();
		 
		} else
			select = null;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_editor = (ITextEditor) targetEditor;

		if(_editor!=null){
			_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();
	}	}

}
