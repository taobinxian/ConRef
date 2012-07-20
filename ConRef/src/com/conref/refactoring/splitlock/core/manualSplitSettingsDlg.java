package com.conref.refactoring.splitlock.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.conref.Global;

public class manualSplitSettingsDlg extends Dialog{
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
	private Table table;
	private Set<String> lockList;
private int id=1;
IFile _file;
private TableViewer tableViewer;
	public manualSplitSettingsDlg(Shell parentShell, IFile file,Set lockList) {
		super(parentShell);
		_file=file;
		this.lockList=lockList;
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(null);

		final Group optionsGroup = new Group(container, SWT.V_SCROLL);
		optionsGroup.setText("Options");
		optionsGroup.setBounds(0, 0, 435, 230);
		final Label label=new Label(optionsGroup, SWT.NONE);
		label.setBounds(40, 30, 150, 20);
		label.setText("add the new lock");
		 tableViewer = new TableViewer(optionsGroup,  SWT.FULL_SELECTION | SWT.BORDER|SWT.V_SCROLL);
		
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setBounds(40, 60, 150, 150);

		final TableColumn col1 = new TableColumn(table, SWT.NONE);
		col1.setWidth(50);
		col1.setText("ID");
		
		final TableColumn col2 = new TableColumn(table, SWT.NONE);
		col2.setWidth(100);
		col2.setText("name");	
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setInput(lockList);
	
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
				tableViewer.setInput(lockList);
			}
		});
		 ADD.setBounds(235,60, 60, 25);
		 ADD.setText("Add...");
		 
		 REMOVE = new Button(optionsGroup, SWT.NONE);
		 REMOVE.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				TableItem[] selection = tableViewer.getTable().getSelection();
				 String deleteLock = selection[0].getData().toString();
				lockList.remove(deleteLock);
				id=1;
				tableViewer.setInput(lockList);
			}
		});
		 REMOVE.setBounds(235,100, 60, 25);
		 REMOVE.setText("Remove");
		 
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
//	@Override
//	protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
//				true);
//		createButton(parent, IDialogConstants.CANCEL_ID,
//				IDialogConstants.CANCEL_LABEL, false);
//	}
//	@Override
//	protected Point getInitialSize() {
//		return new Point(442, 302);
//	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Setting Options for split lock manually");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			 TableItem[] selection = tableViewer.getTable().getSelection();
			 if(selection==null){
				 MessageDialog.openInformation(
							null,
							"selection",
							"please select a lock");
			 }
			String lockname=selection[0].getData().toString();
			Global.lockname=lockname;
		}
		super.buttonPressed(buttonId);
	}

}
