import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import CPP.PrettyPrinter;
import CPP.Absyn.*;

public class Env {
    // functions
    public HashMap<String, FunType> signature = new HashMap<String, FunType>();
    // current variables for TypeChecker
    public LinkedList<HashMap<String,Type>> contexts = new LinkedList<HashMap<String,Type>>();
    // current variables for Interpreter
    public LinkedList<HashMap<String,Object>> varContexts = new LinkedList<HashMap<String,Object>>();
    
    public static Env empty() {
        return new Env();
    }

    public FunType lookupFun(String id) {
        if (signature.containsKey(id))
            return signature.get(id);
        else
            throw new TypeException("Function "+ id +" not defined");
    }

    public Type getTypeOfVar(String id) {
        // search from current to earlier contexts
        ListIterator<HashMap<String, Type>> listIterator = contexts.listIterator(contexts.size());

        while(listIterator.hasPrevious()) {
            HashMap<String, Type> context = listIterator.previous();
            if (context.containsKey(id))
                return context.get(id);
        }
        throw new TypeException("Var "+ id +" not declared in any context.");
    }
    
    public Object lookupVar(String id) {
        return varContexts.getLast().get(id);
    }
    public Env updateVar(String id, Type typ) {
        if (contexts.getLast().containsKey(id)) {
            throw new TypeException("Var "+ id +" already declared");
        } else {
            contexts.getLast().put(id, typ);
            return this;
        }
    }
    
    public Env updateVar(String id, Object varValue) {
        if (varContexts.getLast().containsKey(id)) {
            throw new RuntimeException("Var "+ id +" already declared");
        } else {
        	varContexts.getLast().put(id, varValue);
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
        varContexts.add(new HashMap<String,Object>());
        return this;
    }

    public Env exitBlock() {
        contexts.pollLast();
        varContexts.pollLast();
        return this;
    }
}

