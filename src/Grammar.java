import java.util.*;

/**
 * @Description 文法类
 * @Author Jianlong
 * @Date 2020-05-07 下午 19:54
 */
public class Grammar {
    // 存储文法的映射（产生式规则集合）
    private Map<String, String[]> P;
    // 文法的开始符号
    private String start;
    // 非终结符号集
    private Set<String> nonEndChars;
    // 终结符号集
    private Set<String> endChars;

    public Grammar(String[] Vn, String[] Vt, String[] P, String start){
        storeGrammar(Vn, Vt, P, start);
    }

    // 存储文法
    private void storeGrammar(String[] Vn, String[] Vt, String[] P, String start){

        this.nonEndChars = new HashSet<>();
        this.endChars = new HashSet<>();
        this.start = start;
        this.P = new HashMap<>();

        try{
            // 存储产生式
            for (String value : P) {
                // 分割左部、右部
                String[] split1 = value.split("->");
                // 分割右部的不同产生式
                String[] split2 = split1[1].split("\\|");
                this.P.put(split1[0], split2);
            }

            // 存储符号集
            Collections.addAll(nonEndChars, Vn);
            Collections.addAll(endChars, Vt);
        } catch (Exception e){
            System.out.println("输入文法有错误!");
            e.printStackTrace();
        }
    }

    // 打印文法
    public void printGrammar(){

        System.out.println("此文法的非终结符号集为: ");
        System.out.print("{ ");
        for (String value : nonEndChars){
            System.out.print(value + " ");
        }
        System.out.print("}");

        System.out.println();

        System.out.println("此文法的终结符号集为: ");
        System.out.print("{ ");
        for (String value : endChars){
            System.out.print(value + " ");
        }
        System.out.print("}");

        System.out.println();

        System.out.println("此文法的产生式规则集为: ");
        System.out.println("{ ");
        // 需要遍历map
        for (Map.Entry<String, String[]> entry : P.entrySet()){
            String leftItem = entry.getKey();
            String[] rightItems = entry.getValue();
            System.out.print("  " + leftItem + " -> ");
            for (int i=0; i<rightItems.length; i++){
                if (i == rightItems.length - 1){
                    System.out.print(rightItems[i]);
                } else {
                    System.out.print(rightItems[i] + " | ");
                }
            }
            System.out.println();
        }
        System.out.print("}");

        System.out.println();

        System.out.println("此文法的开始符号为: ");
        System.out.println(start);
    }

    // 将字符串中的符号拆开
    public List<String> disassemble(String value){
        char c;
        List<String> cList = new ArrayList<>();

        // 将右部分解为一个个的符号
        for (int i=0; i<value.length(); i++){
            if (value.equals("NULL")){
                cList.add(value);
                break;
            }
            if (i+1<value.length() && value.charAt(i+1) == '\''){
                cList.add(value.substring(i, i+2));
                i+=1;
            } else {
                cList.add(value.substring(i, i+1));
            }
        }

        return cList;
    }

    public Map<String, String[]> getP() {
        return P;
    }

    public String getStart() {
        return start;
    }

    public Set<String> getNonEndChars() {
        return nonEndChars;
    }

    public Set<String> getEndChars() {
        return endChars;
    }
}