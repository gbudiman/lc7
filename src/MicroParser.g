grammar MicroParser;
@rulecatch {
	/*catch (RecognitionException re) {
		System.out.println("Not Accepted\n");
		System.exit(1);	
	}*/
}
@header {
	import java.util.Vector;
	import java.util.LinkedList;
	import java.util.Iterator;
	import java.util.Collections;
	import java.util.ArrayList;
}
@members {
	private List<String> errors = new LinkedList<String>();
	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
		String hdr = getErrorHeader(e);
		String msg = getErrorMessage(e, tokenNames);
		errors.add(hdr + " " + msg);
	}
	public List<String> getErrors() {
		return errors;
	}
	public int getErrorCount() {
		return errors.size();
	}

	public IntermediateRepresentation ir = new IntermediateRepresentation();
	public List<msTable> masterTable = new Vector<msTable>();
	public List<mSymbol> symbolTable = new Vector<mSymbol>();
	public List<masterIR> mirList = new Vector<masterIR>();
	public List<String> irTable = new Vector<String>();
	public msTable tms = new msTable("__global");
	public masterIR mir = new masterIR("__global");
	public assembler a = new assembler();
	public Stack<String> labelStack = new Stack<String>();
	public int space = 0;
	public int parameter = 0;

	public String getType(String varName) {
		Iterator mtc = symbolTable.iterator();
		//System.out.println("Searching for " + varName);
		while (mtc.hasNext()) {
			mSymbol ese = (mSymbol) mtc.next();
			if (ese.getName().equals(varName)) {
				return ese.getType();
			}
		}

		Iterator mti = masterTable.iterator();
		//System.out.println("Searching global for " + varName);
		while (mti.hasNext()) {
			msTable cmte = (msTable) mti.next();
			if (cmte.scope.equals("__global")) {
				//System.out.println("** searching global for " + varName);
				Iterator esti = cmte.symbolTable.iterator();
				while (esti.hasNext()) {
					mSymbol ese = (mSymbol) esti.next();
					//System.out.println("*** at " + ese.getName());
					if (ese.getName().equals(varName)) {
						//System.out.println("*** -> returning " + ese.getType());
						return ese.getType();
					}
				}
			}
			break;
		}

		System.out.println("Variable " + varName + " not found");
		return null;
	}


	public List<String> flatten(List<masterIR> mirList) {
		List<String> flatIR = new Vector<String>();
		for (masterIR tmir: mirList) {
			flatIR.add("MAKESPACE " + tmir.Space);
			flatIR.add("MAKEPARAM " + tmir.parameter);
			for (String eir: tmir.ir) {
				flatIR.add(eir);
			}
		}

		return flatIR;
	}

        public String findParameter(String name, List<mSymbol> stab) {
		boolean debug = false;
		if (debug) System.out.print(";>Searching for " + name);
                for (mSymbol d : stab) {
                        if (d.getName().equals(name)) {
				if (d.getID() != null) {
					if (debug) System.out.print(" returns " + d.getID() + "\n");
	                                return d.getID();
				}
				else {
					if (debug) System.out.print(" returns " + name + "\n");
					return name;
				}
                        }
                }
		if (debug) System.out.print(" returns " + name + "\n");
                return name;
        }

}
/* Program */
program 	: 'PROGRAM' id 'BEGIN' {
			//irTable.add(ir.generateLabel("main"));
		} 
		pgm_body 'END'
{
	tms.attachTable(symbolTable);
	mir.attachTable(irTable, space, parameter);
	masterTable.add(tms);
	mirList.add(mir);

	// IR table
	//System.out.println("===================");
	/*Iterator irti = irTable.iterator();
	while (irti.hasNext()) {
		System.out.println(";" + irti.next());
	}*/

	/*for (msTable tableEntry: masterTable) {
		tableEntry.listMember();
	}*/

	// Symbol table
	//System.out.println("===================");
	/*Iterator mti = masterTable.iterator();
	while (mti.hasNext()) {
		msTable cmte = (msTable) mti.next();
		if (cmte.scope.equals("__global")) {
			System.out.println("Printing Global Symbol Table");
		}
		else {
			System.out.println("Printing Symbol Table for " + cmte.scope);
		}
		Iterator esti = cmte.symbolTable.iterator();

		while (esti.hasNext()) {
			mSymbol ese = (mSymbol) esti.next();
			System.out.print("name: " + ese.getName());
			System.out.print(" type " + ese.getType());
			if (ese.getValue() != "") {
				System.out.print(" value: " + ese.getValue());
			}
			System.out.print(" id " + ese.getID());
			System.out.println();
		}
		System.out.println();
	}*/
	// End Symbol Table

	cfg c = new cfg();
	for (masterIR xmir : mirList) {
		//System.out.println("; *** " + xmir.scope);
		for (String k : xmir.ir) {
			//System.out.println("; " + k);
			c.loadInstruction(k);
		}
	}

	c.process();
	c.printOut(1);
	a.init(masterTable);
	//List<String> tinyOutput = a.process(flatten(mirList), false);
	List<String> tinyOutput = a.process(c.getIR(), false);

	//System.out.println("===================");
	/*for (String x: tinyOutput) {
		System.out.println(x);
	}*/


};
id		: IDENTIFIER;
pgm_body	: decl func_declarations;
decl 		: (string_decl | var_decl)*;
/* Global String Declaration */
string_decl	: 'STRING' id ':=' str ';'
{
	symbolTable.add(new mSymbol($id.text, "STRING", $str.text));
	irTable.add(ir.store($str.text, $id.text, getType($id.text)));
};
str		: STRINGLITERAL;
string_decl_tail: string_decl string_decl_tail?;
/* Variable Declaration */
var_decl	: var_type id_list ';' 
{
	while (!$id_list.stringList.empty()) {
		String t = $id_list.stringList.pop();
		symbolTable.add(new mSymbol(t, $var_type.text, ir.generateLocal(), "L"));
		space++;
	}
};
var_type	: 'FLOAT' | 'INT';
any_type	: var_type | 'VOID';
id_list	returns [ Stack<String> stringList ]
		: id id_tail 
{
	$stringList = $id_tail.stringList;
	$stringList.push($id.text);
};
id_tail returns [ Stack<String> stringList ]
	 	: ',' id tailLambda = id_tail 
{
	$stringList = $tailLambda.stringList;
	$stringList.push($id.text);
}
		| 
{
	$stringList = new Stack<String>();
};
var_decl_tail	: var_decl var_decl_tail?;
/* Function Parameter List */
param_decl_list : param_decl param_decl_tail;
param_decl	: var_type id {
		symbolTable.add(new mSymbol($id.text, $var_type.text, ir.generateParameter(), "P"));
		parameter++;
};
param_decl_tail	: ',' param_decl param_decl_tail | ;
/* Function Delcarations */
func_declarations: (func_decl func_decl_tail)?;
func_decl	: 'FUNCTION' any_type id 
{
	
	Iterator fti = symbolTable.iterator();
	while (fti.hasNext()) {
		mSymbol init = (mSymbol) fti.next();
	}
	tms.attachTable(symbolTable);
	mir.attachTable(irTable, space, parameter);
	masterTable.add(tms);
	mirList.add(mir);
	tms = new msTable($id.text);
	mir = new masterIR($id.text);
	symbolTable = new Vector<mSymbol>();
	irTable = new Vector<String>();
	ir.resetParameter();
	ir.resetLocal();
	space = 0;
	parameter = 0;
}
		'(' param_decl_list? ')' 'BEGIN' func_body 'END' {
	irTable.add(0, "LINK");
	irTable.add(0, "LABEL " + $id.text);
	irTable.add(0, ";Generating code for function " + $id.text);
	/*for (String c : $func_body.irs) {
		System.out.println(";" + $id.text + " >> " + c);
		//System.out.println(";" + c);
		irTable.add(c);
	}*/	
};
func_decl_tail	: func_decl*;
func_body returns [List<String> irs]	: decl stmt_list {
		$irs = $stmt_list.irs;
};
/* Statement List */
stmt_list returns [List<String> irs]	
		: stmt lambdaStmt = stmt_tail {
		$lambdaStmt.irs.addAll(0, $stmt.irs);
		$irs = $lambdaStmt.irs;
		} | { $irs = new ArrayList(); };
stmt_tail returns [List<String> irs]	
		: stmt lambdaStmt = stmt_tail {
		$lambdaStmt.irs.addAll(0, $stmt.irs);
		$irs = $lambdaStmt.irs;
		} | { $irs = new ArrayList(); };
stmt returns [List<String> irs]		
		: assign_stmt { $irs = $assign_stmt.irs; }
		| read_stmt { $irs = $read_stmt.irs; }
		| write_stmt { $irs = $write_stmt.irs; }
		| return_stmt { $irs = $return_stmt.irs; }
		| if_stmt { $irs = $if_stmt.irs; }
		| do_stmt { $irs = $do_stmt.irs; };
/* Basic Statement */
assign_stmt returns [List<String> irs]	
		: assign_expr ';' { $irs = $assign_expr.irs; } ;
assign_expr returns [List<String> irs]	
		: id ':=' expr {
	List<String> localIrs = new ArrayList<String>();
	if (getType($id.text).equals("INT") || getType($id.text).equals("FLOAT")) {
		//irTable.add(ir.store($expr.temp, $id.text, getType($id.text)));
		irTable.add(ir.store($expr.temp
				, findParameter($id.text, symbolTable)
				, getType($id.text)));
		//localIrs.add(ir.store($expr.temp, $id.text, getType($id.text)));
	}
	$irs = localIrs;
}
;
read_stmt returns [List<String> irs]
		: 'READ' '(' id_list ')' ';' {

	List<String> localIrs = new ArrayList<String>();
	Stack<String> ds = new Stack<String>();
	for (String i : $id_list.stringList) {
		//System.out.println(i + " " + getType(i));
		ds.push(ir.rw(findParameter(i, symbolTable), "READ", getType(i)));
		//localIrs.add(ir.rw(i, "READ", getType(i)));
		//irTable.add(ir.rw(i, "READ", getType(i)));
	}
	while (!ds.empty()) {
		irTable.add(ds.pop());
	}

	$irs = localIrs;
};
write_stmt returns [List<String> irs]
		: 'WRITE' '(' id_list ')' ';' {
	List<String> localIrs = new ArrayList<String>();
	Stack<String> ds = new Stack<String>();
	for (String i : $id_list.stringList) {
		//System.out.println(i + " " + getType(i));
		ds.push(ir.rw(findParameter(i, symbolTable), "WRITE", getType(i)));
		//localIrs.add(ir.rw(i, "WRITE", getType(i)));
		//irTable.add(ir.rw(i, "WRITE", getType(i)));
	}
	while (!ds.empty()) {
		irTable.add(ds.pop());
	}

	$irs = localIrs;
};
return_stmt returns [List<String> irs]	
		: 'RETURN' expr ';' {
	$irs = new ArrayList<String>();
	irTable.add(ir.store(findParameter($expr.temp, symbolTable)
			, ir.generateReturn()
			, getType($expr.temp)));	
	irTable.add("RET");
};
/* Expressions */
expr returns [String temp]
		: factor expr_tail {
		char tempOp;
		String tempVar;
		String left = $factor.temp;
		//System.out.println("xl: " + left);

		//System.out.println(";!> " + $expr.text + " left: " + left);
		while(!$expr_tail.ops.isEmpty()) {
			String result = ir.generate();
			tempOp = $expr_tail.ops.removeFirst();
			tempVar = $expr_tail.temp.removeFirst();
			//System.out.println("left: " + left + " tail: " + tempOp + ", " + tempVar);
			
			//irTable.add(ir.arithmetic(left, tempVar, result, tempOp, getType(tempVar)));
			irTable.add(ir.arithmetic(findParameter(left, symbolTable)
						, findParameter(tempVar, symbolTable)
						, result
						, tempOp
						, getType(tempVar)));
			symbolTable.add(new mSymbol(result, getType(tempVar)));
			left = result;
		}
		//System.out.println("returning: " + left);
		$temp = left;
		};
expr_tail returns [LinkedList<Character> ops, LinkedList<String> temp]
		: addop factor lambda=expr_tail {
			$ops = $lambda.ops;
			$temp = $lambda.temp;
			$ops.addLast($addop.op);
			$temp.addLast($factor.temp);
		}
		| {
			$ops = new LinkedList();
			$temp = new LinkedList();
		};
factor returns [String temp]
		: postfix_expr factor_tail {
		char tempOp;
		String tempVar;
		String left = $postfix_expr.temp;

		while (!$factor_tail.ops.isEmpty()) {
			String result = ir.generate();
			tempOp = $factor_tail.ops.removeFirst();
			tempVar = $factor_tail.temp.removeFirst();
		
			irTable.add(ir.arithmetic(findParameter(left, symbolTable)
						, findParameter(tempVar, symbolTable)
						, result, tempOp, getType(tempVar)));
			symbolTable.add(new mSymbol(result, getType(tempVar)));
			left = result;
		}
		$temp = left;
		};
factor_tail returns [LinkedList<Character> ops, LinkedList<String> temp]
		: mulop postfix_expr lambda=factor_tail {
			$ops = $lambda.ops;
			$temp = $lambda.temp;
			$ops.addLast($mulop.op);
			$temp.addLast($postfix_expr.temp);
		}
		| {
			$ops = new LinkedList();
			$temp = new LinkedList();
		};
postfix_expr returns [String temp]
		: primary {
			$temp = $primary.temp;
		} | call_expr {
		$temp = $call_expr.temp;
		//System.out.println(";cc> " + $call_expr.text);
		symbolTable.add(new mSymbol($temp, "INT"));
};
call_expr returns [String temp]
		: id '(' expr_list? ')' {

		String tVar;
		int i;
		int exprSize = $expr_list.irs.size();
		irTable.add(ir.push());

		for (i = exprSize; i > 0; i--) {
			tVar = $expr_list.irs.get(i - 1);
			irTable.add(ir.push(findParameter(tVar, symbolTable)));
		}
		irTable.add(ir.jsr($id.text));
		for (i = 0; i < exprSize; i++) {
			irTable.add(ir.pop());
		}
		irTable.add(ir.pop(($temp = ir.generate())));
};
expr_list returns [List<String> irs]
		: expr eLambda = expr_list_tail {
		$eLambda.irs.add($expr.temp);
		$irs = $eLambda.irs;
};
expr_list_tail returns [List<String> irs]
		: ',' expr eLambda = expr_list_tail {
		$eLambda.irs.add($expr.temp);
		$irs = $eLambda.irs;
}
		| {
		$irs = new ArrayList();
};
primary returns [String temp]
		: '(' expr ')' {
			$temp = $expr.temp;
		}
		| id {
			$temp = $id.text;
		}
		| INTLITERAL {
			$temp = ir.generate();
			symbolTable.add(new mSymbol($temp, "INT"));
			irTable.add(ir.store($INTLITERAL.text, $temp, "INT"));
		}
		| FLOATLITERAL {
			$temp = ir.generate();
			symbolTable.add(new mSymbol($temp, "FLOAT"));
			irTable.add(ir.store($FLOATLITERAL.text, $temp, "FLOAT"));
		};
addop returns [char op]
		: '+' {$op = '+';} | '-' {$op = '-';};
mulop returns [char op]
		: '*' {$op = '*';} | '/' {$op = '/';};
/* Comples Statemens and Condition */
if_stmt returns [List<String> irs] 	
		: 'IF' '(' cond ')' 'THEN' stmt_list else_part 'ENDIF' {
		irTable.add(ir.label(labelStack.pop()));
		$irs = new ArrayList<String>(); 
};
else_part	: 'ELSE' {
			String nextLabel = labelStack.pop();
			irTable.add(ir.jump(labelStack.peek())); 
			irTable.add(ir.label(nextLabel));
		} stmt_list | { irTable.add(ir.label(labelStack.pop())); };
cond 		: l1=expr compop l2=expr {
			labelStack.push(ir.generateLabel());
			String nLabel = ir.generateLabel();
			labelStack.push(nLabel);
			irTable.add(ir.comparison(findParameter($l1.temp, symbolTable)
						, findParameter($l2.temp, symbolTable)
						, $compop.text, nLabel, getType($l1.temp)));
		};
compop		: '<' | '>' | '=' | '!=';
do_stmt	returns [List<String> irs]	
		: 'DO' {
			String lLabel = ir.generateLabel();
			labelStack.push(lLabel);
			irTable.add(ir.label(lLabel));
		}
	 	stmt_list 'WHILE' '(' cond ')' ';' {
			String loopBack = labelStack.pop();
			String unused = labelStack.pop();
			String loopExit = labelStack.pop();
			irTable.add(ir.jump(loopExit));
			irTable.add(ir.label(loopBack));
			$irs = new ArrayList<String>();
		};

fragment DIGIT          : '0'..'9';
fragment LETTER         : 'A'..'Z'|'a'..'z';
fragment ALPHANUMERIC   : '0'..'9'|'A'..'Z'|'a'..'z';

KEYWORD         : 'PROGRAM' 
                | 'BEGIN' 
                | 'END' 
                | 'PROTO' 
                | 'FUNCTION' 
                | 'READ' 
                | 'WRITE' 
                | 'IF' 
                | 'THEN'
                | 'ELSE'
                | 'ENDIF'
                | 'RETURN'
		| 'CASE'
		| 'ENDCASE'
		| 'BREAK'
		| 'DEFAULT'
		| 'DO' 
		| 'WHILE' 
                | 'FLOAT'
                | 'INT' 
                | 'VOID'
                | 'STRING';
/*OPERATOR        : ':='
                | '+'
                | '-'
                | '*'
                | '/'
                | '='
		| '!='
                | '<'
                | '>'
                | '('
                | ')'
                | ';'
                | ',';*/
INTLITERAL      : (DIGIT)+;
FLOATLITERAL    : (DIGIT)*('.'(DIGIT)+);
STRINGLITERAL   : ('"'(~('\r'|'\n'|'"'))*'"');
WHITESPACE      : ('\n'|'\r'|'\t'|' ')+
                {skip();};
COMMENT         : '--'
                (~('\n'|'\r'))*
                ('\n'|'\r'('\n')?)?
                {skip();};
IDENTIFIER      : (LETTER)(ALPHANUMERIC)*;
