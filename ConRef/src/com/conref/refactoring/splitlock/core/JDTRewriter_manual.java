package com.conref.refactoring.splitlock.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.Global;
import com.conref.refactoring.splitlock.core.JavaCriticalSection.NoMatchingSootMethodException;
import com.conref.util.PathUtils;
import com.conref.util.WorkbenchHelper;

import soot.SootField;

public class JDTRewriter_manual {
	private final class syncStmtVisitor extends ASTVisitor {
		@SuppressWarnings("unchecked")
		public boolean visit(SynchronizedStatement synstmt) {

			if (synstmt.getExpression() instanceof ThisExpression) {
				ASTNode node = synstmt;
				while (!(node instanceof MethodDeclaration)) {
					node = node.getParent();
				}
				Expression exp = ast.newSimpleName(Global.lockname);
				rewriter.replace(synstmt.getExpression(), exp, null);
			}
			return true;

		}
	}

	private final class syncMethodVisitor extends ASTVisitor {
		private final String methodname;

		private syncMethodVisitor(String methodname) {
			this.methodname = methodname;
		}

		@SuppressWarnings({ "unchecked" })
		public boolean visit(MethodDeclaration method) {
			if (method.getName().toString().equals(methodname)) {
				cls = (TypeDeclaration) method.getParent();
				List<Modifier> modifiers = method.modifiers();
				List<String> md = new ArrayList<String>();
				for (Modifier md1 : modifiers) {
					md.add(md1.toString());
				}
				if (md.contains("synchronized")) {
					Iterator<Modifier> it = modifiers.iterator();
					while (it.hasNext()) {
						Modifier str = it.next();
						if (str.toString().equals("synchronized")) {
							rewriter.remove(str, null);
						}
					}

					Block newBody = ast.newBlock();
					SynchronizedStatement statement = ast
							.newSynchronizedStatement();
					ASTNode s = ASTNode.copySubtree(ast, method.getBody());

					statement.setBody((Block) s);

					statement.setExpression(ast.newSimpleName(Global.lockname));
					newBody.statements().add(statement);
					rewriter.replace(method.getBody(), newBody, null);
				} else {
					method.accept(new syncStmtVisitor());
				}
			}
			return true;
		}
	}

	private IFile _file;
	private ClassFeildsAnalyzer analyzer;
	private AST ast;
	private TypeDeclaration cls;
	private ASTRewrite rewriter;
	private boolean flag = false;
	private Set<String> lockList;

	public JDTRewriter_manual(IFile file,Set<String> lockList) {
		this._file = file;
		this.lockList=lockList;
	}

	public Change collectASTChange(String methodname)
			throws JavaModelException, BadLocationException {
		return rewriteAST(methodname);
	}

	public void addAnnotation(String methodname) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart dirtyEditor = null;
		try {
			dirtyEditor = IDE.openEditor(page, _file);
		} catch (PartInitException e) {
			//
			e.printStackTrace();
		}
		String srcpath = PathUtils.getSrcPath(_file);
		String classpath = PathUtils.getClassPath(_file);
		JavaCriticalSectionFinder.reset();
		Collection<JavaCriticalSection> result = JavaCriticalSectionFinder
				.getInstance(srcpath, classpath).getAllSyncs();
		int offset = 0;
		int length = 0;
		for (JavaCriticalSection cs : result) {
			if (cs.getMethodName().equals(methodname)) {
				offset = cs.getSync().getStartPosition();
				length = cs.getSync().getLength();
				break;
			}
		}
		WorkbenchHelper.selectInEditor((ITextEditor) dirtyEditor, offset,
				length);
	}

	private Change rewriteAST(final String methodname)
			throws JavaModelException, MalformedTreeException,
			BadLocationException {
		ICompilationUnit cu = (ICompilationUnit) JavaCore
				.createCompilationUnitFrom(_file);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		rewriter = ASTRewrite.create(unit.getAST());
		ast = unit.getAST();
		unit.accept(new syncMethodVisitor(methodname));
		
			for (String lockname:lockList) {
				SimpleName lock = ast.newSimpleName(lockname);
                if(isExist(unit,lockname)){
                	continue;
                }
				ClassInstanceCreation classInstanceCreation = ast
						.newClassInstanceCreation();
				classInstanceCreation.setType(ast.newSimpleType(ast
						.newName("Object")));
				VariableDeclarationFragment fragment = ast
						.newVariableDeclarationFragment();
				fragment.setName(lock);
				fragment.setInitializer(classInstanceCreation);
				FieldDeclaration fd = ast.newFieldDeclaration(fragment);
				fd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
				fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
				ListRewrite lrw = rewriter.getListRewrite(cls,
						TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				lrw.insertFirst(fd, null);
			}
	

		String source = cu.getSource();
		Document document = new Document(source);

		TextEdit edits = rewriter.rewriteAST(document, cu.getJavaProject()
				.getOptions(true));
		TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
		change.setEdit(edits);
		// edits.apply(document);
		// String newSource = document.get();
		//
		// // update of the compilation unit
		// cu.getBuffer().setContents(newSource);
		return change;
	}
	private boolean isExist;

	private boolean isExist(CompilationUnit unit,final String lockname) {
		unit.accept(new ASTVisitor() {

			public boolean visit(FieldDeclaration fd) {
				List<VariableDeclarationFragment> list = fd.fragments();
				for (VariableDeclarationFragment f : list) {
					if(isExist){continue;}
					 isExist = f.getName().toString().equals(lockname);
				}
				return true;
			}
		});
		if(isExist){
			return true;
		}
		return false;
	}

	private String getPath() {
		String path = _file.getLocation().toString();
		return path;
	}

	@SuppressWarnings("unchecked")

	public static String getFileName(IFile _file) {
		String name = _file.getLocation().toString();
		int location = name.indexOf("src") + 4;
		name = name.substring(location);
		name = name.replaceAll("/", ".");
		location = name.lastIndexOf(".");
		name = name.substring(0, location);
		return name;
	}

}
