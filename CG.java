import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Stack;

public class CG
{
    int pc = 0, t = 0;
    final int MAXN = 200 + 20;
    boolean isInDcl;
    String typeVarDcl, funcValue;
    ThreeAddress code[];
    File interCode;
    BufferedWriter output;
    Stack<SymbolTable> symbolTableStack;
    ArrayList <Integer> arrayDims;
    ArrayList <String> argms, parms;

    CG ()
    {
        code = new ThreeAddress[MAXN];
        symbolTableStack = new Stack<>();
        symbolTableStack.push(new SymbolTable());
        symbolTableStack.get(0).table.put("true", new Descriptor("BOOL", 0, "true", false, false, true, null, false));
        symbolTableStack.get(0).table.put("false", new Descriptor("BOOL", 0, "false", false, false, true, null, false));
        parms = new ArrayList<>();
        isInDcl = false;
    }

    void writeOutput()
    {
        //Optimizer optimizer = new Optimizer(code, pc);
        //pc = optimizer.deadCodeElimination();
        Descriptor dscp = getDescriptor("main");
        if (dscp == null)
            System.out.println("ERROR" + " there is no main function.");
        interCode = new File("interCode");
        try
        {
            output = new BufferedWriter(new FileWriter(interCode));
            for (int i = 0; i < pc; i++)
                code[i].print(output);
            output.flush();
            output.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void openScope()
    {
        symbolTableStack.push(new SymbolTable());
    }

    void deleteScope()
    {
        symbolTableStack.pop();
    }

    String methodCall(String id)
    {
        Descriptor fdscp = symbolTableStack.get(0).table.get(id);
        if (fdscp == null || ! fdscp.isFunc)
            System.out.println("ERROR" + " not declared func");
        ArrayList <String> x = new ArrayList<>();
        for (int i = 0; i < parms.size(); i++)
            x.add(getDescriptor(parms.get(i)).kind);
        boolean e = false;
        for (int i = 0; i < fdscp.argms.size(); i++)
        {
            if (x.size() != fdscp.argms.get(i).size())
                continue;
            e = true;
            for (int j = 0; j < x.size(); j++)
                if (x.get(j) != fdscp.argms.get(i).get(j))
                    e = false;
            if (e)
                break;
        }
        if (!e)
            System.out.println("ERROR" + " not declared func with this signature");
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        String ovl = "";
        if (parms.size() == 0)
            ovl = "0";
        for (int i = 0; i < parms.size(); i++)
        {
            Descriptor pdscp = getDescriptor(parms.get(i));
            ovl += ("" + getSizeOfType(pdscp.kind) + pdscp.kind.charAt(0));
        }
        code[pc].set("CALL", ovl + id, parms);
        pc++;
        symbolTableStack.peek().table.put(ovl + id, new Descriptor(fdscp.value, 0, null, false, false, false, null, false)); //bayad be noe tabe negah koni int inasho doros koni o error o ina
        return ovl + id;
    }

    void checkHasValue(String id)
    {
        // ba estefade az noe argoman bayad esme tabe ro peyda konim (mesal 4Fsalam) search konim
    }

    String bitWiseNeg(String a)
    {
        Descriptor adscp = getDescriptor(a);
        if (adscp.kind.compareTo("BOOL") == 0)
            System.out.println("ERROR" + " ~'s" + " operand can not be bool");
        if (adscp.kind.compareTo("FLOAT") == 0)
            System.out.println("ERROR" + " ~'s" + " operand can not be float");
        String alevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "_" + adscp.level;
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor(adscp.kind, symbolTableStack.size() - 1, null, false, false, false, null, false));
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("~", "$t" + t, alevel + a);
        pc++;
        return "$t" + t;
    }

    String dec(String a)
    {
        Descriptor adscp = getDescriptor(a);
        if (adscp.kind.compareTo("BOOL") == 0)
            System.out.println("ERROR" + " --'s" + " operand can not be bool");
        String alevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "_" + adscp.level;
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor(adscp.kind, symbolTableStack.size() - 1, null, false, false, false, null, false));
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("-", "$t" + t, alevel + a, "#1");
        pc++;
        return "$t" + t;
    }

    String inc(String a)
    {
        Descriptor adscp = getDescriptor(a);
        if (adscp.kind.compareTo("BOOL") == 0)
            System.out.println("ERROR" + " ++'s" + " operand can not be bool");
        String alevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "_" + adscp.level;
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor(adscp.kind, symbolTableStack.size() - 1, null, false, false, false, null, false));
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("+", "$t" + t, alevel + a, "#1");
        pc++;
        return "$t" + t;
    }

    void initParms()
    {
        parms.clear();
    }

    void addParms(String exp)
    {
        parms.add(exp);
    }

    void returnFP(String exp)
    {
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        if (exp != null)
        {
            String op = exp;
            Descriptor adscp =  getDescriptor(exp);
            code[pc].set("RET", op);
        }
        else
            code[pc].set("RET");
        pc++;
    }

    void func(String id, String addr)
    {
        if (!symbolTableStack.get(0).table.containsKey(id))
            symbolTableStack.get(0).table.put(id, new Descriptor(null, 0, funcValue, false, false, false, null, true));
        Descriptor fdscp = symbolTableStack.get(0).table.get(id);
        if (fdscp.value.compareTo(funcValue) != 0)
            System.out.println("ERROR" + " can not be overload. return type not same");
        symbolTableStack.get(0).table.get(id).argms.add(argms);
        Integer funcPC = new Integer(addr);
        if (code[funcPC] == null)
            code[funcPC] = new ThreeAddress();
        String ovl = "";
        for (int i = 0; i < argms.size(); i++)
            ovl += ("" + getSizeOfType(argms.get(i)) + argms.get(i).charAt(0));
        code[funcPC].set("FUN", ovl + id, "," + getSizeOfType());
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("END" , ovl + id);
        pc++;
    }

    void proc(String id, String addr)
    {
        if (!symbolTableStack.get(0).table.containsKey(id))
            symbolTableStack.get(0).table.put(id, new Descriptor(null, 0, funcValue, false, false, false, null, true));
        Descriptor fdscp = symbolTableStack.get(0).table.get(id);
        if (fdscp.value.compareTo(funcValue) != 0)
            System.out.println("ERROR" +  " can not be overload. return type not same");
        symbolTableStack.get(0).table.get(id).argms.add(argms);
        Integer funcPC = new Integer(addr);
        if (code[funcPC] == null)
            code[funcPC] = new ThreeAddress();
        code[funcPC].set("FUN", "0" + id, "," + getSizeOfType());
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("END" , "0" + id);
        pc++;
    }

    String resvLine()
    {
        funcValue = typeVarDcl;
        pc++;
        return pc - 1 + "";
    }

    void initArgms()
    {
        argms = new ArrayList<>();
    }

    void arg(String id)
    {
        symbolTableStack.peek().table.put(id, new Descriptor(typeVarDcl, 1, null, false, false, false, null, false));
        argms.add(typeVarDcl);
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("ARG", "" + sizeOfVarDcl() , id);
        pc++;
    }

    String arithmeticOperand(String op, String a)
    {
        Descriptor adscp =  getDescriptor(a);
        String alevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "" + adscp.level;
        switch (op)
        {
            case "-":
                if (adscp.kind.compareTo("BOOL") == 0)
                    System.out.println("ERROR" + " u-minus's operand can not be bool");
                t++;
                symbolTableStack.peek().table.put("$t" + t, new Descriptor(adscp.kind, symbolTableStack.size() - 1, null, false, false, false, null, false));
                if (code[pc] == null)
                    code[pc] = new ThreeAddress();
                if (adscp.kind.compareTo("FLOAT") == 0)
                    code[pc].set("F"+op, "$t" + t, "#0", alevel + a);
                else
                    code[pc].set(op, "$t" + t, "#0", alevel + a);
                pc++;
                return "$t" + t;
            case "!":
                t++;
                symbolTableStack.peek().table.put("$t" + t, new Descriptor("BOOL", symbolTableStack.size() - 1, null, false, false, false, null, false));
                if (code[pc] == null)
                    code[pc] = new ThreeAddress();
                code[pc].set(op, "$t" + t, alevel + a);
                pc++;
                return "$t" + t;
        }
        return null;
    }

    String arithmeticOperand(String op, String a , String b)
    {
        Descriptor adscp =  getDescriptor(a), bdscp = getDescriptor(b);
        String alevel = "", blevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "_" + adscp.level;
        if (bdscp.level > 0 && b.charAt(0) != '$')
            blevel = "_" + bdscp.level;
        switch (op)
        {
            case "+":
            case "-":
            case "*":
            case "/":
                if (adscp.kind.compareTo("BOOL") == 0 || bdscp.kind.compareTo("BOOL") == 0)
                    System.out.println("ERROR" + " " + op + " 's operands can not be bool");
                t++;
                if (code[pc] == null)
                    code[pc] = new ThreeAddress();
                if (adscp.kind.compareTo("FLOAT") == 0 || bdscp.kind.compareTo("FLOAT") == 0)
                {
                    symbolTableStack.peek().table.put("$t" + t, new Descriptor("FLOAT", symbolTableStack.size() - 1, null, false, false, false, null, false));
                    code[pc].set("F"+op, "$t" + t, alevel + a, blevel + b);
                }
                else
                {
                    symbolTableStack.peek().table.put("$t" + t, new Descriptor("INT", symbolTableStack.size() - 1, null, false, false, false, null, false));
                    code[pc].set(op, "$t" + t, alevel + a, blevel + b);
                }
                pc++;
                return "$t" + t;
            case "%":
                if (adscp.kind.compareTo("INT") != 0 || bdscp.kind.compareTo("INT") != 0)
                    System.out.println("ERROR" + " "+  op + "'s oprands can not be bool");
                t++;
                if (code[pc] == null)
                    code[pc] = new ThreeAddress();
                symbolTableStack.peek().table.put("$t" + t, new Descriptor("INT", symbolTableStack.size() - 1, null, false, false, false, null, false));
                code[pc].set(op, "$t" + t, alevel + a, blevel + b);
                pc++;
                return "$t" + t;
        }
        return null;
    }

    String bitWiseOperand(String op, String a, String b)
    {
        Descriptor adscp = getDescriptor(a), bdscp = getDescriptor(b);
        if (adscp.kind.compareTo("BOOL") == 0 || bdscp.kind.compareTo("BOOL") == 0)
            System.out.println("ERROR" + " " + op + "'s operands can not be bool");
        if (adscp.kind.compareTo("FLOAT") == 0 || bdscp.kind.compareTo("FLOAT") == 0)
            System.out.println("ERROR" + " " + op + "'s operan's can not be float");
        if (getSizeOfType(adscp.kind) != getSizeOfType(bdscp.kind))
            System.out.println("ERROR" + " " + op + "'s operands are not same size");
        String alevel = "", blevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "_" + adscp.level;
        if (bdscp.level > 0 && b.charAt(0) != '$')
            blevel = "_" + bdscp.level;
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor(adscp.kind, symbolTableStack.size() - 1, null, false, false, false, null, false));
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set(op, "$t" + t, alevel + a, blevel + b);
        pc++;
        return "$t" + t;
    }

    String logicalOperand(String op, String a, String b)
    {
        Descriptor adscp = getDescriptor(a), bdscp = getDescriptor(b);
        String alevel = "", blevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "_" + adscp.level;
        if (bdscp.level > 0 && b.charAt(0) != '$')
            blevel = "_" + bdscp.level;
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor("BOOL", symbolTableStack.size() - 1, null, false, false, false, null, false));
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set(op, "$t" + t, alevel + a, blevel + b);
        pc++;
        return "$t" + t;
    }

    String conditionalOperand(String op, String a, String b)
    {
        Descriptor adscp = getDescriptor(a), bdscp = getDescriptor(b);
        if (adscp.kind.compareTo("BOOL") == 0 || bdscp.kind.compareTo("BOOL") == 0)
            System.out.println("ERROR" + " " + op + "'s operands can not be bool");
        String alevel = "", blevel = "";
        if (adscp.level > 0 && a.charAt(0) != '$')
            alevel = "_" + adscp.level;
        if (bdscp.level > 0 && b.charAt(0) != '$')
            blevel = "_" + bdscp.level;
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor("BOOL", symbolTableStack.size() - 1, null, false, false, false, null, false));
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set(op, "$t" + t, alevel + a, blevel + b);
        pc++;
        return "$t" + t;
    }

    Descriptor getDescriptor(String s)
    {
        int level = symbolTableStack.size() - 1;
        while(!symbolTableStack.get(level).table.containsKey(s))
        {
            level--;
            if (level == -1)
            {
                if (s.compareTo("main") != 0)
                    System.out.println("ERROR" + " not declared id");
                return null;
            }
        }
        return symbolTableStack.get(level).table.get(s);
    }

    void addIntTable(Integer i)
    {
        if (!symbolTableStack.get(0).table.containsKey(i.toString()))
            symbolTableStack.get(0).table.put("#" + i, new Descriptor("INT", 0, i.toString(), false, false, true, null, false));
    }

    void addFloatTable(Double d)
    {
        if (!symbolTableStack.get(0).table.containsKey(d.toString()))
            symbolTableStack.get(0).table.put("#" + d, new Descriptor("FLOAT", 0, d.toString(), false, false, true, null, false));
    }

    void addCharTable(String c)
    {
        if (!symbolTableStack.get(0).table.containsKey(c.toString()))
            symbolTableStack.get(0).table.put(c, new Descriptor("CHAR",0, c.substring(1, 2), false, false, true, null, false));
    }

    void assign(String id, String exp)
    {
        Descriptor iddscp = getDescriptor(id);
        if (iddscp.isConst)
            System.out.println("ERROR" + " const variable can not change");
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("=", id, exp);
        pc++;
    }

    void elsePart(String addr)
    {
        Integer jpPC = new Integer(addr);
        if (code[jpPC] == null)
            code[jpPC] = new ThreeAddress();
        code[jpPC].operands.set(0, "" + pc);
    }

    String elseJP()
    {
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].operator = "JP";
        pc++;
        return (new Integer(pc - 1)).toString();
    }

    void ifJZ()
    {
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].operator = "JZ";
        pc++;
    }

    void completeIf(String exp, String addr1, String addr2)
    {
        Integer jzPC = new Integer(addr1);
        if (code[jzPC] == null)
            code[jzPC] = new ThreeAddress();
        code[jzPC].operands.set(0, exp);
        code[jzPC].operands.set(1, addr2);
    }

    void completeFor(String exp, String addr)
    {
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("JP", addr);
        pc++;
        Integer jzPC = new Integer(addr);
        if (code[jzPC] == null)
            code[jzPC] = new ThreeAddress();
        code[jzPC].operands.set(0, exp);
        code[jzPC].operands.set(1, "" + pc);
    }

    void completeRepeat(String exp, String addr)
    {
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("JNZ",exp, addr);
        pc++;
    }

    String storePC()
    {
        return (new Integer(pc)).toString();
    }

    void constVarDcl(String id, Integer value)
    {
        if (symbolTableStack.peek().table.containsKey(id))
        {
            System.out.println("ERROR" + "redeclared id");
            return;
        }
        if (symbolTableStack.size() == 1)
        {
            System.out.println("ERROR" + "no const global" );
        }
        symbolTableStack.peek().addConstToTable(id, typeVarDcl, " " + value, symbolTableStack.size() - 1);
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("CVAR", "" + sizeOfVarDcl(), id, "" + value);
        pc++;
    }

    void varDcl(String variable, String exp)
    {
        if (symbolTableStack.peek().table.containsKey(variable))
        {
            System.out.println("ERROR" + " " + variable +  " redeclared id");
            return;
        }
        if (exp != null && symbolTableStack.size() == 1)
            System.out.println("ERROR" + " global variable can not initial" );
        symbolTableStack.peek().addToTable(variable, typeVarDcl, arrayDims, symbolTableStack.size() - 1);
        Descriptor edscp = null, vdscp = getDescriptor(variable);
        if (exp != null)
            edscp = getDescriptor(exp);
        String varLevel = "", expLevel = "";
        if (edscp != null && edscp.level > 0 && exp.charAt(0) != '$')
            expLevel = "_" + edscp.level;
        if (vdscp.level > 0 && variable.charAt(0) != '$')
            varLevel = "_" + vdscp.level;
        if (arrayDims.size() == 0)
        {
            if (code[pc] == null)
                code[pc] = new ThreeAddress();
            code[pc].set("VAR", "" + sizeOfVarDcl(), varLevel + variable, expLevel + exp);
            pc++;
        }
        else
        {
            int tmp = arrayDims.get(0);
            for (int i = 1; i < arrayDims.size(); i++)
                tmp *= arrayDims.get(i);
            if (code[pc] == null)
                code[pc] = new ThreeAddress();
            code[pc].set("ARR", "" + sizeOfVarDcl(), "#" + tmp, variable);
            pc++;
        }
    }

    void arrayInit()
    {
        arrayDims = new ArrayList<>();
    }

    void addArrayDim(Integer dim)
    {
        arrayDims.add(dim);
    }

    void foreach(String addr, String a, String b)
    {
        Integer fpc = new Integer(addr);
        Descriptor adscp = getDescriptor(a), bdscp = getDescriptor(b);
        if (bdscp.arrayDims.size() != 1)
            System.out.println("ERROR" + " " + b + " is not 1D array");
        if (adscp != null && adscp.level == symbolTableStack.size() - 1)
            System.out.println("ERROR" + " " + a + " redeclared id");
        symbolTableStack.peek().table.put(a, new Descriptor(bdscp.kind, symbolTableStack.size() - 1, null, false, false, false, null, false));
        if (code[fpc] == null)
            code[fpc] = new ThreeAddress();
        code[fpc].set("VAR", "" + getSizeOfType(bdscp.kind), "," + a);
        fpc++;
        if (code[fpc] == null)
            code[fpc] = new ThreeAddress();
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor("INT", symbolTableStack.size() - 1, null, false, false, false, null, false));
        code[fpc].set("=", "$t" + t, "#0");
        fpc++;
        if (code[fpc] == null)
            code[fpc] = new ThreeAddress();
        t++;
        symbolTableStack.peek().table.put("$t" + t, new Descriptor("INT", symbolTableStack.size() - 1, null, false, false, false, null, false));
        code[fpc].set("-", "$t" + t, "#" + bdscp.arrayDims.get(0), "$t" + (t - 1));
        fpc++;
        if (code[fpc] == null)
            code[fpc] = new ThreeAddress();
        code[fpc].set("JZ", "$t" + t, "" + (pc + 2));
        fpc++;
        if (code[fpc] == null)
            code[fpc] = new ThreeAddress();
        code[fpc].set("=", "_" + (symbolTableStack.size() - 1) + a, "@" + "$t" + (t - 1) + ":" + b);
        fpc++;
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("=", "$t" + (t - 1), "$t" + (t - 1), "#1");
        pc++;
        if (code[pc] == null)
            code[pc] = new ThreeAddress();
        code[pc].set("JP", "" + (fpc - 3));
        pc++;
    }

    int getSizeOfType()
    {
        if (funcValue.compareTo("BOOL") == 0)
            return 1;
        if (funcValue.compareTo("CHAR") == 0)
            return 1;
        if (funcValue.compareTo("INT") == 0)
            return 4;
        if (funcValue.compareTo("FLOAT") == 0)
            return 4;
        return 0;
    }

    int getSizeOfType(String s)
    {
    if (s.compareTo("BOOL") == 0)
        return 1;
    if (s.compareTo("CHAR") == 0)
        return 1;
    if (s.compareTo("INT") == 0)
        return 4;
    if (s.compareTo("FLOAT") == 0)
        return 4;
    return 0;
}

    int sizeOfVarDcl()
    {
        if (typeVarDcl.compareTo("BOOL") == 0)
            return 1;
        if (typeVarDcl.compareTo("CHAR") == 0)
            return 1;
        if (typeVarDcl.compareTo("INT") == 0)
            return 4;
        if (typeVarDcl.compareTo("FLOAT") == 0)
            return 4;
        return 0;
    }
}
