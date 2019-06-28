import java.io.BufferedWriter;
import java.util.ArrayList;

public class ThreeAddress
{
    String operator;
    ArrayList<String> operands;

    ThreeAddress()
    {
        operands = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            operands.add("");
    }

    void set(String op)
    {
        operator = op;
    }

    void set(String op, String opr0, ArrayList<String> a)
    {
        operator = op;
        operands.clear();
        operands.add(opr0);
        for (int i = 0; i < a.size(); i++)
            operands.add(a.get(i));
    }

    void set(String op, String opr0)
    {
        operator = op;
        operands.set(0, opr0);
    }

    void set(String op, String opr0, String opr1)
    {
        operator = op;
        operands.set(0, opr0);
        operands.set(1, opr1);
    }

    void set(String op, String opr0, String opr1, String opr2)
    {
        operator = op;
        operands.set(0, opr0);
        operands.set(1, opr1);
        operands.set(2, opr2);
    }

    void print(BufferedWriter output)
    {
        try
        {
            output.write(operator);
            for (int i = 0; i < operands.size() && operands.get(i) != null; i++)
                output.write(" " + operands.get(i));
            output.write('\n');
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
