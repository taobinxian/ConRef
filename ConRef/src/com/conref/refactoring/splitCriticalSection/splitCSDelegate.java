package com.conref.refactoring.splitCriticalSection;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.util.Selection;
import com.conref.util.SelectionAnalyzer;

public class splitCSDelegate implements IEditorActionDelegate{

	private ITextEditor _editor;
	private IFile _file;
	private ASTNode firstSelectedNode;
	private AST ast;
	private ASTRewrite rewriter;

	@Override
	public void run(IAction action) {
		ASTNode parentNode=firstSelectedNode.getParent();
		while(!(parentNode instanceof MethodDeclaration)){
			parentNode=parentNode.getParent();
		}
		MethodDeclaration method=(MethodDeclaration) parentNode;
		List<Statement> stmtList=method.getBody().statements();
		for (Statement statement : stmtList) {
			if (statement.getStartPosition() == firstSelectedNode.getStartPosition()) {
				break;
			}
			MethodDeclaration newmethod=ast.newMethodDeclaration();
			Block methodbody=ast.newBlock();
			newmethod.setBody(methodbody);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof ITextSelection){
			int s=((ITextSelection)selection).getOffset();
			int l=((ITextSelection)selection).getLength();
			Selection sel=Selection.createFromStartLength(s, l);
			SelectionAnalyzer sa=new SelectionAnalyzer(sel, false);
			 firstSelectedNode=sa.getFirstSelectedNode();
		}
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

		_editor = (ITextEditor) targetEditor;

		_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();
		ICompilationUnit cu = (ICompilationUnit) JavaCore
				.createCompilationUnitFrom(_file);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		rewriter = ASTRewrite.create(unit.getAST());
		ast = unit.getAST();

	}
}
