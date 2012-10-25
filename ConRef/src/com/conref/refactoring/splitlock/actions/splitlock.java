package com.conref.refactoring.splitlock.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import com.conref.refactoring.splitlock.core.ClassFeildsAnalyzer;
import com.conref.refactoring.splitlock.core.JDTRewriter_manual;
import com.conref.refactoring.splitlock.core.JavaCriticalSection;
import com.conref.refactoring.splitlock.core.JavaCriticalSection.NoMatchingSootMethodException;
import com.conref.refactoring.splitlock.core.JavaCriticalSectionFinder;
import com.conref.refactoring.splitlock.core.MethodFieldScanner;
import com.conref.refactoring.splitlock.core.manualSplitSettingsDlg;
import com.conref.refactoring.splitlock.views.MethodsView;
import com.conref.util.PathUtils;
import com.conref.util.WorkbenchHelper;
@SuppressWarnings({"unchecked","rawtypes"})
public class splitlock implements IEditorActionDelegate,
		IWorkbenchWindowActionDelegate {
	private ICompilationUnit select;
	private static IFile _file;
	private ITextEditor _editor;
	private static String id = "test.views.SampleView";
	private static ClassFeildsAnalyzer analyzer;
	
	private Map<String, Map> lockmap = new HashMap();
	private CompilationUnit unit;
	private HashSet fieldsNames;

	@Override
	public void run(IAction action) {
	
		if (_editor == null) {
			return;
		}

		String id = action.getId();
		if (id.equals("ConRef.splitlock_auto")) {
			split_auto();
		}
		if (id.equals("ConRef.splitlock_manual")) {
			try {
				split_manual();
//				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD,null);
			} catch (JavaModelException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void split_manual() throws JavaModelException,
			BadLocationException, InterruptedException {
		getLockMap();
		Shell shell = WorkbenchHelper.getActiveShell();
<<<<<<< HEAD
		JDTRewriter_manual jdtRewriter = new JDTRewriter_manual(_file,lockmap);
		manualSplitSettingsDlg settings = new manualSplitSettingsDlg(shell,
				_file,jdtRewriter, lockmap);
		settings.open();
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

=======
		manualSplitSettingsDlg settings = new manualSplitSettingsDlg(null,
				_file, lockList);
		//if (settings.open() == IDialogConstants.OK_ID) {
//			ITextSelection selection = (ITextSelection) _editor
//					.getSelectionProvider().getSelection();
//			IMethod m = JDTUtils.getEnclosingMethod(_editor,
//					selection.getStartLine());
//			JDTRewriter_manual jdtRewriter = new JDTRewriter_manual(_file,lockList);
//			String methodname=m.getElementName();
//			Change change = jdtRewriter.collectASTChange(methodname);
//			WorkbenchHelper.showEditor(_file);
			splitRefactoring refactor = new splitRefactoring(null);
			splitRefactoringWizard wizard = new splitRefactoringWizard(refactor);
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
					wizard);
			op.run(shell, "Split Refactoring");
	//		jdtRewriter.addAnnotation(methodname);
		}
//	}
>>>>>>> 43afddf6fa6c20aaa2dd951f761dc9f4af511029
	private void split_auto() {
		//
		try {
			IWorkbenchPage page = WorkbenchHelper.openViewPage(id);
			// MethodsView viewpart = (MethodsView) page.findView(id);
			// Map<String, Map<String, Integer>> classMap = getClasses(_file);
			// viewpart.getViewer().setInput(classMap);

			page.showView(id);
			Job job = new Job("Analysis") {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("analysis in running,please wait!!!", 8);

					try {
						runAnalysis();
						//
						monitor.subTask("getting All synchrnizations");
						analyzer.getAllSyncs();
						monitor.worked(1);
						monitor.subTask("loading class");
						analyzer.SootExOpreation();
						monitor.worked(1);
						monitor.subTask("building callgraph");
						Collection<JavaCriticalSection> validSyncs = new HashSet<JavaCriticalSection>();
						analyzer.buildCallGraph(validSyncs);
						monitor.worked(1);
						monitor.subTask("assuring All Syncs In CallGraph");
						analyzer.assureAllSyncInCallGraph(validSyncs);
						monitor.worked(1);
						monitor.subTask("getting all involved mthods");
						analyzer.getAllInvolvedMethods();
						monitor.worked(1);
						monitor.subTask("building VarConn Graph");
						analyzer.buildVarConn(validSyncs);
						monitor.worked(1);
						monitor.subTask("classifying VarConn Graph");
						analyzer.classifyVarConn();
						//
					} catch (PartInitException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (NoMatchingSootMethodException e) {
						e.printStackTrace();
					}
					monitor.done();
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							try {
								updateView();
							} catch (PartInitException e) {
								e.printStackTrace();
							}
						}

					});
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		} catch (PartInitException e) {

			e.printStackTrace();

		}

	}

	private void runAnalysis() throws PartInitException, InterruptedException {
		String srcpath = PathUtils.getSrcPath(_file);
		String classpath = PathUtils.getClassPath(_file);
		try {
			analyzer = ClassFeildsAnalyzer.v(null, srcpath, classpath);
		} catch (NoMatchingSootMethodException e) {
			e.printStackTrace();
		}
		// Thread t = new Thread(analyzer);
		// t.start();
	}

	public static void updateView() throws PartInitException {
		IWorkbenchPage page = WorkbenchHelper.openViewPage(id);
		MethodsView viewpart = (MethodsView) page.findView(id);
		viewpart.getViewer().setInput(analyzer.getCandidateCls());
		page.showView(id);
	};

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
//		if(_file!=null){
//			try {
//				_file.refreshLocal(IResource.DEPTH_ZERO, null);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
		lockmap.clear(); 
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
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(select);
			unit = (CompilationUnit) parser.createAST(null);

		} else
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

		if (_editor != null) {
			_file = ((IFileEditorInput) _editor.getEditorInput()).getFile();
		}
	}

	public static Map<String, Map<String, Integer>> getClasses(IFile file) {

		String srcpath = PathUtils.getSrcPath(file);
		String classpath = PathUtils.getClassPath(file);
		Collection<JavaCriticalSection> result = JavaCriticalSectionFinder
				.getInstance(srcpath, classpath).getAllSyncs();

		Set<String> classes = new HashSet<String>();
		Map<String, Map<String, Integer>> mapResult = new HashMap<String, Map<String, Integer>>();
		for (JavaCriticalSection cs : result) {
			if (!cs.getLockName().equals("this"))
				continue;
			String classname = cs.getClassNameOfSourceFile();
			if (!classes.contains(classname)) {
				classes.add(classname);
				mapResult.put(classname, new HashMap<String, Integer>());
			}
			String method = cs.getMethodName();
			Integer line = new Integer(cs.getStartLine());
			mapResult.get(classname).put(method, line);
		}
		return mapResult;
	}

}
