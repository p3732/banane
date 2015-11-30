import java.util.HashMap;
import java.util.LinkedList;

import CPP.PrettyPrinter;
import CPP.Absyn.*;

public class TypeChecker {

    private enum TypeCode {
        INT, DOUBLE, BOOL, VOID
    }

    private static class FunType {
        public LinkedList<Type> intyps;
        public Type outtyp;

        public FunType(LinkedList<Type> intyps, Type outtyp) {
            this.intyps = intyps;
            this.outtyp = outtyp;
        };
    }

    private static class Env {
        public HashMap<String, FunType> signature = new HashMap<String, FunType>();
        public LinkedList<HashMap<String,Type>> contexts = new LinkedList<HashMap<String,Type>>();

        public static Env empty() {
            return new Env();
        }

        public FunType lookupFun(String id) {
            if (signature.containsKey(id))
                return signature.get(id);
            else
                throw new TypeException("Function "+ id +" not defined");
        }

        public Env updateVar(String id, Type typ) {
            if (contexts.getLast().containsKey(id)) {
                throw new TypeException("Var "+ id +" already declared");
            } else {
                contexts.getLast().put(id, typ);
                return this;
            }
        }

        public Env updateFun(String id, FunType funtyp) {
            if (signature.containsKey(id)) {
                throw new TypeException("Function "+ id +" already declared");
            } else {
                signature.put(id, funtyp);
                return this;
            }
        }

        public Env newBlock() {
            contexts.add(new HashMap<String,Type>());
            return this;
        }

        public Env exitBlock() {
            contexts.pollLast();
            return this;
        }
    }

    public void typecheck(Program p) {
        PDefs defs = (PDefs)p;
        Env env = Env.empty();
        LinkedList<Type> args = new LinkedList<Type>();

        // predefined functions
        env.updateFun("readInt", new FunType(args, new Type_int()));
        args.add(new Type_int());
        env.updateFun("printInt", new FunType(args, new Type_void()));
        args.clear();
        env.updateFun("readDouble", new FunType(args, new Type_double()));
        args.add(new Type_double());
        env.updateFun("printDouble", new FunType(args, new Type_void()));
        args.clear();

        for(Def f: defs.listdef_) {
            DFun df = (DFun)f;

            env.newBlock();

            env.updateVar("return", df.type_);

            for(Arg arg: df.listarg_) {
                ADecl decl = (ADecl)arg;
                env.updateVar(decl.id_,decl.type_);
            }

            typecheckStms(env, df.liststm_);

            env.exitBlock();
        }
    }

    public void typecheckStms(Env env, ListStm stms) {
        //TODO return stms
        for(Stm stm:stms) {
            typecheckStm(env, stm);
        }
    }

    public void typecheckStm(Env env, Stm stm) {
        stm.accept(new StmChecker(), env);
    }

    private class StmChecker implements Stm.Visitor<void, Env> {
        public void visit(SExp p, Env env) {
            typeinfer(env, p.exp_);
        }

        public void visit(SDecls p, Env env) {

        }

        public void visit(SInit p, Env env) {

        }

        public void visit(SReturn p, Env env) {

        }

        public void visit(SWhile p, Env env) {

        }

        public void visit(SBlock p, Env env) {

        }

        public void visit(SIfElse p, Env env) {

        }
    }

    public void typeinfer(Env env, Exp exp) {
        return stm.accept(new ExpInferer(), env); //TODO return?
    }

    private class ExpInferer implements Exp.Visitor<Type, Env> {
        // basic
        public Type visit(ETrue e, Env env) { return Type_bool; }
        public Type visit(EFalse e, Env env) { return Type_bool; }
        public Type visit(EInt e, Env env) { return Type_int; }
        public Type visit(EDouble e, Env env) { return Type_double; }

        public Type visit(EId e, Env env) {
            return Type_void; //TODO lookup id type
        }
        public Type visit(EApp e, Env env) {
            return Type_void; //TODO lookup function type
        }

        //++ -- (implicit variable via parser)
        public Type visit(EPostIncr e, Env env) { return visit(e.exp_, env); }
        public Type visit(EPostDecr e, Env env) { return visit(e.exp_, env); }
        public Type visit(EPreIncr e, Env env) { return visit(e.exp_, env); }
        public Type visit(EPreDecr e, Env env) { return visit(e.exp_, env); }

        // * / + - assignment(implicit variable via parser)
        public Type visit(ETimes e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "*"); }
        public Type visit(EDiv e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "/"); }
        public Type visit(EPlus e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "+"); }
        public Type visit(EMinus e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "-"); }
        public Type visit(EAss e, Env env) { return sameType(e.exp_1, e.exp_2, env, "assignment"); }

        // < > >= ... && ||
        public Type visit(ELt e, Env env) { return boolType(e.exp_1, e.exp_2, env, "<");
        public Type visit(EGt e, Env env) { return boolType(e.exp_1, e.exp_2, env, ">");
        public Type visit(ELtEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, "<=");
        public Type visit(EGtEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, ">=");
        public Type visit(EEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, "==");
        public Type visit(ENEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, "!=");
        public Type visit(EAnd e, Env env) { return boolType(e.exp_1, e.exp_2, env, "&&");
        public Type visit(EOr e, Env env) { return boolType(e.exp_1, e.exp_2, env, "||");

        private Type sameType(Exp e1, Exp e2, Env env, String symbol) {
            Type type1 = visit(e1, env);
            Type type2 = visit(e2, env);
            if(!type1.equals(type2)) {
                throw new TypeException("Arguments for "+ symbol +
                " don't match (" + type1 + ", " + type2 + ")");
            }
            return type1;
        }

        private Type intDoubleType(Exp e1, Exp e2, Env env, String symbol) {
            Type type = sameType(e1, e2, env, symbol);
            if(!type.equals(new Type_int()) && !type.equals(new Type_double())) {
                throw new TypeException(symbol + " not applicable for " + type);
            }
            return type;
        }

        private Type boolType(Exp e1, Exp e2, Env env, String symbol) {
            Type type = sameType(e1, e2, env, symbol);
            if(!type.equals(new Type_bool()) && type.equals(new Type_int())
                && !type.equals(new Type_double())) {
                throw new TypeException("Comparison " + symbol +
                " not possible, given arguments (" + type1 + ", " + type2 + ")");
            }
            return new Type_bool();
        }
    }
}
