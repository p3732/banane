import CPP.Absyn.*;

public class Interpreter {

    public void interpret(Program p) {

    }

    private class FunctionInterpreter implements Def.Visitor<Void, Env> {
        public Void visit(DFun d, Env env) {
            DFun df=(DFun)d;
            for(Stm stm:df.liststm_) {
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
        	
            return null;
        }

        public Void visit(SInit df, Env env) {
        	Object varValue = df.exp_.accept(new ExpEval(), env);
        	env.updateVar(df.id_, varValue);
            return null;
        }

        public Void visit(SReturn df, Env env) {
        	Object returnValue = df.exp_.accept(new ExpEval(), env);
        	// TODO: Put the returnValue somewhere
        	env.exitBlock();
            return null;
        }

        public Void visit(SWhile df, Env env) {
        	while ((boolean) df.exp_.accept(new ExpEval(), env)) {
        		df.stm_.accept(new StmEval(), env);
        	}
            return null;
        }

        public Void visit(SBlock df, Env env) {
        	for(Stm stm : df.liststm_) {
                stm.accept(new StmEval(), env);
                if(stm instanceof SReturn) {
                    break;
                }
            }
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
        public Object visit(EApp e, Env env) { return null; }

        //++ -- (implicit variable via parser)
        public Object visit(EPostIncr e, Env env) { return null; }
        public Object visit(EPostDecr e, Env env) { return null; }
        public Object visit(EPreIncr e, Env env) { return null; }
        public Object visit(EPreDecr e, Env env) { return null; }

        // * / + - assignment(implicit variable via parser)
        public Object visit(ETimes e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) * ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) * ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) * ((Integer)v2);
        	return ((Double)v1) * ((Double)v2);
        }
        public Object visit(EDiv e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) / ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) / ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) / ((Integer)v2);
        	return ((Double)v1) / ((Double)v2);
        }
        public Object visit(EPlus e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) + ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) + ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) + ((Integer)v2);
        	return ((Double)v1) + ((Double)v2);
        }
        public Object visit(EMinus e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) - ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) - ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) - ((Integer)v2);
        	return ((Double)v1) - ((Double)v2);
        }
        public Object visit(EAss e, Env env) { return null; }

        // < > >= ... && ||
        public Boolean visit(ELt e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) < ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) < ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) < ((Integer)v2);
        	return ((Double)v1) < ((Double)v2);
        }
        public Boolean visit(EGt e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) > ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) > ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) > ((Integer)v2);
        	return ((Double)v1) > ((Double)v2);
        }
        public Boolean visit(ELtEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) <= ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) <= ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) <= ((Integer)v2);
        	return ((Double)v1) <= ((Double)v2);
        }
        public Boolean visit(EGtEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) >= ((Integer)v2);
        	else if ((v1 instanceof Integer) && (v2 instanceof Double)) return ((Integer)v1) >= ((Double)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Integer)) return ((Double)v1) >= ((Integer)v2);
        	return ((Double)v1) >= ((Double)v2);
        }
        public Boolean visit(EEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) == ((Integer)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Double)) return ((Double)v1) == ((Double)v2);
        	else if ((v1 instanceof Boolean) && (v2 instanceof Boolean)) return ((Boolean)v1) == ((Boolean)v2);
        	// TODO: Which other types may be compared for equality
        	else throw new RuntimeException("You cannot compare two objects of different types for equality.");
        }
        public Boolean visit(ENEq e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	
        	if ((v1 instanceof Integer) && (v2 instanceof Integer)) return ((Integer)v1) != ((Integer)v2);
        	else if ((v1 instanceof Double) && (v2 instanceof Double)) return ((Double)v1) != ((Double)v2);
           	else if ((v1 instanceof Boolean) && (v2 instanceof Boolean)) return ((Boolean)v1) != ((Boolean)v2);
        	// TODO: Which other types may be compared for inequality
        	else throw new RuntimeException("You cannot compare two objects of different types for inequality.");
        }
        public Boolean visit(EAnd e, Env env) { 
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	if (v1 instanceof Integer) {
        		if (((Integer)v1) == 1 ) {
        			if (v2 instanceof Integer) {
        				if (((Integer)v2) == 1 ) {
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
        			return (((Integer)v2) == 1 ? true : false);
        		}
        	}
        	else throw new RuntimeException("You cannot compare two objects of different types for inequality.");
        }
        public Boolean visit(EOr e, Env env) {
        	Object v1 = e.exp_1.accept(new ExpEval(), env); 
        	Object v2 = e.exp_2.accept(new ExpEval(), env);
        	if (v1 instanceof Integer) {
        		if (((Integer)v1) == 1 ) {
        			return true;
        			
        		} else {
        			if (v2 instanceof Integer) {
        				if (((Integer)v2) == 1 ) {
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
        			return (((Integer)v2) == 1 ? true : false);
        		}
        	}
        	else throw new RuntimeException("You cannot compare two objects of different types for inequality.");
        }
    }
}

