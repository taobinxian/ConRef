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
