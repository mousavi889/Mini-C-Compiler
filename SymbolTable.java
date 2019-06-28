import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class SymbolTable
{
    Map <String, Descriptor> table;

    public SymbolTable()
    {
        table = new TreeMap<>();
    }


    public void addConstToTable(String id, String kind, String value, int level)
    {
        if (table.containsKey(id))
            System.out.println("Error, ReDeclare ID");
        table.put(id, new Descriptor(kind, false, true, value, level));
        // bayad value check beshe
    }


    public void addToTable(String id, String kind, ArrayList<Integer> arrayDims, int level)
    {
        if (table.containsKey(id))
            System.out.println("Error, ReDeclare ID");

        if (arrayDims.size() == 0)
            table.put(id, new Descriptor(kind, false, false, level));
        else
        {
            table.put(id, new Descriptor(kind, true, false, arrayDims, level));
        }
    }
}