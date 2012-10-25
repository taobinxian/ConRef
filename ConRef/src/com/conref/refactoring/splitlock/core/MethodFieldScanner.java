package com.conref.refactoring.splitlock.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodFieldScanner {
	CompilationUnit unit;

private HashSet fieldsNames;


private Map<List,Set> fieldsMap=new HashMap();

	public MethodFieldScanner(CompilationUnit unit) {
		this.unit = unit;
		build();
	}

	private void build() {
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
				
				List<String> methodnameAndPara=new LinkedList();
				List paralist = method.parameters();
			for(int i=0;i<paralist.size();i++){
				methodnameAndPara.add(paralist.get(i).toString());
			}
				methodnameAndPara.add(method.getName().toString());
				final Set fieldsInMethod=new HashSet();
			method.accept(new ASTVisitor() {
				public boolean visit(SimpleName name) {
							String fieldname = name.toString();
							if (fieldsNames.contains(fieldname)) {
								fieldsInMethod.add(fieldname);
							}
							return true;
						}
					});
			fieldsMap.put(methodnameAndPara, fieldsInMethod);

				return true;

			}
		});
	}

	public Set<String> getFieldsInMethod(List methodnameAndParameters) {
		return fieldsMap.get(methodnameAndParameters);
	}

}
