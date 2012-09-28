package com.conref.refactoring.makeFieldAtomic.actions;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.makeFieldAtomic.refactor.makeAtomicRefactoring;
import com.conref.refactoring.makeFieldAtomic.refactoringWizard.makeAtomicRefactoringWizard;
import com.conref.util.Selection;
import com.conref.util.SelectionAnalyzer;



public class makeFieldAtomic implements IEditorActionDelegate,
		IWorkbenchWindowActionDelegate {
	private final class SimpleNameVisitor extends ASTVisitor{
				String fieldname;
			public SimpleNameVisitor(String fieldname) {
			  this.fieldname=fieldname;
			}
		public boolean visit(final FieldDeclaration fielddeclaration){
			fielddeclaration.accept(new ASTVisitor(){
			public boolean visit(SimpleName simplename){
				if(simplename.getIdentifier().equals(fieldname)){}
				rewriter.remove(fielddeclaration, null);
				return true;
			}
			});
			
			return true;
		}	
			
		public boolean visit(final MethodDeclaration method) {
			method.accept(new ASTVisitor(){
				public boolean visit(SimpleName simplename){
					if(simplename.getIdentifier().equals(fieldname)){
						replaceWithAtomicInteger(simplename);
						return true;
						
					}
					
					return true;
				}

				@SuppressWarnings("unchecked")
				private void replaceWithAtomicInteger(ASTNode refactoringNode) {
					AST ast = unit.getAST();
					MethodInvocation methodInvocation = ast.newMethodInvocation();
					methodInvocation.setExpression(ast.newSimpleName(fieldname));
					methodInvocation.setName(ast.newSimpleName("getAndIncrement"));
					ExpressionStatement statement = ast.newExpressionStatement(methodInvocation);
					ASTNode parent=refactoringNode.getParent().getParent().getParent();
					ListRewrite lr = rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
					lr.insertLast(statement, null);
					List<Modifier> mod = method.modifiers();
					Iterator<Modifier> it=mod.iterator();
					while(it.hasNext()){
						Modifier m=it.next();
						if(m.toString().equals("synchronized")){
							rewriter.remove(m,null);
						}
					}
					rewriter.remove(refactoringNode.getParent().getParent(), null);
					
				
				}
			});
			
			return true;
			
		}
	
	}
	private Selection selection;
	private ICompilationUnit select;
	private static IFile _file;
	private ITextEditor _editor;
	private CompilationUnit unit;
	private String selectedFieldName;
	ASTNode selectedNode;
	ASTRewrite rewriter; 
	Document document ;
	private boolean condition=false;
	Set<ASTNode> NotRefactoringNode=new HashSet<ASTNode>();
	@Override
	public void run(IAction action) {
		if (_editor == null) {
			return;
		}

//		String id = action.getId();
//		if (id.equals("ConRef.makeFieldAtomic")) {
		IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        Shell shell = window.getShell();
			if(!checkConditionWithIdentical(selectedFieldName)){
				MessageDialog.openInformation(
						shell,
						"makeFieldAtomic",
						"Refactoring can not be done!!!\n\n"+NotRefactoringNode.toString());
				NotRefactoringNode.clear();
				return ;
			}
			try {
				Change change=makeAtomic();
				
		        makeAtomicRefactoring refactor= new makeAtomicRefactoring(change);
				makeAtomicRefactoringWizard wizard = new makeAtomicRefactoringWizard(refactor);
				RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
				op.run(shell, "make field atomic");
			} catch (JavaModelException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
//		}


	private boolean checkConditionWithIdentical(final String selectedFieldName) {
		unit.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration method){
				method.accept(new ASTVisitor(){
					public boolean visit(SimpleName simplename){
						if(simplename.toString().equals(selectedFieldName)){
							condition=simplename.getParent()instanceof PostfixExpression||selectedNode.getParent() instanceof PrefixExpression;
							if (!condition) {
								ASTNode node = simplename;
								while (!(node instanceof Block)) {
									node = node.getParent();
								}
								NotRefactoringNode.add(node);
							}
							
							
						}
						
						return true;
					}
				});
				return true;
			}
		});
//		if(selectedNode.getParent() instanceof PostfixExpression||selectedNode.getParent() instanceof PrefixExpression){
//			return false;
//		}
		
		return condition;
	}


	private Change makeAtomic() throws JavaModelException {
//		ICompilationUnit cu = (ICompilationUnit) JavaCore.createCompilationUnitFrom(_file);
		
		
		 rewriter= ASTRewrite.create(unit.getAST());
		 String source=select.getSource();
		  document = new Document(source);
		
			unit.accept(new SimpleNameVisitor(selectedFieldName));			
		
		replaceFieldDeclaration();
		//change the source code immediately
		
//		try {
//			TextEdit edits = rewriter.rewriteAST(document, select.getJavaProject()
//					.getOptions(true));
//			edits.apply(document);
//		} catch (MalformedTreeException e) {
//			e.printStackTrace();
//		} catch (BadLocationException e) {
//			e.printStackTrace();
//		}
//		String newSource = document.get();
//
//		// update of the compilation unit
//		try {
//			select.getBuffer().setContents(newSource);
//		} catch (JavaModelException e) {
//			e.printStackTrace();
//		}
		
		//put the change into previe dialog
		Document document = new Document(source);

		TextEdit edits = rewriter.rewriteAST(document, select.getJavaProject()
				.getOptions(true));
		TextFileChange change = new TextFileChange("", (IFile) select.getResource());
		change.setEdit(edits);
		// edits.apply(document);
		// String newSource = document.get();
		//
		// // update of the compilation unit
		// cu.getBuffer().setContents(newSource);
		return change;

	}


	private void replaceFieldDeclaration() {
		AST ast=unit.getAST();
		//create pakage import
		ImportDeclaration importDeclaration = ast.newImportDeclaration();
        importDeclaration.setName(ast.newName("java.util.concurrent.atomic.AtomicInteger"));
//        unit.imports().add(importDeclaration);
//        ImportDeclaration id=  (ImportDeclaration) unit.imports().iterator().next();
//        ChildListPropertyDescriptor property = (ChildListPropertyDescriptor) id.getLocationInParent();
        ListRewrite lrw1 = rewriter.getListRewrite(unit,CompilationUnit.IMPORTS_PROPERTY);
        lrw1.insertLast(importDeclaration, null);
        
		//create new field declaration
		SimpleName fieldname = ast.newSimpleName(selectedFieldName);

		ClassInstanceCreation classInstanceCreation = ast
				.newClassInstanceCreation();
		classInstanceCreation.setType(ast.newSimpleType(ast
				.newName("AtomicInteger")));
		VariableDeclarationFragment fragment = ast
				.newVariableDeclarationFragment();
		fragment.setName(fieldname);
		fragment.setInitializer(classInstanceCreation);
		FieldDeclaration fd = ast.newFieldDeclaration(fragment);
		fd.setType(ast.newSimpleType(ast.newSimpleName("AtomicInteger")));
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
		 if(selectedNode==null){return;}
		 int selectedNodeType = selectedNode.getNodeType();
			if (selectedNodeType == ASTNode.SIMPLE_NAME) {
				selectedFieldName=((SimpleName)selectedNode).getIdentifier();
			}
			if(selectedNodeType==ASTNode.VARIABLE_DECLARATION_FRAGMENT){
				selectedFieldName=((VariableDeclarationFragment)selectedNode).getName().getIdentifier();
			}
		} else
			// _file =
			// (IFile)SelectionResolver.getSelectedResource(selection,IResource.FILE);
			select = null;
		action.setEnabled(true);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

		_editor = (ITextEditor) targetEditor;

		_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();
	}


}

