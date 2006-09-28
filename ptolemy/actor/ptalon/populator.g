header {/* 

 Copyright (c) 2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.ptalon;
}

{
	import java.util.StringTokenizer;
}
class PtalonPopulator extends TreeParser;
options {
	importVocab = Ptalon;
	buildAST = true;
	defaultErrorHandler = false;
	ASTLabelType = "PtalonAST";
}

{
	private NestedActorManager info;

	public NestedActorManager getCodeManager() {
		return info;
	}
	
	private String scopeName;
	
	private boolean evalBool = false;
	
}

port_declaration throws PtalonRuntimeException
:
	#(PORT (a:ID
	{
		if (info.isReady() && !info.isCreated(a.getText())) {
			info.addPort(a.getText());
		}
	}
	| #(DYNAMIC_NAME g:ID h:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(h.getText());
			if (value != null) {
				String name = g.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "port");
				}
    			if (!info.isCreated(name)) {
    				info.addPort(name);
    			}
    		}
		}
	}
	)) | #(INPORT (b:ID
	{
		if (info.isReady() && !info.isCreated(b.getText())) {
			info.addInPort(b.getText());
		}
	}
	| #(DYNAMIC_NAME i:ID j:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(j.getText());
			if (value != null) {
				String name = i.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "inport");
				}
    			if (!info.isCreated(name)) {
    				info.addInPort(name);
    			}
    		}
		}
	}
	)) | #(OUTPORT (c:ID
	{
		if (info.isReady() && !info.isCreated(c.getText())) {
			info.addOutPort(c.getText());
		}
	}
	| #(DYNAMIC_NAME k:ID l:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(l.getText());
			if (value != null) {
				String name = k.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "outport");
				}
    			if (!info.isCreated(name)) {
    				info.addOutPort(name);
    			}
    		}
		}
	}
	)) | #(MULTIPORT (d:ID
	{
		if (info.isReady() && !info.isCreated(d.getText())) {
			info.addPort(d.getText());
		}
	}
	| #(DYNAMIC_NAME m:ID n:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(n.getText());
			if (value != null) {
				String name = m.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "multiport");
				}
    			if (!info.isCreated(name)) {
    				info.addPort(name);
    			}
    		}
		}
	}
	)) | #(MULTIINPORT (e:ID
	{
		if (info.isReady() && !info.isCreated(e.getText())) {
			info.addInPort(e.getText());
		}
	}
	| #(DYNAMIC_NAME o:ID p:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(p.getText());
			if (value != null) {
				String name = o.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "multiinport");
				}
    			if (!info.isCreated(name)) {
    				info.addInPort(name);
    			}
    		}
		}
	}
	)) | #(MULTIOUTPORT (f:ID
	{
		if (info.isReady() && !info.isCreated(f.getText())) {
			info.addOutPort(f.getText());
		}
	}
	| #(DYNAMIC_NAME q:ID r:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(r.getText());
			if (value != null) {
				String name = q.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "multioutport");
				}
    			if (!info.isCreated(name)) {
    				info.addOutPort(name);
    			}
    		}
		}
	}
	))
	exception
	catch [PtalonScopeException excep]
	{
		throw new PtalonRuntimeException("", excep);
	}
;

parameter_declaration throws PtalonRuntimeException
:
	#(PARAMETER (a:ID
	{
		if (info.isReady() && !info.isCreated(a.getText())) {
			info.addParameter(a.getText());
		}
	}
	| #(DYNAMIC_NAME c:ID d:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(d.getText());
			if (value != null) {
				String name = c.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "parameter");
				}
    			if (!info.isCreated(name)) {
    				info.addParameter(name);
    			}
    		}
		}
	}
	)) | #(ACTOR b:ID
	{
		if (info.isReady() && !info.isCreated(b.getText())) {
			info.addActorParameter(b.getText());
		}
	}
	)
	exception
	catch [PtalonScopeException excep]
	{
		throw new PtalonRuntimeException("", excep);
	}
	
;

assigned_parameter_declaration throws PtalonRuntimeException
{
	boolean dynamic_name = false;
}
:
	#(PARAM_EQUALS #(PARAMETER (a:ID | #(DYNAMIC_NAME c:ID d:EXPRESSION)
	{
		dynamic_name = true;
	}
	)) e:EXPRESSION
	{
		if (dynamic_name) {
    		if (info.isReady()) {
    			String value = info.evaluateString(d.getText());
    			if (value != null) {
    				String name = c.getText() + value;
    				if (!info.inScope(name)) {
    					info.addSymbol(name, "parameter");
    				}
        			if (!info.isCreated(name)) {
        				info.addParameter(name, e.getText());
        			}
        		}
    		}
		} else {
    		if (info.isReady() && !info.isCreated(a.getText())) {
    			info.addParameter(a.getText(), e.getText());
    		}
		}
	}
	) | #(ACTOR_EQUALS #(ACTOR b:ID) q:qualified_identifier
	{
		if (info.isReady() && !info.isCreated(b.getText())) {
			info.addActorParameter(b.getText(), q.getText());
		}
	}
	)
	exception
	catch [PtalonScopeException excep]
	{
		throw new PtalonRuntimeException("", excep);
	}
;


relation_declaration throws PtalonRuntimeException
:
	#(RELATION (a:ID
	{
		if (info.isReady() && !info.isCreated(a.getText())) {
			info.addRelation(a.getText());
		}
	}	
	| #(DYNAMIC_NAME c:ID d:EXPRESSION)
	{
		if (info.isReady()) {
			String value = info.evaluateString(d.getText());
			if (value != null) {
				String name = c.getText() + value;
				if (!info.inScope(name)) {
					info.addSymbol(name, "relation");
				}
    			if (!info.isCreated(name)) {
    				info.addRelation(name);
    			}
    		}
		}
	}
	))
	exception
	catch [PtalonScopeException excep]
	{
		throw new PtalonRuntimeException("", excep);
	}
;

qualified_identifier
:
	QUALID
;

assignment throws PtalonRuntimeException
:
	#(ASSIGN l:ID (ID | #(d:DYNAMIC_NAME i:ID e:EXPRESSION)
	| nested_actor_declaration | EXPRESSION))
;

/**
 * This is for a top level actor declaration, which 
 * requires seperate treatement from a nested actor
 * declaration.
 */
actor_declaration throws PtalonRuntimeException
{
	boolean oldEvalBool = false;
}
:
	#(a:ACTOR_DECLARATION 
	{
		info.enterActorDeclaration(a.getText());
		if (info.isActorReady()) {
			oldEvalBool = evalBool;
			evalBool = true;
		}
	}
	(b:assignment)*
	{
		if (info.isActorReady()) {
			evalBool = oldEvalBool;
			info.addActor(a.getText());
		}
		info.exitActorDeclaration();
	}
	)
;

/**
 * In this case we do not add any actors, but rather
 * defer this decision to any generated actors.
 */
nested_actor_declaration throws PtalonRuntimeException
:
	#(a:ACTOR_DECLARATION 
	{
		info.enterActorDeclaration(a.getText());
	}
	(b:assignment)*
	{
		info.exitActorDeclaration();
	}
	)
;

atomic_statement throws PtalonRuntimeException
:
	(port_declaration | parameter_declaration |
	 assigned_parameter_declaration | 
		relation_declaration | actor_declaration)
;

conditional_statement throws PtalonRuntimeException
{
	boolean ready;
}
:
	#(a:IF 
	{
		info.enterIfScope(a.getText());
		ready = info.isIfReady();
		if (ready) {
			evalBool = true;
		}
	}
	e:EXPRESSION 
	{
		if (ready) {
			info.setActiveBranch(info.evaluateBoolean(e.getText()));
			evalBool = false;
		}
	}
	#(TRUEBRANCH 
	{
		if (ready) {
			info.setCurrentBranch(true);
		}
	}
	(atomic_statement | conditional_statement | iterative_statement)*) #(FALSEBRANCH
	{
		if (ready) {
			info.setCurrentBranch(false);
		}
	}
	(atomic_statement | conditional_statement | iterative_statement)*))
	{
		info.exitIfScope();
	}
;

iterative_statement throws PtalonRuntimeException
{
	boolean ready;
	PtalonAST inputAST = (PtalonAST)_t;
}
:
	#(f:FOR #(VARIABLE a:ID) #(INITIALLY b:EXPRESSION) #(SATISFIES c:EXPRESSION)
	{
		info.enterForScope(f.getText(), inputAST, this);
		ready = info.isForReady();
		if (ready) {
			info.setActiveBranch(true);
			info.setCurrentBranch(false);
		}
	}
		(atomic_statement | conditional_statement | iterative_statement)*
		#(NEXT n:EXPRESSION))
	{
		if (ready) {
			info.evaluateForScope();
		}
		info.exitForScope();
	}
;

iterative_statement_evaluator throws PtalonRuntimeException
:
	#(f:FOR #(VARIABLE a:ID) #(INITIALLY b:EXPRESSION) #(SATISFIES c:EXPRESSION)
		(atomic_statement | conditional_statement | iterative_statement)*
		#(NEXT n:EXPRESSION))
;


actor_definition[NestedActorManager info] throws PtalonRuntimeException
{
	this.info = info;
	this.info.startAtTop();
}
:
	#(a:ACTOR_DEFINITION 
		(atomic_statement | conditional_statement | iterative_statement)*)
;
