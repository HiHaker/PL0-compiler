import java.util.*;

/**
 * @Description 分析判定一个文法是不是LL1文法
 * @Author Jianlong
 * @Date 2020-04-27 上午 10:59
 */
public class LL1Analysis {

    // 存储文法的映射（产生式规则集合）
    private Map<String, String[]> P;
    // 文法的开始符号
    private String start;
    // 非终结符号集
    private Set<String> nonEndChars;
    // 终结符号集
    private Set<String> endChars;

    // FIRST集合
    private Map<String, Set<String>> FIRST;
    // FOLLOW集合
    private Map<String, Set<String>> FOLLOW;
    // SELECT集合
    private Map<String, Set<String>> SELECT;

    public LL1Analysis(){

        // 非终结符集合
        String[] Vn = {"E", "E\'", "T", "T\'", "F"};
        // 终结符集合
        // 这里定义空串为 NULL
        String[] Vt = {"+", "NULL", "*", "(", ")", "i"};
        // 预定义产生式规则集合P
        String[] P = {
                "E->TE\'",
                "E\'->+TE\'|NULL",
                "T->FT\'",
                "T\'->*FT\'|NULL",
                "F->(E)|i"
        };
        // 开始符号
        String S = "E";

        storeGrammar(Vn, Vt, P, S);
        preProcess();
    }

    public LL1Analysis(String[] Vn, String[] Vt, String[] P, String start){
        storeGrammar(Vn, Vt, P, start);
        preProcess();
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

    // 预处理
    private void preProcess(){
        calculateFIRST();
        calculateFOLLOW();
        calculateSELECT();
    }

    // 计算FIRST集合
    private void calculateFIRST(){

        FIRST = new HashMap<>();

        // 终结符号的FIRST集合就是它本身
        for (String value : endChars){
            Set<String> first = new HashSet<>();
            first.add(value);
            FIRST.put(value, first);
        }

        // 对于每个非终结符
        for (String value : nonEndChars){
            FIRST.put(value, FIRSTx(value));
        }

    }

    // 将字符串中的符号拆开
    private List<String> disassemble(String value){
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

    // 递归地计算某符号的FIRST
    private Set<String> FIRSTx(String x){

        Set<String> first = new HashSet<>();

        // 如果x是终结符，FIRST集就是它本身
        // 这里，空串也包含在终结符号里
        if (endChars.contains(x)){
            first.add(x);
            return first;
        } else {
            // 否则，就等于其右部各个符号的FIRST集相加
            String[] rightItems = P.get(x);
            // 遍历每一个右部
            for (String value : rightItems){
                List<String> cList = disassemble(value);

                // 如果右部的产生式是单个符号的，就直接加入到first集合
                if (cList.size() == 1){
                    first.addAll(FIRSTx(cList.get(0)));
                } else {
                    // 如果右部是多个符号，就看串首符号
                    for (int i=0; i<cList.size(); i++){
                        String character = cList.get(i);
                        // 如果是终结符，直接把它加入first集合
                        if (endChars.contains(character)){
                            first.add(character);
                            break;
                        } else {
                            // 如果是非终结符，就计算它的FIRST集
                            Set<String> cFirst = FIRSTx(character);
                            // 如果包含有空串，就要继续计算后一个符号的FIRST集
                            if (cFirst.contains("NULL")){
                                // 如果此时是最后一个符号了
                                // 就要把空串页加入进first集合
                                if (i == cList.size() - 1){
                                    first.addAll(cFirst);
                                } else {
                                    cFirst.remove("NULL");
                                    first.addAll(cFirst);
                                }
                            } else {
                                // 如果不包含空串，就不需要继续计算了
                                first.addAll(cFirst);
                                break;
                            }
                        }
                    }
                }
            }
            return first;
        }
    }

    private Map<String, Set<String>> getFIRST(){
        return this.FIRST;
    }

    // 将某个符号加入到某个非终结符的FOLLOW集合中
    private void addCharToFOLLOW(String character, String nonEndChar){
        Set<String> nonEndCharFollow = FOLLOW.get(nonEndChar);
        // 加入前先判断有没有在映射中了
        if (nonEndCharFollow != null){
            nonEndCharFollow.add(character);
        } else{
            nonEndCharFollow = new HashSet<>();
            nonEndCharFollow.add(character);
            FOLLOW.put(nonEndChar, nonEndCharFollow);
        }
    }

    // 将某个符号集加入到某个非终结符的FOLLOW集合中
    private void addCharsToFOLLOW(Set<String> characterSet, String nonEndChar){
        Set<String> nonEndCharFollow = FOLLOW.get(nonEndChar);
        // 加入前先判断有没有在映射中了
        if (nonEndCharFollow != null){
            nonEndCharFollow.addAll(characterSet);
        } else{
            nonEndCharFollow = new HashSet<>();
            nonEndCharFollow.addAll(characterSet);
            FOLLOW.put(nonEndChar, nonEndCharFollow);
        }
    }

    // 计算FOLLOW集合
    private void calculateFOLLOW(){

        FOLLOW = new HashMap<>();
        // 首先在开始符号的FOLLOW集中加入界符
        Set<String> startFollow = new HashSet<>();
        startFollow.add("#");
        FOLLOW.put(start, startFollow);

        for (String character : nonEndChars) {
            String[] rightItems = P.get(character);
            for (String item : rightItems) {
                // 获取符号列表
                List<String> cList = disassemble(item);
                String rightChar = cList.get(cList.size() - 1);
                // 如果最右的符号是非终结符，就要把界符加入到其FOLLOW集
                if (nonEndChars.contains(rightChar)) {
                    addCharToFOLLOW("#", rightChar);
                }
            }
        }

        for (String character : nonEndChars){
            FOLLOWx(character);
        }
    }

    // 递归地计算非终结符的FOLLOW集合
    private Set<String> FOLLOWx(String x){
        // 在产生式中搜索所有非终结符出现的位置
        for (String character : nonEndChars){
            String[] rightItems = P.get(character);

            for (String item : rightItems){
                // 获取符号列表
                List<String> cList = disassemble(item);

                // 接下来，搜索当前查找的非终结符的位置
                for (int i=0; i<cList.size(); i++){
                    String nonEndChar = cList.get(i);
                    if (nonEndChar.equals(x)){
                        // 判断是否处于最右的位置
                        if (i < cList.size() - 1){
                            // 如果没在最右边的位置
                            // 下面循环判断后一个符号是否是非终结符
                            for (int j=i+1; j<cList.size(); j++){
                                String nextChar = cList.get(j);
                                if (nonEndChars.contains(nextChar)){
                                    // 如果是非终结符，查看其FIRST集是否包含空串
                                    Set<String> nextFirst = FIRST.get(nextChar);
                                    // 如果包含空串，并且此时这个符号是最后一个符号
                                    // 就要将其FIRST除去空串的集合加入FOLLOW集，且左部的FOLLOW集加入FOLLOW集
                                    if (nextFirst.contains("NULL")){
                                        // 判断是否是最后一个符号
                                        if (j == cList.size() - 1){
                                            Set<String> leftFOLLOW = FOLLOWx(character);
                                            Set<String> nextFirstExceptNULL = new HashSet<>(nextFirst);
                                            nextFirstExceptNULL.remove("NULL");
                                            addCharsToFOLLOW(leftFOLLOW, x);
                                            addCharsToFOLLOW(nextFirstExceptNULL, x);
                                        } else{
                                            Set<String> nextFirstExceptNULL = new HashSet<>(nextFirst);
                                            nextFirstExceptNULL.remove("NULL");
                                            addCharsToFOLLOW(nextFirstExceptNULL, x);
                                        }
                                    } else{
                                        // 如果不包含空串加入FIRST之后跳出循环
                                        addCharsToFOLLOW(nextFirst, x);
                                        break;
                                    }
                                } else{
                                    // 如果不是非终结符，把此符号加入到当前查找的非终结符的FOLLOW集中
                                    addCharToFOLLOW(nextChar, nonEndChar);
                                    break;
                                }
                            }
                        }
                        // 如果在最右边，将FOLLOW（左部）加入到当前非终结符的FOLLOW集合
                        else{
                            // 首先判断当前的非终结符和左部是否相等，如果相等，break
                            if (character.equals(x)){
                                break;
                            }
                            Set<String> leftFOLLOW = FOLLOWx(character);
                            addCharsToFOLLOW(leftFOLLOW, x);
                        }
                    }
                }
            }
        }

        return FOLLOW.get(x);
    }

    public Map<String, Set<String>> getFOLLOW(){
        return FOLLOW;
    }

    // 计算SELECT集
    private void calculateSELECT(){

        // 为true的话，说明右部可以推导出空串
        boolean situation;
        SELECT = new HashMap<>();

        // 遍历产生式
        for (Map.Entry<String, String[]> entry : P.entrySet()){
            String leftItem = entry.getKey();
            String[] rightItems = entry.getValue();

            // 遍历右部
            for (String item : rightItems){
                situation = false;
                // 分解串为符号
                List<String> characters = disassemble(item);

                Set<String> first = new HashSet<>();

                // 计算串的FIRST集
                for (int i=0; i<characters.size(); i++){
                    String character = characters.get(i);
                    Set<String> currentFIRST = FIRST.get(character);
                    if (currentFIRST.contains("NULL")){
                        // 如果最后一个符号也能推导出空串，说明右部可以推导出空串
                        if (i == characters.size()-1){
                            situation = true;
                            first.addAll(FIRST.get(character));
                            first.remove("NULL");
                        } else{
                            first.addAll(FIRST.get(character));
                            first.remove("NULL");
                        }
                    } else{
                        first.addAll(FIRST.get(character));
                        break;
                    }
                }

                String p = leftItem + " -> " + item;

                if (situation){
                    Set<String> follow = FOLLOW.get(leftItem);
                    first.addAll(follow);
                    SELECT.put(p, first);
                } else{
                    SELECT.put(p, first);
                }
            }
        }
    }

    public Map<String, Set<String>> getSELECT(){
        return SELECT;
    }

    // 判断是否是LL1文法
    public boolean isLL1(){

        boolean isIntersect = false;

        outer:for (String P1 : SELECT.keySet()){

            String left1 = P1.substring(0,1);
            Set<String> set1 = SELECT.get(P1);
            Set<String> leftKeySet = new HashSet<>(SELECT.keySet());
            leftKeySet.remove(P1);

            for (String P2 : leftKeySet){
                String left2 = P2.substring(0,1);
                // 如果两条产生式的左部相等的话
                if (left1.equals(left2)){
                    Set<String> set2 = SELECT.get(P2);
                    // 判断是否相交
                    if (set1.retainAll(set2)){
                        isIntersect = true;
                        break outer;
                    }
                    else{
                        set1 = SELECT.get(P1);
                    }
                }
            }
        }

        return isIntersect;
    }

    public static void main(String[] args) {
        // 非终结符集合
//        String[] Vn = {"S", "S\'", "B", "A"};
        String[] Vn = {"C", "B", "E", "S", "D"};

        // 终结符集合
        // 这里定义空串为 NULL
//        String[] Vt = {"a", "b", "e", "NULL"};
        String[] Vt = {"i", "t", "e", "NULL", "a", "b", "+", "*"};

        // 预定义产生式规则集合P
//        String[] P = {
//                "S->aBS\'",
//                "S\'->bBS\'|NULL",
//                "B->Ab|e",
//                "A->a|NULL"
//        };
        String[] P = {
                "C->iEtSB",
                "B->NULL|eS",
                "E->a|b",
                "S->aD",
                "D->+b|*b"
        };
        // 开始符号
        String S = "C";

        LL1Analysis myLL1 = new LL1Analysis(Vn, Vt, P, S);
        // 打印出文法
        myLL1.printGrammar();

        System.out.println();
        System.out.println("FIRST集为: ");

        Map<String, Set<String>> FIRST = myLL1.getFIRST();
        for (Map.Entry<String, Set<String>> entry : FIRST.entrySet()){
            System.out.print("FIRST(" + entry.getKey() + "): { ");
            for (String value : entry.getValue()){
                System.out.print(value + " ");
            }
            System.out.println("}");
        }

        System.out.println();
        System.out.println("FOLLOW集为：");

        Map<String, Set<String>> FOLLOW = myLL1.getFOLLOW();
        for (String character : Vn){
            Set<String> follow = FOLLOW.get(character);
            System.out.println("FOLLOW(" + character + "): { ");
            for (String value : follow){
                System.out.print(value + " ");
            }
            System.out.println(" }");
        }

        System.out.println();
        System.out.println("SELECT集为: ");

        Map<String, Set<String>> SELECT = myLL1.getSELECT();
        for (Map.Entry<String, Set<String>> entry : SELECT.entrySet()){
            System.out.print("SELECT(" + entry.getKey() + "): { ");
            for (String value : entry.getValue()){
                System.out.print(value + " ");
            }
            System.out.println("}");
        }

        System.out.println();
        System.out.println("该文法是LL1文法吗？ " + myLL1.isLL1());
    }
}