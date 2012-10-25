package com.conref.refactoring.mergeLocks.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.mergeLocks.util.JDTRewriter_Merge;
import com.conref.refactoring.mergeLocks.util.mergeSettingDlg;
import com.conref.refactoring.splitlock.core.MethodFieldScanner;
import com.conref.util.WorkbenchHelper;
@SuppressWarnings({"rawtypes","unchecked"})
public class mergeLocks implements IEditorActionDelegate{

	private ITextEditor _editor;
	private IFile _file;
	private ICompilationUnit select;
	private CompilationUnit unit;
	
	private HashSet fieldsNames;
	
	private Map<String, Map> lockmap = new HashMap(); 
	@Override
	public void run(IAction action) {
		getLockMap();
		Shell shell = WorkbenchHelper.getActiveShell();
		JDTRewriter_Merge jdtRewriter = new JDTRewriter_Merge(_file,lockmap);
		mergeSettingDlg settings = new mergeSettingDlg(shell,
				_file,jdtRewriter, lockmap);
		settings.open();
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
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(select);
			unit = (CompilationUnit) parser.createAST(null);

		} else
			select = null;
//		action.setEnabled(false);
	
	}
	private void getLockMap() {
		lockmap.clear();
		 final MethodFieldScanner mfc=new MethodFieldScanner(unit);
		unit.accept(new ASTVisitor() {
		
			public boolean visit(final MethodDeclaration method) {
				TypeDeclaration cls = (TypeDeclaration) method.getParent();
				FieldDeclaration[] fields = cls.getFields();
				fieldsNames = new HashSet();
				for (int i = 0; i < fields.length; i++) {
					VariableDeclarationFragment vardec = (VariableDeclarationFragment) fields[i]
							.fragments().iterator().next();
					fieldsNames.add(vardec.getName().toString());
				}
				List<Modifier> modifiers = method.modifiers();
				List<String> md = new ArrayList<String>();
				for (Modifier md1 : modifiers) {
					md.add(md1.toString());
				}
				if (md.contains("synchronized")) {

					Map vars_lockedby_This = lockmap.get("this");
					if (vars_lockedby_This == null) {
						vars_lockedby_This = new HashMap();
						lockmap.put("this", vars_lockedby_This);
					}
					List Parameters = method.parameters();
					List methodnameAndParameters=new LinkedList();
					for(int i=0;i<Parameters.size();i++){
						methodnameAndParameters.add(Parameters.get(i));
					}
					methodnameAndParameters.add(method.getName().toString());
					Set<String> fieldsInMethod = mfc.getFieldsInMethod(methodnameAndParameters);
//					Set<String> fieldsInMethod=getFieldsInMethod(method);
					vars_lockedby_This.put(fieldsInMethod, method);

				} else {
					method.accept(new ASTVisitor() {
						public boolean visit(final SynchronizedStatement sync) {
							final Set fieldsInSync=new HashSet();
							final String lockname = sync.getExpression().toString();
							sync.accept(new ASTVisitor() {

								public boolean visit(SimpleName name) {
									
									String fieldname = name.toString();
									if(fieldname.toString().equals(lockname)){
										return true;
									}
									if (fieldsNames.contains(fieldname)) {
										
										fieldsInSync.add(fieldname);
									}
									return true;

								}
							});
				Map vars_lockedby_lockname = lockmap.get(lockname);
				if (vars_lockedby_lockname == null) {
					vars_lockedby_lockname = new HashMap();
					lockmap.put(lockname,vars_lockedby_lockname);
					}
					vars_lockedby_lockname.put(fieldsInSync,sync);
							return true;
						}

					});

				}

				return true;

			}
		});
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_editor = (ITextEditor) targetEditor;

		if (_editor != null) {
			_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();
		}
	}

}
