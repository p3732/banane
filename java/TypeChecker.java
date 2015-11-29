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
        public Type visit(EPlus e, Env env) {
            if(typecheckExp(env, new Type_int(), e.exp_1) &&
                typecheckExp(env, new Type_int(), e.exp_2)) {

            } else if(typecheckExp(env, new Type_double(), e.exp_1) &&
                typecheckExp(env, new Type_double(), e.exp_2)) {
            } else {

            }
            return new Type_int();
        }

        boolean typecheckExp(Env env, Type type1, Type type2) {
            return type1.equals(type2);
        }
    }
}
