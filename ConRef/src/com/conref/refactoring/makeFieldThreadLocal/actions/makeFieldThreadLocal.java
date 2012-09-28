package com.conref.refactoring.makeFieldThreadLocal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
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

import com.conref.refactoring.makeFieldThreadLocal.refactor.makeFieldThreadLocalRefactoring;
import com.conref.refactoring.makeFieldThreadLocal.refactorWizard.makeFieldThreadLocalRefactoringWizard;
import com.conref.util.Selection;
import com.conref.util.SelectionAnalyzer;
import com.conref.util.WorkbenchHelper;

public class makeFieldThreadLocal implements IEditorActionDelegate {

	private ITextEditor _editor;
	private IFile _file;
	private ICompilationUnit ICU;
	private CompilationUnit unit;
	private ASTRewrite rewriter;
	private Selection selection;
	private ASTNode selectedNode;
	private String selectedFieldName;
private boolean flagOfSetThreadLcoal=false;
	@Override
	public void run(IAction action) {
		Shell shell = WorkbenchHelper.getActiveShell();
		Change change=null;
		try {
			change = createChange();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		makeFieldThreadLocalRefactoring refactor= new makeFieldThreadLocalRefactoring(change);
		makeFieldThreadLocalRefactoringWizard wizard = new makeFieldThreadLocalRefactoringWizard(refactor);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
		try {
			op.run(shell, "make Field ThreadLocal");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	private Change createChange() throws JavaModelException {

		rewriter = ASTRewrite.create(unit.getAST());
		unit.accept(new ASTVisitor() {
			public boolean visit(final MethodDeclaration method){
				//replace use of the field with "field.get()";
				method.accept(new ASTVisitor(){
					@SuppressWarnings("unchecked")
					public boolean visit(SimpleName simplename){
						if(!flagOfSetThreadLcoal&&simplename.getIdentifier().equals(selectedFieldName)){
							AST ast=unit.getAST();
							MethodInvocation methodInvo = ast.newMethodInvocation();
							methodInvo.setName(ast.newSimpleName("set"));
							methodInvo.setExpression(ast.newSimpleName("threadlocal"));
//							StringLiteral stringLiteral4 = ast.newStringLiteral();
//				            stringLiteral4.setLiteralValue(selectedFieldName);
							
				            methodInvo.arguments().add(ast.newSimpleName(selectedFieldName));
				            ExpressionStatement statement = ast.newExpressionStatement(methodInvo);
				            ASTNode block=simplename;
				            while(!(block instanceof Block)){
				            	block=block.getParent();
				            }
							ListRewrite lr = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
							lr.insertLast(statement, null);
							flagOfSetThreadLcoal=true;

							//Problem:如果field在声明的时候没有实例化，那么如何转变代码
							//拟解决办法：如果field在声明的时候已经实例化了，那么在何处添加代码“xxx.set(field)”。
							//如果没有实例化，那么可以在其实例化的代码后面添加“XXX.set()”。
							//Problem：如何判断目标代码是实例化该field                     
						}
						
						return true;
					}
				});
				return true;
			};
			public boolean visit(final FieldDeclaration field) {

				field.accept(new ASTVisitor() {
					
					@SuppressWarnings("unchecked")
					public boolean visit(VariableDeclarationFragment vdf) {
						String vdfname = vdf.getName().toString();
						if(vdfname.equals(selectedFieldName)){
							//TODO:add refactoring code
							AST ast=unit.getAST();
							SimpleName fieldname = ast.newSimpleName("threadlocal");

							ClassInstanceCreation classInstanceCreation = ast
									.newClassInstanceCreation();
							classInstanceCreation.setType(ast.newSimpleType(ast
									.newName("ThreadLocal")));
							VariableDeclarationFragment fragment = ast
									.newVariableDeclarationFragment();
							fragment.setName(fieldname);
							fragment.setInitializer(classInstanceCreation);
							FieldDeclaration fd = ast.newFieldDeclaration(fragment);
							fd.setType(ast.newSimpleType(ast.newSimpleName("ThreadLocal")));
							fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
							fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
							fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
							ASTNode node=selectedNode;
							while(node.getNodeType()!=ASTNode.TYPE_DECLARATION){
								node=node.getParent();
							}
							TypeDeclaration cls=(TypeDeclaration)node;
							ListRewrite lrw = rewriter.getListRewrite(cls,
									TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
							lrw.insertFirst(fd, null);	
//							rewriter.remove(field, null);
						}
						return true;
					}
				});
				return true;
			}
		});
		String source = ICU.getSource();

		Document document = new Document(source);

		TextEdit edits = rewriter.rewriteAST(document, ICU.getJavaProject()
				.getOptions(true));
		TextFileChange change = new TextFileChange("",
				(IFile) ICU.getResource());
		change.setEdit(edits);
		return change;

	}
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof ITextSelection){

			ICU = (ICompilationUnit) JavaCore.createCompilationUnitFrom(_file);
			
		int s=((ITextSelection)selection).getOffset();
		int l=((ITextSelection)selection).getLength();
		this.selection=Selection.createFromStartLength(s, l);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(ICU);
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
		
		}
		else{
			action.setEnabled(false);
		}
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_editor = (ITextEditor) targetEditor;

		_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();			
	}

}
