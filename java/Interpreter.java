import java.util.LinkedList;

import CPP.Absyn.*;

public class Interpreter {

    public void interpret(Program p) {
//	System.out.println("Interpreting started....");
    	PDefs defs = (PDefs)p;
        Env env = Env.empty();

        // Iterate over all function declarations
        for(Def f: defs.listdef_) {
            DFun df = (DFun)f;
//            System.out.println(df.id_ + " seen");
            LinkedList<String> params = new LinkedList<String>();

            for(Arg arg: df.listarg_) {
                ADecl decl = (ADecl)arg;
                params.add(decl.id_);
            }
	    	Function func = new Function(params, df.liststm_, df);
            env.updateFunction(df.id_, func);
        }
//        System.out.println("Start interpreting main()");
        // interpret function main()
        interpretMain(env);
    }
    
    private void interpretMain(Env env) {
    	FunctionInterpreter fi = new FunctionInterpreter();
    	Env mainEnv = env.newFunction("main");
    	Function f = mainEnv.lookupFunction("main");
    	fi.visit(f.cFunDecl, mainEnv);
    }

    private class FunctionInterpreter implements Def.Visitor<Void, Env> {
        public Void visit(DFun df, Env env) {
            for(Stm stm : df.liststm_) {
                stm.accept(new StmEval(), env);
                if(stm instanceof SReturn) {
                    break;
                }
            }
            return null;
        }
    }

    private class StmEval implements Stm.Visitor<Void, Env> {
        public Void visit(SExp expr, Env env) {
        	expr.exp_.accept(new ExpEval(), env);
            return null;
        }

        public Void visit(SDecls df, Env env) {
        	for (String id : df.listid_) {
        		env.declareVar(id, null);
        	}
            return null;
        }

        public Void visit(SInit df, Env env) {
        	Object varValue = df.exp_.accept(new ExpEval(), env);
        	env.declareVar(df.id_, varValue);
            return null;
        }

        public Void visit(SReturn df, Env env) {
        	Object returnValue = df.exp_.accept(new ExpEval(), env);
        	env.declareVar("return", returnValue);
            return null;
        }

        public Void visit(SWhile df, Env env) {
        	while ((boolean) df.exp_.accept(new ExpEval(), env)) {
        		df.stm_.accept(new StmEval(), env);
        	}
            return null;
        }

        public Void visit(SBlock df, Env env) {
        	env.newBlock();
        	for(Stm stm : df.liststm_) {
                stm.accept(new StmEval(), env);
                if(stm instanceof SReturn) {
                    break;
                }
            }
        	env.exitBlock();
            return null;
        }


        public Void visit(SIfElse df, Env env) {
        	if ((boolean) df.exp_.accept(new ExpEval(), env)) {
        		df.stm_1.accept(new StmEval(), env);
        	} else {
        		df.stm_2.accept(new StmEval(), env);
        	}
            return null;
        }
    }

    private class ExpEval implements Exp.Visitor<Object, Env> {
        // basic
        public Boolean visit(ETrue e, Env env) { return true; }
        public Boolean visit(EFalse e, Env env) { return false; }
        public Integer visit(EInt e, Env env) { return e.integer_; }
        public Double visit(EDouble e, Env env) { return e.double_; }

        // var, function
        public Object visit(EId e, Env env) { return env.lookupVar(e.id_); }
        
        public Object visit(EApp e, Env env) { 
        	return evaluateFunction(e, env);
        }

        //++ -- (implicit variable via parser)
        public Object visit(EPostIncr e, Env env) { 
        	if (!(e.exp_ instanceof EId)) {
        		throw new RuntimeException("LHS of post-increment is not an EId, it is " + e.exp_.getClass());
        	}
        	String var = ((EId) e.exp_).id_;
        	Object oldValue = env.lookupVar(var);
        	if (oldValue instanceof Integer) {
        		env.updateVar(var, ((Integer) oldValue).intValue() + 1);
        	} else if (oldValue instanceof Double) {
        		env.updateVar(var, ((Double) oldValue).doubleValue() + 1.0);
        	} else {
        		throw new RuntimeException("oldValue is neither Integer nor Double, it's " + oldValue.getClass());
        	}
        	
        	return oldValue; 
        }
        public Object visit(EPostDecr e, Env env) {
        	if (!(e.exp_ instanceof EId)) {
        		throw new RuntimeException("LHS of post-increment is not an EId, it is " + e.exp_.getClass());
        	}
        	String var = ((EId) e.exp_).id_;
        	Object oldValue = env.lookupVar(var);
        	if (oldValue instanceof Integer) {
        		env.updateVar(var, ((Integer) oldValue).intValue() - 1);
        	} else if (oldValue instanceof Double) {
        		env.updateVar(var, ((Double) oldValue).doubleValue() - 1.0);
        	} else {
        		throw new RuntimeException("oldValue is neither Integer nor Double, it's " + oldValue.getClass());
        	}
        	
        	return oldValue; 
        }
        public Object visit(EPreIncr e, Env env) { 
        	if (!(e.exp_ instanceof EId)) {
        		throw new RuntimeException("LHS of post-increment is not an EId, it is " + e.exp_.getClass());
        	}
        	String var = ((EId) e.exp_).id_;
        	Object oldValue = env.lookupVar(var);
        	if (oldValue instanceof Integer) {
        		Integer newValue = ((Integer) oldValue).intValue() + 1;
        		env.updateVar(var, newValue);
        		return newValue;
        	} else if (oldValue instanceof Double) {
        		Double newValue = ((Double) oldValue).doubleValue() + 1.0;
        		env.updateVar(var, newValue);
        		return newValue;
        	} else {
        		throw new RuntimeException("oldValue is neither Integer nor Double, it's " + oldValue.getClass());
        	}
        }
        public Object visit(EPreDecr e, Env env) { 
        	if (!(e.exp_ instanceof EId)) {
        		throw new RuntimeException("LHS of post-increment is not an EId, it is " + e.exp_.getClass());
        	}
        	String var = ((EId) e.exp_).id_;
        	Object oldValue = env.lookupVar(var);
        	if (oldValue instanceof Integer) {
        		Integer newValue = ((Integer) oldValue).intValue() - 1;
        		env.updateVar(var, newValue);
        		return newValue;
        	} else if (oldValue instanceof Double) {
        		Double newValue = ((Double) oldValue).doubleValue() - 1.0;
        		env.updateVar(var, newValue);
        		return newValue;
        	} else {
        		throw new RuntimeException("oldValue is neither Integer nor Double, it's " + oldValue.getClass());
        	}
        }

        // * / + - assignment(implicit variable via parser)
        public Object visit(ETimes e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) * ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) * ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) * ((int)v2);
        	return ((double)v1) * ((double)v2);
        }
        public Object visit(EDiv e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) / ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) / ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) / ((int)v2);
        	return ((double)v1) / ((double)v2);
        }
        public Object visit(EPlus e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) + ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) + ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) + ((int)v2);
        	return ((double)v1) + ((double)v2);
        }
        public Object visit(EMinus e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) - ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) - ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) - ((int)v2);
        	return ((double)v1) - ((double)v2);
        }
        public Object visit(EAss e, Env env) {
        	if (!(e.exp_1 instanceof EId)) {
        		throw new RuntimeException("Left-hand side of the assignment is not a variable.");
        	}
        	String id = ((EId) e.exp_1).id_;
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	env.updateVar(id, v2);
        	return v2; 
        }

        // < > >= ... && ||
        public Boolean visit(ELt e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) < ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) < ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) < ((int)v2);
        	return ((double)v1) < ((double)v2);
        }
        public Boolean visit(EGt e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) > ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) > ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) > ((int)v2);
        	return ((double)v1) > ((double)v2);
        }
        public Boolean visit(ELtEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) <= ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) <= ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) <= ((int)v2);
        	return ((double)v1) <= ((double)v2);
        }
        public Boolean visit(EGtEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) >= ((int)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((int)v1) >= ((double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((double)v1) >= ((int)v2);
        	return ((double)v1) >= ((double)v2);
        }
        public Boolean visit(EEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) { return ((int)v1) == ((int)v2); }
        	else if ((v1 instanceof Double) && (v2 instanceof Double)) return ((double)v1) == ((double)v2);
        	else if ((v1 instanceof Boolean) && (v2 instanceof Boolean)) return ((boolean)v1) == ((boolean)v2);
        	// TODO: Which other types may be compared for equality
        	else throw new RuntimeException("You cannot compare two objects of different types for equality.");
        }
        public Boolean visit(ENEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((int)v1) != ((int)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Double)) return ((double)v1) != ((double)v2);
           	else if ((v1 instanceof Boolean) && (v2 instanceof Boolean)) return ((boolean)v1) != ((boolean)v2);
        	// TODO: Which other types may be compared for inequality
        	else throw new RuntimeException("You cannot compare two objects of different types for inequality.");
        }
        public Boolean visit(EAnd e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	if (v1 instanceof Integer) {
        		if (((int)v1) == 1 ) {
        			if (v2 instanceof Integer) {
        				if (((int)v2) == 1 ) {
        					return true;
        				} else {
        					return false;
        				}
        			} else {
        				return (Boolean) v2;
        			}
        		} else {
        			return false;
        		}
        	}
        	if (v1 instanceof Boolean) {
        		if (!((Boolean) v1)) return false;
        		if (v2 instanceof Boolean) return (Boolean) v2;
        		else {
        			return (((int)v2) == 1 ? true : false);
        		}
        	}
        	else throw new RuntimeException("You cannot compare two objects of different types for inequality.");
        }
        public Boolean visit(EOr e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	if (v1 instanceof Integer) {
        		if (((int)v1) == 1 ) {
        			return true;
        			
        		} else {
        			if (v2 instanceof Integer) {
        				if (((int)v2) == 1 ) {
        					return true;
        				} else {
        					return false;
        				}
        			} else {
        				return (Boolean) v2;
        			}
        		}
        	}
        	if (v1 instanceof Boolean) {
        		if (((Boolean) v1)) return true;
        		if (v2 instanceof Boolean) return (Boolean) v2;
        		else {
        			return (((int)v2) == 1 ? true : false);
        		}
        	}
        	else throw new RuntimeException("You cannot compare two objects of different types for inequality.");
        }
        
        private Object evaluateFunction(EApp exp, Env env) {
        	LinkedList<Object> argEvaluation = new LinkedList<Object>();
        	// Evaluate the arguments
        	for (Exp e : exp.listexp_) {
        		argEvaluation.addLast(e.accept(new ExpEval(), env));
        	}
        	
        	switch (exp.id_) {
        	case "printInt": {
        		assert(argEvaluation.size() == 1) : "printInt got too many arguments.";
        		Integer i = (Integer) argEvaluation.get(0);
        		System.out.println(i);
        		return null;
        	}
        	case "printDouble": {
        		assert(argEvaluation.size() == 1) : "printDouble got too many arguments.";
        		Double d = (Double) argEvaluation.get(0);
        		System.out.println(d);
        		return null;
        	}
        	
        	case "readInt": {
        		// System.in
        		// TODO:
        	}
        	case "readDouble": {
        		// System.in
        		// TODO:
        	}
        	}
        	Function func = env.lookupFunction(exp.id_);
        	// Put new environment
        	// Create mapping from parameter to its appropriate value (in the new environment)
        	Env funcEnv = env.newFunction(func.cFunDecl.id_);
        	assert(func.cParameters.size() == argEvaluation.size());
        	for (int i = 0; i < func.cParameters.size(); i++) {
        		funcEnv.declareVar(func.cParameters.get(i), argEvaluation.get(i));
        	}
        	// Execute the stmts of the function
        	FunctionInterpreter fi = new FunctionInterpreter();
        	fi.visit(func.cFunDecl, funcEnv);
        	// Return the returned value of the function
        	Object returnValue = null;
        	try {
        		returnValue = funcEnv.lookupVar("return");
        	} catch (RuntimeException re) {
        		// If the function has no return value, return "null".
        	}
        	return returnValue;
        }
        	
    }
}

