import java.util.ArrayList;

public class Descriptor
{
    String kind, value;
    boolean isArray, isConst, isImmidiate, isFunc = false;
    int level;
    ArrayList<Integer> arrayDims;
    ArrayList<ArrayList<String>> argms;

    Descriptor(String kind, int level, String value, boolean isArray, boolean isConst, boolean isImmidiate, ArrayList arrayDims, boolean isFunc)
    {
        this.kind = kind;
        this.level = level;
        this.value = value;
        this.isArray = isArray;
        this.isConst = isConst;
        this.isImmidiate = isImmidiate;
        this.arrayDims = arrayDims;
        this.isFunc = isFunc;
        argms = new ArrayList<>();
    }

    Descriptor(String kind, boolean isArray, boolean isConst, String exp, int level)
    {
        this.kind = kind;
        this.isArray = isArray;
        this.isConst = isConst;
        this.value = exp;
        this.level = level;
    }

    Descriptor(String s, boolean isArray, boolean isConst, int level)
    {
        kind = s;
        this.isArray = isArray;
        this.isConst = isConst;
        this.level = level;
    }

    Descriptor(String s, boolean isArray, boolean isConst, ArrayList<Integer> a, int level)
    {
        kind = s;
        this.isArray = isArray;
        this.isConst = isConst;
        arrayDims = a;
        this.level = level;
    }
}