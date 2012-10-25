package com.conref.refactoring.mergeLocks.util;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
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

import com.conref.Global;

public class JDTRewriter_Merge {
	private final class syncStmtVisitor extends ASTVisitor {
		private Set<String> locknames;
		public syncStmtVisitor(Set<String> locknames){
			this.locknames=locknames;
		}
		public boolean visit(final FieldDeclaration fd){
			
			fd.accept(new ASTVisitor(){
				public boolean visit(VariableDeclarationFragment vd){
					boolean IsLockMerged=false;
					for(String lockname:locknames){
						if(vd.getName().toString().equals(lockname)){
							IsLockMerged=true;
						}
					}
					if (IsLockMerged) {
						rewriter.remove(fd, null);
					}
					return true;
				}
			});
			return true;
		}
		public boolean visit(SynchronizedStatement synstmt) {
			ASTNode node=synstmt;
			while(!(node instanceof TypeDeclaration)){
				node=node.getParent();
			}
			cls=(TypeDeclaration)node;
			boolean IsExpNeedChange=false;
			for(String lockname:locknames){
				if(lockname.equals(synstmt.getExpression().toString())){
					IsExpNeedChange=true;
				}
			}
			if (!IsExpNeedChange) {
				return true;
			}
		
				Expression exp = ast.newSimpleName(Global.lockname);
				rewriter.replace(synstmt.getExpression(), exp, null);
//			}
			return true;

		}
	}

//	private final class syncMethodVisitor extends ASTVisitor {
//		private final Set<String> methodnames;
//
//		private syncMethodVisitor(Set<String> methodnames) {
//			this.methodnames = methodnames;
//		}
//		@SuppressWarnings({ "unchecked" })
//		public boolean visit(MethodDeclaration method) {
//			boolean IsMethodNeedRefactoring=false;
//			for (String s : methodnames) {
//				if (method.getName().toString().equals(s)) {
//					IsMethodNeedRefactoring = true;
//					break;
//				}
//			}
//			if(!IsMethodNeedRefactoring){
//				return true;
//			}
//			String name = method.getName().toString();
//			cls = (TypeDeclaration) method.getParent();
//			//add field declaration
//					method.accept(new syncStmtVisitor());
//				
//			return true;
//		}
//
//	}

	private IFile _file;

	private AST ast;
	private TypeDeclaration cls;
	private ASTRewrite rewriter;
	private CompilationUnit unit;

	@SuppressWarnings("rawtypes")
	public JDTRewriter_Merge(IFile file,Map lockList) {
		this._file = file;
	}

	public Change collectASTChange(Set<String> locknames)
			throws JavaModelException, BadLocationException {
		return rewriteAST(locknames);
	}

	@SuppressWarnings("unchecked")
	private void addFieldDec() {
		boolean selectedLockIsExist=isExist(unit,Global.lockname);
		  if(!selectedLockIsExist){
			  SimpleName fieldname = ast.newSimpleName(Global.lockname);

				ClassInstanceCreation classInstanceCreation = ast
						.newClassInstanceCreation();
				classInstanceCreation.setType(ast.newSimpleType(ast
						.newName("Object")));
				VariableDeclarationFragment fragment = ast
						.newVariableDeclarationFragment();
				fragment.setName(fieldname);
				fragment.setInitializer(classInstanceCreation);
				FieldDeclaration fd = ast.newFieldDeclaration(fragment);
				fd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
				fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
			
				ListRewrite lrw = rewriter.getListRewrite(cls,
						TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				lrw.insertFirst(fd, null);		
		  }
	}
	private Change rewriteAST(Set<String>  locknames)
			throws JavaModelException, MalformedTreeException,
			BadLocationException {
		ICompilationUnit cu = (ICompilationUnit) JavaCore
		.createCompilationUnitFrom(_file);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		unit = (CompilationUnit) parser.createAST(null);
		rewriter = ASTRewrite.create(unit.getAST());
		ast = unit.getAST();
		
		unit.accept(new syncStmtVisitor(locknames));
		  addFieldDec();

		String source = cu.getSource();
		Document document = new Document(source);

		TextEdit edits = rewriter.rewriteAST(document, cu.getJavaProject()
				.getOptions(true));
		TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
		change.setEdit(edits);

		return change;
	}
	private boolean isExist;

	private boolean isExist(CompilationUnit unit,final String lockname) {
		unit.accept(new ASTVisitor() {

			@SuppressWarnings("unchecked")
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

