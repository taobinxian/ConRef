<<<<<<< HEAD
package com.conref.refactoring.splitlock.refactoringWizard;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.TableColumn;
@SuppressWarnings({ "rawtypes", "unchecked" })
public class splitlockInputPage extends Dialog{
	class TableLabelProvider extends LabelProvider  implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return element.toString();
			} else if (columnIndex == 1) {
				Map map=(Map) root.get(element);
				return map.keySet().toString();
			
			} else {
				Map map=(Map) root.get(element);
				return map.values().toString();
			}

		}
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof Map){
				root=(Map) inputElement;
				return root.keySet().toArray();
			}
			else if(inputElement instanceof Set){
				return ((Set) inputElement).toArray();
			}
			else{
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
	private Table table;
	private Map lockMap;
	IFile _file;
	private CheckboxTableViewer tableViewer;
	private Set<String> lockList=new HashSet<String>();
	public splitlockInputPage(Shell parentShell, IFile file,Map lockMap) {
		super(parentShell);
		_file=file;
		this.lockMap=lockMap;
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(null);

		final Group optionsGroup = new Group(container, SWT.V_SCROLL);
		optionsGroup.setText("Options");
		optionsGroup.setBounds(0, 0, 435, 230);
		final Label label=new Label(optionsGroup, SWT.NONE);
		label.setBounds(40, 30, 350, 20);
		label.setText("please select the lock need to split");
		tableViewer = CheckboxTableViewer.newCheckList(
				optionsGroup, SWT.BORDER);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setBounds(40, 60,350, 150);
		final TableColumn col1 = new TableColumn(table, SWT.NONE);
		col1.setWidth(100);
		col1.setText("Lock");
		final TableColumn col2 = new TableColumn(table, SWT.NONE);
		col2.setWidth(100);
		col2.setText("Var");
		final TableColumn col3 = new TableColumn(table, SWT.NONE);
		col3.setWidth(350);
		col3.setText("Context");
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setInput(lockMap);
	
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent,IDialogConstants.NEXT_ID,"replace",true);
		createButton(parent, IDialogConstants.OK_ID, "Next",false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, true);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Setting Options for split lock manually");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.NEXT_ID) {
			replaceLockDlg rlg=new replaceLockDlg(getParentShell());
			if (rlg.open() == IDialogConstants.OK_ID) {
			
//				Change change = jdtRewriter.collectASTChange(methodname);
//				WorkbenchHelper.showEditor(_file);
//				splitRefactoring refactor = new splitRefactoring(change);
//				splitmanualRefactoringWizard wizard = new splitmanualRefactoringWizard(
//						refactor);
//				RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
//						wizard);
//				op.run(shell, "Split Refactoring");
			}
		}
		
		if(buttonId == IDialogConstants.OK_ID)
		super.buttonPressed(buttonId);
	}
class replaceLockDlg extends Dialog{

	protected replaceLockDlg(Shell parentShell) {
		super(parentShell);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("select or create a lock to repalce the selected lock");
	}
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(null);
		final Group optionsGroup = new Group(container, SWT.V_SCROLL);
		optionsGroup.setText("Options");
		optionsGroup.setBounds(0, 0, 435, 230);
		final CheckboxTableViewer tv= CheckboxTableViewer.newCheckList(optionsGroup, SWT.BORDER);
		 Table ta = tv.getTable();
		ta.setLayoutData(new GridData(GridData.FILL_BOTH));
		ta.setLinesVisible(true);
		ta.setHeaderVisible(true);
		ta.setBounds(40, 60,200, 150);
		tv.setContentProvider(new ContentProvider());
		tv.setLabelProvider(new TableLabelProvider());
		tv.setInput(lockMap.keySet());
		Button ADD=new Button(optionsGroup, SWT.PUSH);
		 lockList.addAll(lockMap.keySet());
		ADD.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				InputDialog inputdlg=new InputDialog(null, "LockName", "input the name of the new lock", "splitLock",  new IInputValidator(){
					public String isValid(String newText){
						if(lockList.contains(newText)){
							return "this lock is exsit";
						}
						return null;
					}
			}
		);
				inputdlg.open();
				String newlockName = inputdlg.getValue();
				lockList.add(newlockName);
				tv.setInput(lockList);
	}
});
		ADD.setBounds(250,60, 60, 25);
		 ADD.setText("add");
		return container;
		
	}
}
}
=======
package com.conref.refactoring.splitlock.refactoringWizard;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class splitlockInputPage extends UserInputWizardPage{
	class TableLabelProvider extends LabelProvider  implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 0) {
					return String.valueOf(id++);
				} else if (columnIndex == 1) {
					return element.toString();
				}
				return null;

		}
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof Set){
				return ((Set)inputElement).toArray();
			}else{
				return new Object[0];
			}
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Button ADD;
	private Button REMOVE;
	Shell shell;
	private Table table_origin;
	private Table table_new;
	private Set<String> lockList;
private int id=1;
IFile _file;
private TableViewer tableViewer_origin;
private TableViewer tableViewer_new;
	private static String PAGE_NAME="splitlockInputPage";
	private Document fSignaturePreviewDocument;

	public splitlockInputPage() {
		super(PAGE_NAME);
//		setImageDescriptor(JavaPluginImages.DESC_WIZBAN_REFACTOR_CU);
//		setDescription(DESCRIPTION);
//		fFirstTime= true;
		fSignaturePreviewDocument= new Document();	}

	@Override
	public void createControl(Composite parent) {

		
//		Composite container = (Composite) super.createDialogArea(parent);
//		container.setLayout(null);

		final Group optionsGroup = new Group(parent, SWT.V_SCROLL);
		optionsGroup.setText("Options");
		optionsGroup.setBounds(0, 0, 435, 230);
		final Label label=new Label(optionsGroup, SWT.NONE);
		label.setBounds(40, 30, 150, 20);
		label.setText("add the new lock");
		 tableViewer_origin = new TableViewer(optionsGroup,  SWT.FULL_SELECTION | SWT.BORDER|SWT.V_SCROLL);

		table_origin = tableViewer_origin.getTable();
		table_origin.setLinesVisible(true);
		table_origin.setHeaderVisible(true);
		table_origin.setBounds(40, 60, 150, 150);
		tableViewer_new = new TableViewer(optionsGroup, SWT.FULL_SELECTION
				| SWT.BORDER | SWT.V_SCROLL);
		table_new = tableViewer_new.getTable();
		table_new.setLinesVisible(true);
		table_new.setHeaderVisible(true);
		table_new.setBounds(270, 60, 150, 150);
		final TableColumn col1 = new TableColumn(table_origin, SWT.NONE);
		col1.setWidth(50);
		col1.setText("Sync");
		
		final TableColumn col2 = new TableColumn(table_origin, SWT.NONE);
		col2.setWidth(100);
		col2.setText("Lock");	
		tableViewer_origin.setContentProvider(new ContentProvider());
		tableViewer_origin.setLabelProvider(new TableLabelProvider());
		tableViewer_origin.setInput(lockList);
	
		 ADD = new Button(optionsGroup, SWT.NONE);
		 ADD.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				InputDialog inputdlg=new InputDialog(null, "LockName", "input the name of the new lock", "splitLock",  new IInputValidator(){
					public String isValid(String newText){
						if(lockList.contains(newText)){
							return "this lock is exsit";
						}
						return null;
					}
				} );
				inputdlg.open();
				String lockName = inputdlg.getValue();
				id=1;
				lockList.add(lockName);
				tableViewer_origin.setInput(lockList);
			}
		});
		 ADD.setBounds(205,80, 60, 25);
		 ADD.setText(">>>");
		 
		 REMOVE = new Button(optionsGroup, SWT.NONE);
		 REMOVE.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				TableItem[] selection = tableViewer_origin.getTable().getSelection();
				 String deleteLock = selection[0].getData().toString();
				lockList.remove(deleteLock);
				id=1;
				tableViewer_origin.setInput(lockList);
			}
		});
		 REMOVE.setBounds(205,160, 60, 25);
		 REMOVE.setText("<<<");
		 
	}

}
>>>>>>> 43afddf6fa6c20aaa2dd951f761dc9f4af511029
