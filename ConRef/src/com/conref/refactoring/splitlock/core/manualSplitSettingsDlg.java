package com.conref.refactoring.splitlock.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.conref.Global;
import com.conref.refactoring.splitlock.refactor.splitRefactoring;
import com.conref.refactoring.splitlock.refactoringWizard.splitmanualRefactoringWizard;
import com.conref.util.WorkbenchHelper;
@SuppressWarnings({ "rawtypes", "unchecked" })
public class manualSplitSettingsDlg extends Dialog {
	class MyTreeContenetProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (root.keySet().contains(parentElement)) {
				return ((Map) root.get(parentElement)).keySet().toArray();
			}
			// return new Object[0];
			return null;
		}

		public Object getParent(Object element) {
			for (Object cls : root.keySet()) {
				if (((Map) root.get(cls)).keySet().contains(element)) {
					return cls;
				}
			}
			return null;

		}

		public boolean hasChildren(Object element) {
			return getChildren(element) != null;
		}

		public Object[] getElements(Object inputElement) {

			if (inputElement instanceof Map) {
				root = (Map) inputElement;
				return root.keySet().toArray();
			}
			return new HashSet(0).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class MyTreeLableProvider extends LabelProvider implements
			ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return element.toString();
			}
			if (columnIndex == 1) {
				for (Object map : root.values()) {
					if (((Map) map).containsKey(element)) {
						return ((Map) map).get(element).toString();
					}
				}
			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

	class TableLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return element.toString();
			} else if (columnIndex == 1) {
				Map map = (Map) root.get(element);
				return map.keySet().toString();

			} else {
				Map map = (Map) root.get(element);
				return map.values().toString();
			}

		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Map) {
				root = (Map) inputElement;
				return root.keySet().toArray();
			} else if (inputElement instanceof Set) {
				return ((Set) inputElement).toArray();
			} else {
				return new Object[0];
			}
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	Map root;
	Shell shell;
	private Tree tree;
	private Map lockMap;
	IFile _file;
	private CheckboxTreeViewer treeViewer;
	private Set<String> lockList = new HashSet<String>();
	private String newlock;
	JDTRewriter_manual jdtRewriter;
	private String methodname;

	public manualSplitSettingsDlg(Shell parentShell, IFile file,
			JDTRewriter_manual jdtRewriter, Map lockMap) {
		super(parentShell);
		this._file = file;
		this.jdtRewriter = jdtRewriter;
		this.lockMap = lockMap;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(null);

		final Group optionsGroup = new Group(container, SWT.V_SCROLL);
		optionsGroup.setText("Options");
		optionsGroup.setBounds(0, 0, 435, 230);
		final Label label = new Label(optionsGroup, SWT.NONE);
		label.setBounds(40, 30, 350, 20);
		label.setText("please select the lock need to split");
		treeViewer = new CheckboxTreeViewer(optionsGroup, SWT.BORDER);
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tree.setBounds(40, 60, 350, 150);
		final TreeColumn col1 = new TreeColumn(tree, SWT.NONE);
		col1.setWidth(150);
		col1.setText("Lock");
		final TreeColumn col3 = new TreeColumn(tree, SWT.NONE);
		col3.setWidth(350);
		col3.setText("Context");
		treeViewer.setContentProvider(new MyTreeContenetProvider());
		treeViewer.setLabelProvider(new MyTreeLableProvider());
		treeViewer.setInput(lockMap);
		treeViewer.expandAll();
		treeViewer.addCheckStateListener(new ICheckStateListener(){

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if(event.getChecked())
				getButton(IDialogConstants.OK_ID).setEnabled(true);		
			}});
		return container;
	}

	/**
	 * Create contents of the button bar
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "replace", true);
//		createButton(parent, IDialogConstants.OK_ID, "Next", true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);

	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Setting Options for split lock manually");
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			replaceLockDlg rlg = new replaceLockDlg(getParentShell());
			if (rlg.open() == IDialogConstants.OK_ID) {

				Global.lockname = newlock;
				
				//appear preview dialog
				
				creatchange();

			}
		}
		super.buttonPressed(buttonId);
	}

	private void creatchange() {
		Object[] locksNeedChange = treeViewer.getCheckedElements();
		TreeItem[] items = treeViewer.getTree().getItems();
		for (TreeItem treeItem : items) {
			TreeItem[] subItems = treeItem.getItems();
			for (int i = 0; i < subItems.length; i++) {
				String itemString = subItems[i].getData().toString();
				if (itemString.equals(locksNeedChange[0].toString())) {
					String parent = treeItem.getData().toString();
					Map map = (Map) lockMap.get(parent);
					Set set = map.entrySet();
					for (Object o : set) {
						if (((Entry) o).getKey().toString()
								.equals(locksNeedChange[0].toString())) {
							ASTNode node = (ASTNode) (((Entry) o)
									.getValue());
							if (node instanceof MethodDeclaration) {
								methodname=((MethodDeclaration) node)
										.getName().toString();
							} else {
								while (!(node instanceof MethodDeclaration)) {
									node = node.getParent();
								}
								methodname=((MethodDeclaration) node)
										.getName().toString() ;
							}
						}
					}
				}
			}
		}

		Change change;
		try {
			change = jdtRewriter.collectASTChange(methodname);

			WorkbenchHelper.showEditor(_file);
			splitRefactoring refactor = new splitRefactoring(change);
			splitmanualRefactoringWizard wizard = new splitmanualRefactoringWizard(
					refactor);
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
					wizard);
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			Shell shell = window.getShell();
			op.run(shell, "Split Refactoring");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	class replaceLockDlg extends Dialog {
		CheckboxTableViewer tv;

		protected replaceLockDlg(Shell parentShell) {
			super(parentShell);
		}

		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("select or create a lock to repalce the selected lock");
		}
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, "OK", true);
			createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", true);
			getButton(OK).setEnabled(false);
		}
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				Object[] checkedlock = tv.getCheckedElements();
				if (checkedlock.length == 0) {
				}
				newlock = (String) checkedlock[0];

			}
			super.buttonPressed(buttonId);
		}

		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			container.setLayout(null);
			final Group optionsGroup = new Group(container, SWT.V_SCROLL);
			optionsGroup.setText("Options");
			optionsGroup.setBounds(0, 0, 435, 230);
			tv = CheckboxTableViewer.newCheckList(optionsGroup, SWT.BORDER);
			Table ta = tv.getTable();
			ta.setLayoutData(new GridData(GridData.FILL_BOTH));
			ta.setLinesVisible(true);
			ta.setHeaderVisible(true);
			ta.setBounds(40, 60, 200, 150);
			tv.setContentProvider(new ContentProvider());
			tv.setLabelProvider(new TableLabelProvider());
			tv.setInput(lockMap.keySet());
			Button ADD = new Button(optionsGroup, SWT.PUSH);
			lockList.addAll(lockMap.keySet());
			ADD.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent e) {
					InputDialog inputdlg = new InputDialog(null, "LockName",
							"input the name of the new lock", "splitLock",
							new IInputValidator() {
								public String isValid(String newText) {
									if (lockList.contains(newText)) {
										return "this lock is exsit";
									}
									return null;
								}
							});
					inputdlg.open();
					String newlockName = inputdlg.getValue();
					lockList.add(newlockName);
					tv.setInput(lockList);

				}
			});
			ADD.setBounds(250, 60, 60, 25);
			ADD.setText("add");
			tv.addCheckStateListener(new ICheckStateListener(){

				@Override
				public void checkStateChanged(CheckStateChangedEvent event) {
					if(event.getChecked())
					getButton(OK).setEnabled(true);		
				}});
			return container;

		}

	}
}