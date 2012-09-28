package com.conref.refactoring.makeClassThreadSafe.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.makeClassThreadSafe.refactor.makeClassThreadSafeRefactoring;
import com.conref.refactoring.makeClassThreadSafe.refactorWizard.makeClassThreadSafeRefactoringWizard;
import com.conref.util.WorkbenchHelper;

public class makeClassThreadSafe implements IEditorActionDelegate {

	private ITextEditor _editor;
	private IFile _file;
	private ICompilationUnit select;
	private CompilationUnit unit;
	private ASTRewrite rewriter;

	@Override
	public void run(IAction action) {
		try {
			Shell shell = WorkbenchHelper.getActiveShell();
			Change change=createChange();
			makeClassThreadSafeRefactoring refactor= new makeClassThreadSafeRefactoring(change);
			makeClassThreadSafeRefactoringWizard wizard = new makeClassThreadSafeRefactoringWizard(refactor);
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
			op.run(shell, "make field atomic");
		
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Change createChange() throws JavaModelException {
		select = (ICompilationUnit) JavaCore.createCompilationUnitFrom(_file);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(select);
		 unit = (CompilationUnit) parser.createAST(null);
		 rewriter= ASTRewrite.create(unit.getAST());
		 unit.accept(new ASTVisitor(){
			@SuppressWarnings("unchecked")
			public boolean visit(MethodDeclaration method) {
				AST ast = method.getAST();
				ASTNode modifier = null;
				List<Modifier> modifiers=method.modifiers();
				List<String> md=new ArrayList<String>();
				for (Modifier md1 : modifiers) {
					md.add(md1.toString());
				}
				if (!md.contains("synchronized")){
				 modifier=ast.newModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
					ListRewrite lr = rewriter.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
					lr.insertAt(modifier, 1, null);
				}
				
				return true;
			} 
		 });
		 String	source = select.getSource();
		
			Document document = new Document(source);

			TextEdit edits = rewriter.rewriteAST(document, select.getJavaProject()
					.getOptions(true));
			TextFileChange change = new TextFileChange("", (IFile) select.getResource());
			change.setEdit(edits);
			return change;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_editor = (ITextEditor) targetEditor;

		_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();		
	}

}
