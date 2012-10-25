package com.conref.refactoring.lockCallSites.Refactor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;

import com.conref.util.Selection;
import com.conref.util.SelectionAnalyzer;

public class lockCallSitesRefactoring extends Refactoring {
	private CompositeChange compositeChange=new CompositeChange("");
	private boolean otherClassNeedRefactoring;
	private ICompilationUnit unit;
	private String name="lock CallSites";
	private ASTNode selectedNode;
	private Selection selection;
	private CompilationUnit cu;
	private String selectedMethodName;
	private boolean isClassChanged=false;
	private boolean isLockFieldExist=false; 

	public lockCallSitesRefactoring(ICompilationUnit unit, int selectionStart,int selectionLength) {
		this.unit=unit;
		this.selection = Selection.createFromStartLength(selectionStart,selectionLength);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(unit);
		cu = (CompilationUnit) parser.createAST(null);
		SelectionAnalyzer sa = new SelectionAnalyzer(this.selection, true);
		cu.accept(sa);
		selectedNode = sa.getFirstSelectedNode();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();

	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,OperationCanceledException {
		int selectedNodeType = selectedNode.getNodeType();
		if (selectedNodeType == ASTNode.SIMPLE_NAME) {
			selectedMethodName=((SimpleName)selectedNode).getIdentifier();
		}
		if(selectedNodeType==ASTNode.VARIABLE_DECLARATION_FRAGMENT){
			selectedMethodName=((VariableDeclarationFragment)selectedNode).getName().getIdentifier();
		}
		IJavaElement[] elements=null;
		if(otherClassNeedRefactoring){
		IPackageFragment pr=(IPackageFragment) unit.getParent();
		elements= pr.getChildren();
		}else{
			elements=new IJavaElement[1];
			elements[0]=unit;
		}
		for (int i = 0; i < elements.length; i++) {
			ICompilationUnit unit = (ICompilationUnit) elements[i];
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(unit);
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
			if (changeMethodStatement(cu, rewriter)) {
				TextEdit edits = rewriter.rewriteAST();
				TextFileChange change = new TextFileChange("lock callsites",
						(IFile) unit.getResource());
				change.setEdit(edits);
				compositeChange.add(change);
			}
		}
		return compositeChange;
	}
		private boolean changeMethodStatement(final CompilationUnit cu,final ASTRewrite rewriter) {
			isClassChanged=false;
			 cu.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration method) {
				method.accept(new ASTVisitor() {

					@SuppressWarnings("unchecked")
					public boolean visit(MethodInvocation mi) {
						if (mi.getName().toString().equals(selectedMethodName)) {
							addLockField(mi);
							ASTNode node = mi;
							while (!(node instanceof Statement)) {
								node = node.getParent();
							}
							AST ast = cu.getAST();
							SynchronizedStatement sync = ast.newSynchronizedStatement();
							ASTNode stmt = ASTNode.copySubtree(ast, node);
							Block syncblock = ast.newBlock();
							syncblock.statements().add(stmt);
							sync.setBody(syncblock);
							SimpleName lockname = ast.newSimpleName("callSiteLock");
							sync.setExpression(lockname);
							rewriter.replace(node, sync, null);
							isClassChanged = true;
						}
						return true;
					}

					@SuppressWarnings("unchecked")
					private void addLockField(MethodInvocation mi) {
						if (!isLockFieldExist) {
							isLockFieldExist=true;
							AST ast = cu.getAST();
							SimpleName fieldname = ast.newSimpleName("callSiteLock");
							ClassInstanceCreation classInstanceCreation = ast.newClassInstanceCreation();
							classInstanceCreation.setType(ast.newSimpleType(ast.newName("Object")));
							VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
							fragment.setName(fieldname);
							fragment.setInitializer(classInstanceCreation);
							FieldDeclaration fd = ast.newFieldDeclaration(fragment);
							fd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
							fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
							ASTNode node = mi;
							while (node.getNodeType() != ASTNode.TYPE_DECLARATION) {
								node = node.getParent();
							}
							TypeDeclaration cls = (TypeDeclaration) node;
							ListRewrite lrw = rewriter.getListRewrite(cls,TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
							lrw.insertFirst(fd, null);
						}
					}
				});

				return true;
			}
			 });
			return isClassChanged;
	}

	public void setOtherClassNeedRefactoring(boolean b) {
		otherClassNeedRefactoring=b;
	}

}
