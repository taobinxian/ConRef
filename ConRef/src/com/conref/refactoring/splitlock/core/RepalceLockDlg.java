package com.conref.refactoring.splitlock.core;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
@SuppressWarnings({ "rawtypes" })
public class RepalceLockDlg extends Dialog{
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
	public RepalceLockDlg(Shell parentShell, IFile file,Map lockMap) {
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
				IDialogConstants.CANCEL_LABEL, false);
	}
//	@Override
//	protected Point getInitialSize() {
//		return new Point(442, 302);
//	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Setting Options for split lock manually");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.NEXT_ID) {
			
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
		CheckboxTableViewer tv= CheckboxTableViewer.newCheckList(optionsGroup, SWT.BORDER);
		 Table ta = tv.getTable();
		ta.setLayoutData(new GridData(GridData.FILL_BOTH));
		ta.setLinesVisible(true);
		ta.setHeaderVisible(true);
		ta.setBounds(40, 60,350, 150);
		tv.setInput(lockMap.keySet());
		return container;
		
	}
}
}