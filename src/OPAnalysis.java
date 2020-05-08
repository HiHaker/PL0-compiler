import java.util.*;

/**
 * @Description 分析一个文法是不是算符优先文法
 * @Author Jianlong
 * @Date 2020-05-07 下午 19:51
 */
public class OPAnalysis {

    private Grammar grammar;

    // FIRSTVT集合
    private Map<String, Set<String>> FIRSTVT;
    // FOLLOWVT集合
    private Map<String, Set<String>> LASTVT;
    // 算符优先关系矩阵
    private String[][] matrix;
    // 存储终结符号对应矩阵的下标
    private Map<String, Integer> index;
    // 是否是算符优先文法
    private boolean isOPGrammar;

    public OPAnalysis(){
        // 非终结符集合
        String[] Vn = {"E", "T", "F"};
        // 终结符集合
        // 这里定义空串为 NULL
        String[] Vt = {"+", "*", "(", ")", "i"};
        // 预定义产生式规则集合P
        String[] P = {
                "E->E+T|T",
                "T->T*F|F",
                "F->(E)|i"
        };
        // 开始符号
        String S = "E";

        this.grammar = new Grammar(Vn, Vt, P, S);
        grammar.printGrammar();
        initialize();
    }

    public OPAnalysis(Grammar grammar){
        this.grammar = grammar;
        grammar.printGrammar();
        initialize();
    }

    private void initialize(){
        // 根据终结符的个数构造优先关系矩阵
        Set<String> endChars = grammar.getEndChars();
        index = new HashMap<>();
        matrix = new String[endChars.size()][endChars.size()];
        int count = 0;
        for (String endChar : endChars){
            index.put(endChar, count);
            count++;
        }
    }

    /**
     * 判断该文法是否是算符文法
     * 算符文法：任何一个产生式中不包含两个非终结符相邻的情况
     * @return
     */
    public boolean isOperatorG(){

        // 标识是否有非终结符相邻的情况
        boolean flag = false;

        Map<String, String[]> P = grammar.getP();
        Set<String> nonEndChars = grammar.getNonEndChars();

        // 遍历所有产生式
        outer:for (String[] rightItems : P.values()) {
            // 遍历右部
            for (String item : rightItems) {
                // 分解为符号列表
                List<String> characters = grammar.disassemble(item);
                // 检查是否有相邻的非终结符
                for (int i=0; i<characters.size(); i++){
                    // 两个符号两个符号地进行扫描
                    if (i < characters.size() - 2){
                        String char1 = characters.get(i);
                        String char2 = characters.get(i+1);
                        if (nonEndChars.contains(char1) && nonEndChars.contains(char2)){
                            flag = true;
                            break outer;
                        }
                    }
                }
            }
        }

        return !flag;
    }

    /**
     * 判断文法是否含有空产生式
     * @return
     */
    public boolean isContainsNULLP(){

        boolean flag = false;

        Map<String, String[]> P = grammar.getP();

        outer:for (String[] rightItems : P.values()) {
            // 遍历右部
            for (String item : rightItems) {
                // 分解为符号列表
                List<String> characters = grammar.disassemble(item);
                // 遍历每一个符号
                for (String character : characters){
                    if (character.equals("NULL")){
                        flag = true;
                        break outer;
                    }
                }
            }
        }

        return flag;
    }

    /**
     * 计算FIRSTVT集合
     */
    public void calculateFIRSTVT(){

        FIRSTVT = new HashMap<>();
        Set<String> nonEndChars = grammar.getNonEndChars();

        for (String nonEndChar : nonEndChars){
            FIRSTVT.put(nonEndChar, FIRSTVTx(nonEndChar));
        }
    }

    /**
     * 递归地计算非终结符号x的FIRSTVT集合
     * @param x
     * @return
     */
    private Set<String> FIRSTVTx(String x){

        Set<String> nonEndChars = grammar.getNonEndChars();
        Set<String> endChars = grammar.getEndChars();
        Map<String, String[]> P = grammar.getP();

        Set<String> firstvt = new HashSet<>();

        for (Map.Entry<String, String[]> entry : grammar.getP().entrySet()) {

            String leftItem = entry.getKey();

            // 找到左部为查找的非终结符的那些产生式
            // 否则跳过
            if (!leftItem.equals(x)){
                continue;
            }

            String[] rightItems = entry.getValue();

            // 遍历右部
            for (String item : rightItems) {
                // 分解为符号列表
                List<String> characters = grammar.disassemble(item);
                // 判断长度是否大于2
                if (characters.size() < 2){
                    String character = characters.get(0);
                    // 如果是非终结符，就把其FIRSTVT加入
                    if (nonEndChars.contains(character)){
                        firstvt.addAll(FIRSTVTx(character));
                    }
                    else{
                        firstvt.add(character);
                    }
                }
                else{
                    String character = characters.get(0);
                    if (nonEndChars.contains(character)){
                        firstvt.add(characters.get(1));
                    }
                    else{
                        firstvt.add(character);
                    }
                }
            }
        }

        return firstvt;
    }

    /**
     * 计算LASTVT集合
     */
    public void calculateLASTVT(){

        LASTVT = new HashMap<>();
        Set<String> nonEndChars = grammar.getNonEndChars();

        for (String nonEndChar : nonEndChars){
            LASTVT.put(nonEndChar, LASTVTx(nonEndChar));
        }
    }

    /**
     * 递归地计算非终结符号x的LASTVT集合
     * @param x
     * @return
     */
    private Set<String> LASTVTx(String x){

        Set<String> nonEndChars = grammar.getNonEndChars();
        Set<String> endChars = grammar.getEndChars();
        Map<String, String[]> P = grammar.getP();

        Set<String> lastvt = new HashSet<>();

        for (Map.Entry<String, String[]> entry : grammar.getP().entrySet()) {

            String leftItem = entry.getKey();

            // 找到左部为查找的非终结符的那些产生式
            // 否则跳过
            if (!leftItem.equals(x)){
                continue;
            }

            String[] rightItems = entry.getValue();

            // 遍历右部
            for (String item : rightItems) {
                // 分解为符号列表
                List<String> characters = grammar.disassemble(item);
                // 判断长度是否大于2
                if (characters.size() < 2){
                    String character = characters.get(characters.size() - 1);
                    // 如果是非终结符，就把其LASTVT加入
                    if (nonEndChars.contains(character)){
                        lastvt.addAll(LASTVTx(character));
                    }
                    else{
                        lastvt.add(character);
                    }
                }
                else{
                    String character = characters.get(characters.size() - 1);
                    if (nonEndChars.contains(character)){
                        lastvt.add(characters.get(characters.size() - 2));
                    }
                    else{
                        lastvt.add(character);
                    }
                }
            }
        }

        return lastvt;
    }

    /**
     * 写优先关系矩阵
     * character是左边的符号，其优先级小于vt里的符号
     * @param vt
     * @param character
     * @return
     */
    private boolean writeMatrixL(Set<String> vt, String character){

        int i = index.get(character);

        for (String endChar : vt){
            int j = index.get(endChar);
            if (matrix[i][j] != ""){
                return false;
            }
            else{
                matrix[i][j] = "<";
            }
        }

        return true;
    }

    /**
     * 写优先关系矩阵
     * character是右边的符号，其优先级小于vt里的符号
     * @param vt
     * @param character
     * @return
     */
    private boolean writeMatrixR(Set<String> vt, String character){

        int i = index.get(character);

        for (String endChar : vt){
            int j = index.get(endChar);
            if (matrix[j][i] != ""){
                return false;
            }
            else{
                matrix[j][i] = ">";
            }
        }

        return true;
    }

    /**
     * 填充算符优先关系矩阵并判断文法是否是算符优先文法
     * @return
     */
    private boolean isOPGrammar(){

        // 初始化算符优先关系矩阵
        for (int i=0; i<matrix.length; i++){
            // 因为是方阵，行列都一样
            for (int j=0; j<matrix.length; j++){
                matrix[i][j] = "";
            }
        }

        Map<String, String[]> P = grammar.getP();
        Set<String> endChars = grammar.getEndChars();

        // 遍历所有产生式
        for (String[] rightItems : P.values()) {
            // 遍历右部
            for (String item : rightItems) {
                // 分解为符号列表
                List<String> characters = grammar.disassemble(item);
                // 找到终结符的位置
                for (int i = 0; i < characters.size(); i++) {
                    // 获得当前符号
                    String character = characters.get(i);
                    if (endChars.contains(character)){
                        // 其左边的非终结符的LASTVT集合里的符号优先级都大于当前的终结符
                        if (i > 0){
                            Set<String> lastvt = LASTVT.get(characters.get(i-1));
                            if (! writeMatrixR(lastvt, character)){
                                return false;
                            }
                        }
                        // 其右边的非终结符的FIRSTVT集合里的符号优先级都大于当前的终结符
                        if (i < characters.size() - 1){
                            Set<String> firstvt = FIRSTVT.get(characters.get(i+1));
                            if (! writeMatrixL(firstvt, character)){
                                return false;
                            }
                        }
                        // 处理等号的情况
                        if (i+2 <characters.size()){
                            int row = index.get(character);
                            int column = index.get(characters.get(i+2));
                            matrix[row][column] = "=";
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * 分析函数
     */
    public void analysis(){
        if (isOperatorG() && !isContainsNULLP()){

            calculateFIRSTVT();
            calculateLASTVT();

            if (isOPGrammar()){
                isOPGrammar = true;
            }
            else{
                isOPGrammar = false;
            }
        }
        else{
            isOPGrammar = false;
        }
    }

    public static void main(String[] args) {

        // 非终结符集合
        String[] Vn = {"M", "N"};
        // 终结符集合
        // 这里定义空串为 NULL
        String[] Vt = {"a", "b", "(", ")", ","};
        // 预定义产生式规则集合P
        String[] P = {
                "M->a|b|(N)",
                "N->N,M|M"
        };
        // 开始符号
        String S = "M";

        Grammar grammar = new Grammar(Vn, Vt, P, S);

        OPAnalysis opa = new OPAnalysis(grammar);
        opa.analysis();

        System.out.println();
        System.out.println("该文法是算符优先文法吗？");
        System.out.println(opa.isOPGrammar);

        if (!opa.isOPGrammar){
            return;
        }

        System.out.println();
        System.out.println("FIRSTVT集为：");

        Map<String, Set<String>> FIRSTVT = opa.FIRSTVT;
        for (String character : opa.grammar.getNonEndChars()){
            Set<String> firstvt = FIRSTVT.get(character);
            System.out.println("FIRSTVT(" + character + "): { ");
            for (String value : firstvt){
                System.out.print(value + " ");
            }
            System.out.println(" }");
        }

        System.out.println();
        System.out.println("LASTVT集为：");

        Map<String, Set<String>> LASTVT = opa.LASTVT;
        for (String character : opa.grammar.getNonEndChars()){
            Set<String> lastvt = LASTVT.get(character);
            System.out.println("LASTVT(" + character + "): { ");
            for (String value : lastvt){
                System.out.print(value + " ");
            }
            System.out.println(" }");
        }

        System.out.println();
        System.out.println("其算符优先矩阵如下：");

        System.out.print(" ");
        for (int i=0; i<opa.matrix.length; i++){
            for (Map.Entry<String, Integer> entry : opa.index.entrySet()){
                if (entry.getValue() == i){
                    System.out.printf("%2s", entry.getKey());
                }
            }
        }
        System.out.println();
        for (int i=0; i<opa.matrix.length; i++){
            for (Map.Entry<String, Integer> entry : opa.index.entrySet()){
                if (entry.getValue() == i){
                    System.out.print(entry.getKey());
                }
            }
            for (int j=0; j<opa.matrix.length; j++){
                System.out.printf("%2s",opa.matrix[i][j]);
            }
            System.out.println();
        }
    }
}