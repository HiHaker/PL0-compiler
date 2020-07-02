import java.util.*;

/**
 * @Description 语法分析器（预测分析方法）
 * @Author Jianlong
 * @Date 2020-07-02 下午 17:11
 */
public class SyntacticParser {

    // 分析表
    private Map<String, String> analysisTable;

    // 构造函数，输入一个LL1文法和符号串，进行语法分析
    public SyntacticParser(Grammar G, List<String> str){

        LL1Analysis myLL1 = new LL1Analysis(G);

        // 如果文法是LL1文法的话，继续进行语法分析
        if (myLL1.isLL1()){
            // 获取SELECT集合，准备构建预测分析表
            Map<String, Set<String>> SELECT = myLL1.getSELECT();
            // 构造分析表
            createTable(SELECT);

            System.out.println("开始使用预测分析法进行语法分析:");
            System.out.println("输入的符号串为: ");

            for (String c : str){
                System.out.print(c+" ");
            }
            System.out.println();

            predictAnalysis(G, str);
        }
        // 否则报错
        else{
            throw new IllegalArgumentException("ERROR:输入的文法不是LL1文法!");
        }
    }

    public Map<String, String> getAnalysisTable() {
        return analysisTable;
    }

    // 创建分析表
    private void createTable(Map<String, Set<String>> SELECT){

        this.analysisTable = new HashMap<>();

        // 遍历SELECT集合
        for (Map.Entry<String, Set<String>> entry : SELECT.entrySet()){
            for (String value : entry.getValue()){
                String production = entry.getKey();
                this.analysisTable.put(production.split("->")[0].trim()+value, production);
            }
        }
    }

    // 预测分析方法
    private void predictAnalysis(Grammar grammar, List<String> str){
        // 分析栈
        Stack<String> analysisStack = new Stack<>();
        // 首先放入界符
        analysisStack.push("#");
        // 放入开始符号
        analysisStack.push(grammar.getStart());

        System.out.println("初始状态：放入左界符和开始符号...");

        for (int i=0; i<str.size(); i++){
            String x = analysisStack.peek();
            String c = str.get(i);

            System.out.println("栈顶符号为"+x+", 指针指向输入串的符号为"+c);

            // 如果栈顶是终结符
            if (grammar.getEndChars().contains(x) || x.equals("#")){
                if (c.equals("#") && c.equals(x)){
                    System.out.println("分析成功!");
                }
                else{
                    System.out.println("符号匹配，出栈，指针指向下一个符号");
                    analysisStack.pop();
                }
            }
            else{

                System.out.println("寻找对应产生式进行规约...");
                String production = this.analysisTable.get(x+c);

                if (production != null){

                    System.out.println("找到了产生式 "+production);

                    analysisStack.pop();
                    String right = production.split("->")[1].trim();
                    List<String> rightChars = grammar.disassemble(right);

                    for (int j=rightChars.size()-1; j>=0; j--){

                        String tempChar = rightChars.get(j);

                        // 如果是空串，就不入栈
                        if (tempChar.equals("NULL")){
                            continue;
                        }
                        analysisStack.push(tempChar);
                    }
                    i--;
                }
                else{
                    System.out.println("未找到产生式...");
                    throw new IllegalArgumentException("ERROR:分析出错!");
                }
            }
        }
    }

    public static void main(String[] args) {
        // 非终结符集合
        String[] Vn = {"S", "S\'", "B", "A"};

        // 终结符集合
        // 这里定义空串为 NULL
        String[] Vt = {"a", "b", "e", "NULL"};

        // 预定义产生式规则集合P
        String[] P = {
                "S->aBS\'",
                "S\'->bBS\'|NULL",
                "B->Ab|e",
                "A->a|NULL"
        };

        // 开始符号
        String S = "S";

        List<String> inputStr = new ArrayList<>();
        inputStr.add("a");
        inputStr.add("a");
        inputStr.add("b");
        inputStr.add("#");

        SyntacticParser myParser = new SyntacticParser(new Grammar(Vn, Vt, P, S), inputStr);
    }
}
