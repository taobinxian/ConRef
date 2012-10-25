package com.conref.refactoring.convertReentrantLockToReadWriteLock.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.convertReentrantLockToReadWriteLock.Refactor.convertReentrantLockToRWLockRefactoring;
import com.conref.refactoring.convertReentrantLockToReadWriteLock.RefactoringWizard.convertReentrantLockToRWLockWizard;
import com.conref.util.Selection;
import com.conref.util.SelectionAnalyzer;

public class convertReentrantLockToRWLock  implements IEditorActionDelegate {
	

	class unitVisitor extends ASTVisitor {
		public boolean visit(MethodDeclaration method) {
			if (method.getName().toString().startsWith("get")) {
				method.accept(new ASTVisitor() {
					public boolean visit(SimpleName varName) {
						if(varName.toString().equals(selectedLockName)){
							ASTNode node=varName;
							while(!(node instanceof Statement)){
								node=node.getParent();
							}
							AST ast=unit.getAST();
							SimpleName newlockname=ast.newSimpleName("readlock");	
							rewriter.replace(varName, newlockname, null);
						}
						return true;
					}
				});
			}
			if(method.getName().toString().startsWith("set")){
				method.accept(new ASTVisitor() {
					public boolean visit(SimpleName varName) {
						if(varName.toString().equals(selectedLockName)){
							ASTNode node=varName;
							while(!(node instanceof Statement)){
								node=node.getParent();
							}
							AST ast=unit.getAST();
							SimpleName newlockname=ast.newSimpleName("writelock");	
							rewriter.replace(varName, newlockname, null);						}
						return true;
					}
				});
				
			}
			
			return true;
		}

		public boolean visit(final FieldDeclaration field) {
			field.accept(new ASTVisitor() {
				public boolean visit(SimpleName name) {
					if (name.toString().equals(selectedLockName)) {
						replaceFieldDeclaration();
						rewriter.remove(field, null);
					}
					return true;
				}

				@SuppressWarnings("unchecked")
				private void replaceFieldDeclaration() {

					AST ast = unit.getAST();
					// create pakage import
					ImportDeclaration importDeclaration = ast
							.newImportDeclaration();
					importDeclaration.setName(ast
							.newName("java.util.concurrent.locks.ReentrantReadWriteLock"));
					ImportDeclaration readlockimportDeclaration = ast
							.newImportDeclaration();
					readlockimportDeclaration.setName(ast
							.newName("java.util.concurrent.locks.Lock"));
					ListRewrite lrw1 = rewriter.getListRewrite(unit,
							CompilationUnit.IMPORTS_PROPERTY);
					lrw1.insertLast(importDeclaration, null);
					lrw1.insertLast(readlockimportDeclaration, null);

					// create new field declaration
					SimpleName fieldname = ast
							.newSimpleName(selectedLockName);

					ClassInstanceCreation classInstanceCreation = ast
							.newClassInstanceCreation();
					classInstanceCreation.setType(ast.newSimpleType(ast
							.newName("ReentrantReadWriteLock")));
					VariableDeclarationFragment fragment = ast
							.newVariableDeclarationFragment();
					fragment.setName(fieldname);
					fragment.setInitializer(classInstanceCreation);
					FieldDeclaration fd = ast.newFieldDeclaration(fragment);
					fd.setType(ast.newSimpleType(ast
							.newSimpleName("ReentrantReadWriteLock")));
					fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
					ASTNode node = selectedNode;
					while (node.getNodeType() != ASTNode.TYPE_DECLARATION) {
						node = node.getParent();
					}
					TypeDeclaration cls = (TypeDeclaration) node;

					// Ôö¼ÓreadLock
					SimpleName readLock = ast.newSimpleName("readlock");
					MethodInvocation methodInvocation = ast
							.newMethodInvocation();
					methodInvocation.setExpression(ast.newSimpleName(selectedLockName));
					methodInvocation.setName(ast.newSimpleName("readLock"));

					VariableDeclarationFragment readlockfragment = ast
							.newVariableDeclarationFragment();
					readlockfragment.setName(readLock);
					readlockfragment.setInitializer(methodInvocation);
					FieldDeclaration readlockfd = ast
							.newFieldDeclaration(readlockfragment);
					readlockfd.setType(ast.newSimpleType(ast
							.newSimpleName("Lock")));
					readlockfd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
					// add write lock
					SimpleName writeLock = ast.newSimpleName("writelock");
					MethodInvocation writeLockmethodInvocation = ast
							.newMethodInvocation();
					writeLockmethodInvocation.setExpression(ast
							.newSimpleName(selectedLockName));
					writeLockmethodInvocation.setName(ast
							.newSimpleName("writeLock"));

					VariableDeclarationFragment writeLockfragment = ast
							.newVariableDeclarationFragment();
					writeLockfragment.setName(writeLock);
					writeLockfragment.setInitializer(writeLockmethodInvocation);
					FieldDeclaration writelockfd = ast
							.newFieldDeclaration(writeLockfragment);
					writelockfd.setType(ast.newSimpleType(ast
							.newSimpleName("Lock")));
					writelockfd
							.modifiers()
							.add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
					ListRewrite lrw = rewriter.getListRewrite(cls,
							TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
					lrw.insertFirst(fd, null);
					lrw.insertAt(readlockfd, 1, null);
					lrw.insertAt(writelockfd, 2, null);

				}

			});
			return true;
		}

	}

	private Selection selection;
	private ICompilationUnit select;
	private IFile _file;
	private ITextEditor _editor;
	private CompilationUnit unit;
	private String selectedLockName;
	ASTNode selectedNode;
	ASTRewrite rewriter;
	Document document;

	@Override
	public void run(IAction action) {
		if (selectedNode == null) {
			return;
		}
		int selectedNodeType = selectedNode.getNodeType();
		if (selectedNodeType == ASTNode.SIMPLE_NAME) {
			selectedLockName = ((SimpleName) selectedNode).getIdentifier();
		}
		if (selectedNodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			selectedLockName = ((VariableDeclarationFragment) selectedNode)
					.getName().getIdentifier();
		}
		Change change = null;
		try {
			change = createChange();
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}

		if (_editor == null) {
			return;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		Shell shell = window.getShell();

		convertReentrantLockToRWLockRefactoring refactor = new convertReentrantLockToRWLockRefactoring(
				change);
		convertReentrantLockToRWLockWizard wizard = new convertReentrantLockToRWLockWizard(refactor);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
				wizard);
		try {
			op.run(shell, "convert ReentrantLock To RWLock");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Change createChange() throws JavaModelException {
		rewriter = ASTRewrite.create(unit.getAST());
		String source;

		source = select.getSource();

		document = new Document(source);
		// changeFieldStatement();
		// changeMethodStatement();
		unit.accept(new unitVisitor());
		Document document = new Document(source);

		TextEdit edits = rewriter.rewriteAST(document, select.getJavaProject()
				.getOptions(true));
		TextFileChange change = new TextFileChange("",
				(IFile) select.getResource());
		change.setEdit(edits);

		return change;
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
			select = (ICompilationUnit) JavaCore
					.createCompilationUnitFrom(_file);

			int s = ((ITextSelection) selection).getOffset();
			int l = ((ITextSelection) selection).getLength();
			this.selection = Selection.createFromStartLength(s, l);
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(select);
			unit = (CompilationUnit) parser.createAST(null);
			SelectionAnalyzer sa = new SelectionAnalyzer(this.selection, true);
			unit.accept(sa);
			selectedNode = sa.getFirstSelectedNode();

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
