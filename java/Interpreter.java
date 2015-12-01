import CPP.Absyn.*;

public class Interpreter {

    public void interpret(Program p) {

    }

    private class FunctionInterpreter implements Def.Visitor<Void, Env> {
        Void visit(Def d, Env env) {
            DFun df=(DFun)d;
            for(Stm stm:df.liststm_) {
                stm.accept(new StmEval(), env);
                if(stm.equals(new SReturn()) {
                    break;
                }
            }

            return null;
        }
    }

    private class StmEval implements Stm.Visitor<Void, Env> {
        Void visit(SExp df, Env env) {

            return null;
        }

        Void visit(SDecls df, Env env) {

            return null;
        }

        Void visit(SInit df, Env env) {

            return null;
        }

        Void visit(SReturn df, Env env) {

            return null;
        }

        Void visit(SWhile df, Env env) {

            return null;
        }

        Void visit(SBlock df, Env env) {

            return null;
        }


        Void visit(SIfElse df, Env env) {

            return null;
        }
    }

    private class ExpEval implements Exp.Visitor<Object, Env> {
        // basic
        public Boolean visit(ETrue e, Env env) { return null; }
        public Boolean visit(EFalse e, Env env) { return null; }
        public Integer visit(EInt e, Env env) { return null; }
        public Double visit(EDouble e, Env env) { return null; }

        // var, function
        public Object visit(EId e, Env env) { return null; }
        public Object visit(EApp e, Env env) { return null; }

        //++ -- (implicit variable via parser)
        public Object visit(EPostIncr e, Env env) { return null; }
        public Object visit(EPostDecr e, Env env) { return null; }
        public Object visit(EPreIncr e, Env env) { return null; }
        public Object visit(EPreDecr e, Env env) { return null; }

        // * / + - assignment(implicit variable via parser)
        public Object visit(ETimes e, Env env) { return null; }
        public Object visit(EDiv e, Env env) { return null; }
        public Object visit(EPlus e, Env env) { return null; }
        public Object visit(EMinus e, Env env) { return null; }
        public Object visit(EAss e, Env env) { return null; }

        // < > >= ... && ||
        public Object visit(ELt e, Env env) { return null; }
        public Object visit(EGt e, Env env) { return null; }
        public Object visit(ELtEq e, Env env) { return null; }
        public Object visit(EGtEq e, Env env) { return null; }
        public Object visit(EEq e, Env env) { return null; }
        public Object visit(ENEq e, Env env) { return null; }
        public Object visit(EAnd e, Env env) { return null; }
        public Object visit(EOr e, Env env) { return null; }
    }
}
