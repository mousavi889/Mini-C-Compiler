import java.util.ArrayList;

public class Optimizer
{
    ThreeAddress code[], optCode[];
    int pc;
    final int MAX_Of_LINES = 200 + 20;

    Optimizer(ThreeAddress code[], int pc)
    {
        this.code = code;
        this.pc = pc;
    }

    int deadCodeElimination()
    {
        class Node
        {
            int start, end;
            boolean mark;

            Node(int start, int end)
            {
                this.start = start;
                this.end = end;
                mark = false;
            }

            void dfs(Node node, Node nodes[], int numNode[])
            {
                if (node.mark)
                    return;
                node.mark = true;
                if (code[node.end].operator.compareTo("JZ") == 0 || code[node.end].operator.compareTo("JNZ") == 0)
                {
                    dfs(nodes[numNode[new Integer(code[node.end].operands.get(1))]], nodes, numNode);
                    dfs(nodes[numNode[node.end + 1]], nodes, numNode);
                }
                else if (code[node.end].operator.compareTo("JP") == 0)
                    dfs(nodes[numNode[new Integer(code[node.end].operands.get(0))]], nodes, numNode);
                else if(code[node.end].operator.compareTo("END") != 0 && node.end + 1 != pc )
                    dfs(nodes[numNode[node.end + 1]] , nodes, numNode);
            }
        }

        boolean isInFunc = false, isStart[] = new boolean[MAX_Of_LINES], isS[] = new boolean[MAX_Of_LINES];
        int nodeNum[] = new int[MAX_Of_LINES];
        Node nodes[] = new Node[MAX_Of_LINES];
        ArrayList<Integer> s = new ArrayList<>();
        optCode = new ThreeAddress[MAX_Of_LINES];
        int optpc = 0, n = 0, p = 0;

        isStart[0] = true;
        for (int i = 1; i < pc; i++)
            isStart[i] = false;
        isStart[pc] = true;
        for (int i = 0; i <= pc; i++)
            isS[i] = false;
        for (int i = 0; i < pc; i++)
        {
            if (!isInFunc)
            {
                isStart[i] = true;
                isS[i] = true;
            }
            if (code[i].operator.compareTo("FUN") == 0)
            {
                isInFunc = true;
            }
            if (code[i].operator.compareTo("END") == 0)
            {
                isInFunc = false;
            }
            if (code[i].operator.compareTo("JZ") == 0 || code[i].operator.compareTo("JNZ") == 0)
            {
                isStart[i + 1] = true;
                isStart[new Integer(code[i].operands.get(1))] = true;
            }
            if (code[i].operator.compareTo("JP") == 0)
            {
                isStart[i + 1] = true;
                isStart[new Integer(code[i].operands.get(0))] = true;
            }
        }
        while (p < pc)
        {
            if (isStart[p])
            {
                int start = p;
                while (!isStart[p + 1])
                {
                    nodeNum[p] = n;
                    p++;
                }
                nodeNum[p] = n;
                nodes[n] = new Node(start, p);
                if (isS[start])
                    s.add(n);
                p++;
                n++;
            }
        }

        for (int i = 0; i < s.size(); i++)
        {
            nodes[0].dfs(nodes[s.get(i)], nodes, nodeNum);
        }

        for (int i = 0; i < n; i++)
        {
            if (nodes[i].mark)
            {
                for (int j = nodes[i].start; j <= nodes[i].end; j++)
                {
                    optCode[optpc] = code[j];
                    optpc++;
                }
            }

        }

        code = optCode;
        return optpc;
    }
}
