import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import CPP.PrettyPrinter;
import CPP.Absyn.*;

public class TypeChecker {

    private enum TypeCode {
        INT, DOUBLE, BOOL, VOID
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

    private class StmChecker implements Stm.Visitor<Void, Env> {
        public Void visit(SExp p, Env env) {
            p.exp_.accept(new ExpInferer(), env);
            return null;
        }

        public Void visit(SDecls p, Env env) {
            for(String id:p.listid_) {
                env.updateVar(id, p.type_);
            }
            return null;
        }

        public Void visit(SInit p, Env env) {
            env.updateVar(p.id_, p.type_);
            return null;
        }

        public Void visit(SReturn p, Env env) {
            Type retType = env.lookupVar("return");
            //void
            if(retType.equals(new Type_void())) {
                throw new TypeException("Did not expect return statement in void method.");
            } else {
                Type expType = p.exp_.accept(new ExpInferer(),env);
                if (!retType.equals(expType)) {
                    throw new TypeException("Returned type doesn't match declared one.");
                }
            }
            return null;
        }

        public Void visit(SWhile p, Env env) {
            Type expType = p.exp_.accept(new ExpInferer(),env);
            if(!expType.equals(new Type_bool())) {
                throw new TypeException("Expression in while loop is not of type bool.");
            } else {
                env.newBlock();
                p.stm_.accept(this, env);
                env.exitBlock();
            }
            return null;
        }

        public Void visit(SBlock p, Env env) {
            return null;
        }

        public Void visit(SIfElse p, Env env) {
            return null;
        }
    }

    private class ExpInferer implements Exp.Visitor<Type, Env> {
        // basic
        public Type visit(ETrue e, Env env) { return new Type_bool(); }
        public Type visit(EFalse e, Env env) { return new Type_bool(); }
        public Type visit(EInt e, Env env) { return new Type_int(); }
        public Type visit(EDouble e, Env env) { return new Type_double(); }

        // var, function
        public Type visit(EId e, Env env) { return env.lookupVar(e.id_); }
        public Type visit(EApp e, Env env) { return env.lookupFun(e.id_).outtyp; }

        //++ -- (implicit variable via parser)
        public Type visit(EPostIncr e, Env env) { return e.exp_.accept(this, env); }
        public Type visit(EPostDecr e, Env env) { return e.exp_.accept(this, env); }
        public Type visit(EPreIncr e, Env env) { return e.exp_.accept(this, env); }
        public Type visit(EPreDecr e, Env env) { return e.exp_.accept(this, env); }

        // * / + - assignment(implicit variable via parser)
        public Type visit(ETimes e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "*"); }
        public Type visit(EDiv e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "/"); }
        public Type visit(EPlus e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "+"); }
        public Type visit(EMinus e, Env env) { return intDoubleType(e.exp_1, e.exp_2, env, "-"); }
        public Type visit(EAss e, Env env) { return sameType(e.exp_1, e.exp_2, env, "assignment"); }

        // < > >= ... && ||
        public Type visit(ELt e, Env env) { return boolType(e.exp_1, e.exp_2, env, "<"); }
        public Type visit(EGt e, Env env) { return boolType(e.exp_1, e.exp_2, env, ">"); }
        public Type visit(ELtEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, "<="); }
        public Type visit(EGtEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, ">="); }
        public Type visit(EEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, "=="); }
        public Type visit(ENEq e, Env env) { return boolType(e.exp_1, e.exp_2, env, "!="); }
        public Type visit(EAnd e, Env env) { return boolType(e.exp_1, e.exp_2, env, "&&"); }
        public Type visit(EOr e, Env env) { return boolType(e.exp_1, e.exp_2, env, "||"); }

        private Type sameType(Exp e1, Exp e2, Env env, String symbol) {
            Type type1 = e1.accept(this, env);
            Type type2 = e2.accept(this, env);
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
                " not possible, given arguments are not comparable.");
            }
            return new Type_bool();
        }
    }
}
