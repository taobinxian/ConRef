/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate 
 * copyright notice.  All rights reserved.
 */
package com.conref.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;



public class JDTUtils {
    private static final IJavaElement[] EMPTY_RESULT = new IJavaElement[0];

    /**     
     * Finds a type by its qualified type name (dot separated).
     * @param jproject The java project to search in
     * @param fullyQualifiedName The fully qualified name (type name with enclosing type names and package (all separated by dots))
     * @return The type found, or null if not existing
    */
    public static IType findType(IJavaProject jproject, String fullyQualifiedName) throws JavaModelException {
        fullyQualifiedName = fullyQualifiedName.replace('$', '.');
        IType type = jproject.findType(fullyQualifiedName, (IProgressMonitor)null);
        if (type != null) return type;
        
        IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            IPackageFragmentRoot root = roots[i];
            type = findType(root, fullyQualifiedName);
            if (type != null && type.exists()) 
            	return type;
        }

        return null;
    }    

    private static IType findType(IPackageFragmentRoot root, String fullyQualifiedName) 
         throws JavaModelException {
        IJavaElement[] children = root.getChildren();
        for (int i = 0; i < children.length; i++) {
            IJavaElement element = children[i];
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                IPackageFragment pack = (IPackageFragment)element;
                if (!fullyQualifiedName.startsWith(pack.getElementName())) continue;
                IType type = findType(pack, fullyQualifiedName);
                if (type != null && type.exists()) return type;
            }
        }

        return null;
    }    

    private static IType findType(IPackageFragment pack, String fullyQualifiedName) 
            throws JavaModelException {
        ICompilationUnit[] cus = pack.getCompilationUnits();
        for (int i = 0; i < cus.length; i++) {
            ICompilationUnit unit = cus[i];
            IType type = findType(unit, fullyQualifiedName);
            if (type != null && type.exists()) return type;
        }

        return null;
    }

    private static IType findType(ICompilationUnit cu, String fullyQualifiedName) 
            throws JavaModelException {
        IType[] types = cu.getAllTypes();
        for (int i = 0; i < types.length; i++) {
            IType type = types[i];
            if (getFullyQualifiedName(type).equals(fullyQualifiedName)) return type;
        }

        return null;
    }    

    /**
     * Returns the fully qualified name of the given type using '.' as separators.
     * This is a replace for IType.getFullyQualifiedTypeName
     * which uses '$' as separators. As '$' is also a valid character in an id
     * this is ambiguous. JavaCore PR: 1GCFUNT
    */

    public static String getFullyQualifiedName(IType type) {
        try {
            if (type.isBinary() && !type.isAnonymous()) {
                IType declaringType = type.getDeclaringType();

                if (declaringType != null) {
                    return getFullyQualifiedName(declaringType) + '.' + type.getElementName();
                }
            }
        } catch (JavaModelException e) {
        }

        return type.getFullyQualifiedName('.');
    }

    

    /**
     * Force a reconcile of a compilation unit.
     * @param unit
    */
    public static void reconcile(ICompilationUnit unit) throws JavaModelException {
        unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
    }
    

    /**
     * Returns the package fragment root of &lt;code&gt;IJavaElement&lt;/code&gt;. If the given
     * element is already a package fragment root, the element itself is returned.
    */
    public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
        return (IPackageFragmentRoot)element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    }
    

    /**
     * Resolves a type name in the context of the declaring type.
     * @param refTypeSig the type name in signature notation (for example 'QVector') this can also be an array type,
     *        but dimensions will be ignored.
     * @param declaringType the context for resolving (type where the reference was made in)
     * @return returns the fully qualified type name or build-in-type name. if a unresolved type couldn't be 
     *         resolved null is returned
    */
    public static String getResolvedTypeName(String refTypeSig, IType declaringType) throws JavaModelException {
        int arrayCount = Signature.getArrayCount(refTypeSig);
        char type = refTypeSig.charAt(arrayCount);
        
        String fullName = null; 
        if (type == Signature.C_UNRESOLVED) {
            String name = "";
            int bracket = refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
            if (bracket > 0){ 
            	name = refTypeSig.substring(arrayCount + 1, bracket); 
            }
            else{
                int semi = refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
                if (semi == -1) {
                    throw new IllegalArgumentException();
                }

                name = refTypeSig.substring(arrayCount + 1, semi);
            }

            String[][] resolvedNames = declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) {
            	String pkgname = resolvedNames[0][0];
            	String clsname = resolvedNames[0][1];
            	
            	StringBuffer buf = new StringBuffer();
            	for(int i=0;i<clsname.length();i++){
            		char ch = clsname.charAt(i);
            		if(ch=='.')
            			buf.append('$');
            		else
            			buf.append(ch);
            	}
            	
            	clsname = buf.toString();
            	
                fullName = concatenateName(pkgname, clsname);
            }
        } else  {
            fullName = Signature.toString(refTypeSig.substring(arrayCount));
        }
        
        //append array tag
        for(int i=0;fullName!=null && i<arrayCount;i++){
        	fullName += "[]";
        }
        
        return fullName;
    }


    /*public static String getResolvedTypeFileName(String refTypeSig, IType declaringType) 
             throws JavaModelException {
        int arrayCount = Signature.getArrayCount(refTypeSig);
        char type = refTypeSig.charAt(arrayCount);
        if (type == Signature.C_UNRESOLVED) {
            String name = "";
            int bracket = refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
            if (bracket > 0) 
            	name = refTypeSig.substring(arrayCount + 1, bracket); 
            else {
                int semi = refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
                if (semi == -1) {
                    throw new IllegalArgumentException();
                }
                
                name = refTypeSig.substring(arrayCount + 1, semi);
            }

            String[][] resolvedNames = declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) {
                return concatenateName(resolvedNames[0][0], resolvedNames[0][1].replace('.', '$'));
            }
            return "*";
        } else  {
            return Signature.toString(refTypeSig.substring(arrayCount));
        }
    }*/
    

    /**
     * Determine if refTypeSig is unresolved.
    */
    /*public static boolean isTypeNameUnresolved(String refTypeSig) {
        return refTypeSig.charAt(Signature.getArrayCount(refTypeSig)) == Signature.C_UNRESOLVED;

    }
     
    public static String resolveTypeName(String refTypeSig, IType declaringType) 
           throws JavaModelException {
        int arrayCount = Signature.getArrayCount(refTypeSig);
        char type = refTypeSig.charAt(arrayCount);
        if (type == Signature.C_UNRESOLVED) {
            String name = "";
            int bracket = refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
            if (bracket > 0) name = refTypeSig.substring(arrayCount + 1, bracket); else  {
                int semi = refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
                if (semi == -1) {
                    throw new IllegalArgumentException();
                }

                name = refTypeSig.substring(arrayCount + 1, semi);
            }

            String[][] resolvedNames = declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) {
                return JDTUtils.concatenateName(resolvedNames[0][0], resolvedNames[0][1]);
            } else  {
                throw new JavaModelException(new Exception(name + " not resolvable"), 
                		                     IJavaModelStatusConstants.EVALUATION_ERROR);
            }
        }

        return refTypeSig;
    }
    
    
    public static void resolveTypeName(String refTypeSig, IType declaringType, StringBuffer sb) 
         throws JavaModelException {
        if (isTypeNameUnresolved(refTypeSig)) {
            sb.append(refTypeSig.substring(0, refTypeSig.indexOf(Signature.C_UNRESOLVED)));
            sb.append('L');
            sb.append(resolveTypeName(refTypeSig, declaringType));
            sb.append(';');
        } else  {
            sb.append(refTypeSig);
        }
    }*/
    

    /**
     * Concatenates two names. Uses a dot for separation.
     * Both strings can be empty or &lt;code&gt;null&lt;/code&gt;.
    */
    public static String concatenateName(String name1, String name2) {
        StringBuffer buf = new StringBuffer();
        if (name1 != null && name1.length() > 0) {
            buf.append(name1);
        }

        if (name2 != null && name2.length() > 0) {
            if (buf.length() > 0) {
                buf.append('.');
            }

            buf.append(name2);
        }

        return buf.toString();
    }
    

    public static IJavaElement[] codeResolve(ITextEditor editor) throws JavaModelException {
        return codeResolve(editor, true);
    }
    

    /**
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @since 3.2
    */
    public static IJavaElement[] codeResolve(ITextEditor editor, boolean primaryOnly) 
           throws JavaModelException {
        return codeResolve(getInput(editor, primaryOnly), 
        		          (ITextSelection)editor.getSelectionProvider().getSelection());
    }
    

    public static IJavaElement getInput(IEditorPart editor) {
        return getInput(editor, true);
    }

    

    /**
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @since 3.2
    */
    private static IJavaElement getInput(IEditorPart editor, boolean primaryOnly) {
        if (editor == null) 
        	return null;

        return getEditorInputJavaElement(editor, primaryOnly);
    }    

    public static ICompilationUnit getInputAsCompilationUnit(ITextEditor editor) {
        Object editorInput = getInput(editor);
        if (editorInput instanceof ICompilationUnit) 
        	return (ICompilationUnit)editorInput;

        return null;
    }    

    /**
     * Returns the given editor's input as Java element.
     * @param editor the editor
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @return the given editor's input as Java element or &lt;code&gt;null&lt;/code&gt; if none
     * @since 3.2
    */
    public static IJavaElement getEditorInputJavaElement(IEditorPart editor, boolean primaryOnly) {
        assert (editor!=null);
        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput == null) 
        	return null;

        IJavaElement je = getEditorInputJavaElement(editorInput);

        if (je != null || primaryOnly) 
        	return je;

        return JavaUI.getWorkingCopyManager().getWorkingCopy(editorInput);
    }    

    /**
     * Returns the Java element wrapped by the given editor input.     
     * @param editorInput the editor input
     * @return the Java element wrapped by &lt;code&gt;editorInput&lt;/code&gt; or &lt;code&gt;null&lt;/code&gt; if none
     * @since 3.2
    */
    public static IJavaElement getEditorInputJavaElement(IEditorInput editorInput) {
        IJavaElement je = JavaUI.getWorkingCopyManager().getWorkingCopy(editorInput);
        if (je != null) 
        	return je;
        return (IJavaElement)editorInput.getAdapter(IJavaElement.class);
    }
    

    public static IJavaElement[] codeResolve(IJavaElement input, ITextSelection selection) 
           throws JavaModelException {
        return codeResolve(input,selection.getOffset(), selection.getLength());
    }    

    public static IJavaElement[] codeResolve(IJavaElement input, int offset,int length) 
    	throws JavaModelException {
    	if (input instanceof ICodeAssist) {
    		if (input instanceof ICompilationUnit) {
    			reconcile((ICompilationUnit)input);
    		}

    		IJavaElement[] elements = ((ICodeAssist)input).codeSelect(offset, length);
    		if (elements != null && elements.length > 0)
    			return elements;
    	}

    	return EMPTY_RESULT;
    }    
    
    public static IJavaElement[] codeResolve(IEditorPart editor, int offset,int length)
    	throws JavaModelException {
    	IJavaElement input = getInput(editor);
    	return codeResolve( input, offset, length);
    }
    
    
    /**
     * @param primaryOnly if &lt;code&gt;true&lt;/code&gt; only primary working copies will be returned
     * @since 3.2
     */
    public static IJavaElement getElementAtOffset(ITextEditor editor, boolean primaryOnly) 
    	throws JavaModelException {
    	ITextSelection selection = (ITextSelection)editor.getSelectionProvider().getSelection();
        return getElementAtOffset(getInput(editor, primaryOnly), selection.getOffset());
    }
  
    public static IJavaElement getElementAtOffset(IJavaElement input, int offset) throws JavaModelException {
		if (input instanceof ICompilationUnit) {
			ICompilationUnit cunit = (ICompilationUnit) input;
			reconcile(cunit);
			IJavaElement ref = cunit.getElementAt(offset);
			if (ref == null)
				return input;
			else
				return ref;

		} else if (input instanceof IClassFile) {
			IJavaElement ref = ((IClassFile) input).getElementAt(offset);
			if (ref == null)
				return input;
			else
				return ref;
		}
		return null;
	}
    
    public static IMethod getMethod(ILocalVariable local){
    	IJavaElement parent = local.getParent();
    	IMethod method = null;
    	while(parent!=null){
    		if(parent instanceof IMethod){
    			method = (IMethod)parent;
    			break;
    		}
    		
    		parent = parent.getParent();
    	}
    	
    	return method;
    }
    
    /** Get the soot signature (recognizable by soot) of a given method. */
    public static String getMethodSootSignature(IMethod method){
    	IType declaringType = method.getDeclaringType();

		try {
			String m = "<" + method.getDeclaringType().getFullyQualifiedName();
			String returnType = method.getReturnType();
			returnType = JDTUtils.getResolvedTypeName(returnType, declaringType);
			// returnType = Signature.toString(returnType);
			m += ": " + returnType;
			m += " " + method.getElementName() + "(";

			String[] params = method.getParameterTypes();
			for (int i = 0; i < params.length; i++) {
				String p = params[i];
				p = JDTUtils.getResolvedTypeName(p, declaringType);				
				m += p;
				if (i < params.length - 1) {
					m +=  ",";
				}
			} 

			m += ")>";

			//m+="";
			return m;
		} catch (Exception e) {
		}
    	
    	return null;
    }
    
    public static String getTypeSootSignature(IType type){
    	String name = type.getTypeQualifiedName();
		String pkg = type.getPackageFragment().getElementName();
		String signature = pkg+"."+name;
		return signature;
    }
    
    
    public static String getFieldSignature(IField field){    	
    	try{
    		IType declaringType = field.getDeclaringType();
    		String classname = declaringType.getFullyQualifiedName();						 
    		//classname = JDTUtils.getResolvedTypeName(classname, declaringType);		
    		String type = field.getTypeSignature();
    		type = JDTUtils.getResolvedTypeName(type, declaringType);
    		
    		//if the declaration is a generic type, then unfortunately, the type will
    		//be null, but actually it is of java.lang.Object type
    		if(type==null){
    			type = "java.lang.Object";
    		}
    		
    		String sig = "<"+classname + ": " + type + " " +field.getElementName()+">";
    		return sig;
    	}catch(Exception e){
    		
    	}
    	
		return "";
    }
    
    public static String getLineText(ITextEditor editor,int line,boolean includeLineLabel,boolean keepHeadBlanks){
    	try{
    		IDocumentProvider provider = editor.getDocumentProvider();
        	IDocument doc = provider.getDocument(editor.getEditorInput());
        	IRegion region = doc.getLineInformation(line);
     		int offset = region.getOffset();
     		int length = region.getLength();
     		
     		String text = doc.get(offset,length); 
     		
     		if(keepHeadBlanks)
     			text = text.replaceAll("\t", "  ");
     		else
     			text = text.trim();
     		
     		if(includeLineLabel){
     			String linestr = "" + (line+1);
     			while(linestr.length()<5){
     				linestr += " ";
     			}
     			
     			
     			return linestr+": "+text;
     		}
     		else{
     			return text;
     		}
    	}
    	catch(Exception e){
    		
    	}
		
    	return "";
    }
    
    
    public static String getSelectedText(ITextEditor editor, ITextSelection selection) 
    	throws Exception {
    	
    	IDocumentProvider provider = editor.getDocumentProvider();
    	IDocument doc = provider.getDocument(editor.getEditorInput());

    	return doc.get(selection.getOffset(),selection.getLength());
    }   
    
    
    public static IMethod getEnclosingMethod(ITextEditor editor,int line)
    							throws JavaModelException{
    	ICompilationUnit cunit = getInputAsCompilationUnit(editor);
    	reconcile(cunit);
    	
    	int startOffset = 0;
		int endOffset = 0;
		
		try{
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument doc = provider.getDocument(editor.getEditorInput());
			IRegion region = doc.getLineInformation(line);
			
			startOffset = region.getOffset();
			endOffset = startOffset + region.getLength();
		}
		catch(Exception e){}
    	
		
		for(int i=startOffset;i<endOffset;i++){
			IJavaElement ref = cunit.getElementAt(i);
			if(ref!=null){
				IMethod m = findMethod(ref);
				if(m!=null)
					return m;
			}
		}
		
    	return null; 
    }
    
    public static int getMethodStartLine(ITextEditor editor,IMethod method){
    	try{
    		IDocumentProvider provider = editor.getDocumentProvider();
    		IDocument doc = provider.getDocument(editor.getEditorInput());
    		//ISourceRange range = method.getSourceRange();
    		ISourceRange range = method.getNameRange();
    		if(range!=null){
    			int offset = range.getOffset();
    			return doc.getLineOfOffset(offset);
    		}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return -1;
    }
    
    
    private static IMethod findMethod(IJavaElement elmt){
    	while(elmt!=null){
    		if(elmt instanceof IMethod){
    			return (IMethod)elmt;    			
    		}
    		
    		elmt = elmt.getParent();
    	}
    	
    	return null;
    }   
    
    
    public static ICompilationUnit getCompliationUnit(ITextEditor editor){
    	IEditorInput input = editor.getEditorInput();
		ICompilationUnit icompilationUnit = null;
		if(input instanceof ICompilationUnit){
			icompilationUnit = (ICompilationUnit)input;
		}
		else{
			FileEditorInput fileEditorInput = (FileEditorInput)input;
			IFile ifile = fileEditorInput.getFile();
			icompilationUnit = JavaCore.createCompilationUnitFrom(ifile);
		}
		
		return icompilationUnit;
    }
}

