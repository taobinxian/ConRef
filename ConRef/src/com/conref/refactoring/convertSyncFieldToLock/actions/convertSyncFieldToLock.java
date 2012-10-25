package com.conref.refactoring.convertSyncFieldToLock.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
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

import com.conref.refactoring.convertSyncFieldToLock.refactoring.convertSyncFieldToLockRefactoring;
import com.conref.refactoring.convertSyncFieldToLock.refactoringWizard.convertSyncFieldToLockWizard;
import com.conref.util.Selection;
import com.conref.util.SelectionAnalyzer;

public class convertSyncFieldToLock implements IEditorActionDelegate{
	private Selection selection;
	private ICompilationUnit select;
	private static IFile _file;
	private ITextEditor _editor;
	private CompilationUnit unit;
	private String selectedSyncFieldName;
	ASTNode selectedNode;
	ASTRewrite rewriter; 
	Document document ;
	@Override
	public void run(IAction action) {
		if(selectedNode==null){return;}
		 int selectedNodeType = selectedNode.getNodeType();
			if (selectedNodeType == ASTNode.SIMPLE_NAME) {
				selectedSyncFieldName=((SimpleName)selectedNode).getIdentifier();
			}
			if(selectedNodeType==ASTNode.VARIABLE_DECLARATION_FRAGMENT){
				selectedSyncFieldName=((VariableDeclarationFragment)selectedNode).getName().getIdentifier();
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
					
	        convertSyncFieldToLockRefactoring refactor= new convertSyncFieldToLockRefactoring(change);
	        convertSyncFieldToLockWizard wizard = new convertSyncFieldToLockWizard(refactor);
					RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
					try {
						op.run(shell, "convert SyncField To Lock");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			
	}

	private Change createChange() throws JavaModelException {
		rewriter = ASTRewrite.create(unit.getAST());
		String source;
		
		
			source = select.getSource();

			document = new Document(source);
			changeFieldStatement();
			changeMethodStatement();
			Document document = new Document(source);

			TextEdit edits = rewriter.rewriteAST(document, select
					.getJavaProject().getOptions(true));
			TextFileChange change = new TextFileChange("",
					(IFile) select.getResource());
			change.setEdit(edits);
		
		return change;
	}
	@SuppressWarnings("unchecked")
	private void replaceFieldDeclaration() {
		AST ast=unit.getAST();
		//create pakage import
		ImportDeclaration importDeclaration = ast.newImportDeclaration();
        importDeclaration.setName(ast.newName("java.util.concurrent.locks.ReentrantLock"));
//        unit.imports().add(importDeclaration);
//        ImportDeclaration id=  (ImportDeclaration) unit.imports().iterator().next();
//        ChildListPropertyDescriptor property = (ChildListPropertyDescriptor) id.getLocationInParent();
        ListRewrite lrw1 = rewriter.getListRewrite(unit,CompilationUnit.IMPORTS_PROPERTY);
        lrw1.insertLast(importDeclaration, null);
        
		//create new field declaration
		SimpleName fieldname = ast.newSimpleName(selectedSyncFieldName);

		ClassInstanceCreation classInstanceCreation = ast
				.newClassInstanceCreation();
		classInstanceCreation.setType(ast.newSimpleType(ast
				.newName("ReentrantLock")));
		VariableDeclarationFragment fragment = ast
				.newVariableDeclarationFragment();
		fragment.setName(fieldname);
		fragment.setInitializer(classInstanceCreation);
		FieldDeclaration fd = ast.newFieldDeclaration(fragment);
		fd.setType(ast.newSimpleType(ast.newSimpleName("ReentrantLock")));
		fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
		ASTNode node=selectedNode;
		while(node.getNodeType()!=ASTNode.TYPE_DECLARATION){
			node=node.getParent();
		}
		TypeDeclaration cls=(TypeDeclaration)node;
		ListRewrite lrw = rewriter.getListRewrite(cls,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		lrw.insertFirst(fd, null);		
	}

	private void changeMethodStatement() {

		unit.accept(new ASTVisitor() {
			@SuppressWarnings("unchecked")
			public boolean visit(SynchronizedStatement syncstatement) {
				 Expression ex = syncstatement.getExpression();
				if(ex.toString().equals(selectedSyncFieldName)){
					Block syncblock=syncstatement.getBody();
					AST ast=unit.getAST();
					MethodInvocation lockmethodInvo = ast.newMethodInvocation();
					lockmethodInvo.setName(ast.newSimpleName("lock"));
					lockmethodInvo.setExpression(ast.newSimpleName(selectedSyncFieldName));
		            ExpressionStatement firststatement = ast.newExpressionStatement(lockmethodInvo);
		            MethodInvocation unlockmethodInvo = ast.newMethodInvocation();
					unlockmethodInvo.setName(ast.newSimpleName("unlock"));
					unlockmethodInvo.setExpression(ast.newSimpleName(selectedSyncFieldName));
		            ExpressionStatement laststatement = ast.newExpressionStatement(unlockmethodInvo);
//		            ListRewrite lr = rewriter.getListRewrite(syncblock, Block.STATEMENTS_PROPERTY);
//		            lr.insertFirst(firststatement, null);
//		            lr.insertLast(laststatement, null);
		            ASTNode stmts=ASTNode.copySubtree(ast, syncblock);
		            List<Statement> list=((Block)stmts).statements();
		            ListRewrite lr2 = rewriter.getListRewrite(syncstatement.getParent(),Block.STATEMENTS_PROPERTY);
		            int statementIndex= lr2.getOriginalList().indexOf(syncstatement);
		            lr2.insertAt(firststatement, ++statementIndex, null);
		              for(Statement stmt:list){
		            	  lr2.insertAt(stmt, ++statementIndex, null);
		              }
		              lr2.insertAt(laststatement, ++statementIndex, null);
		            lr2.remove(syncstatement, null);  
				}
				return true;
			}
		});

	}

	private void changeFieldStatement() {
		unit.accept(new ASTVisitor(){
			public boolean visit(final FieldDeclaration field){
				field.accept(new ASTVisitor() {
					public boolean visit(SimpleName name) {
						if(name.toString().equals(selectedSyncFieldName)){
							replaceFieldDeclaration();
							rewriter.remove(field, null);
						}
						return true;
					}

				});
				return true;
			}
			public boolean visit(SimpleName simplename){
				return true;
			}
			
		});
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
			
		int s=((ITextSelection)selection).getOffset();
		int l=((ITextSelection)selection).getLength();
		this.selection=Selection.createFromStartLength(s, l);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(select);
		 unit = (CompilationUnit) parser.createAST(null);
		SelectionAnalyzer sa = new SelectionAnalyzer(this.selection, true);
		unit.accept(sa);
		 selectedNode = sa.getFirstSelectedNode();
		 
		} else
			// _file =
			// (IFile)SelectionResolver.getSelectedResource(selection,IResource.FILE);
			select = null;
		action.setEnabled(true);
	
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

		_editor = (ITextEditor) targetEditor;

		if(_editor!=null){
			_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();
	}	}

}
