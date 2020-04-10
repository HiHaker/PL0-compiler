import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2020/4/10 0010
 * BY Jianlong
 */
public class LexicalAnalysis {

    // 经过预处理之后的源程序长度
    private int length;
    // 每一次扫描的起始位置
    private int begin;
    // 存储关键字和运算符和界符表
    Map<String, Integer> keyWords;

    // 内部类
    private class Token{
        // 单词的值
        public String value;
        // 种别码
        public Integer syn;

        public Token(String value, Integer syn) {
            this.value = value;
            this.syn = syn;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "value='" + value + '\'' +
                    ", syn=" + syn +
                    '}';
        }
    }

    public LexicalAnalysis(){
        keyWords = new HashMap<>();
        keyWords.put("begin", 1);
        keyWords.put("end", 2);
        keyWords.put("if", 3);
        keyWords.put("then", 4);
        keyWords.put("while", 5);
        keyWords.put("do", 6);
        keyWords.put("const", 7);
        keyWords.put("var", 8);
        keyWords.put("call", 9);
        keyWords.put("procedure", 10);
        keyWords.put("odd", 11);
        keyWords.put("+", 14);
        keyWords.put("-", 15);
        keyWords.put("*", 16);
        keyWords.put("/", 17);
        keyWords.put(":=", 18);
        keyWords.put("<", 19);
        keyWords.put("<=", 20);
        keyWords.put(">", 21);
        keyWords.put(">=", 22);
        keyWords.put("<>", 23);
        keyWords.put("=", 24);
        keyWords.put("#", 25);
        keyWords.put(",", 26);
        keyWords.put(";", 27);
        keyWords.put("(", 28);
        keyWords.put(")", 29);
        keyWords.put(":", 30);
    }

    // getter方法


    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getLength(){
        return this.length;
    }

    // 首先，对输入源程序做预处理，处理掉注释、换行符、制表符等等
    public char[] preProcess(char[] source) throws IllegalArgumentException{
        // 构造一个临时数组存储预处理后的源程序
        char[] temp = new char[8000];
        int count = 0;

        // 逐个扫描源程序中的字符
        outLoop: for (int i=0; i<source.length; i++){
            switch (source[i]){
                // 去除单行注释
                case '/':
                    if (source[i+1] == '/'){
                        // 跳过单行注释
                        i = i+2;
                        // 跳过这一行，直到遇到回车换行
                        while (source[i] != '\n'){
                            i++;
                        }
                    } else{
                        temp[count++] = source[i];
                    }
                    break;
                // 去除多行注释
                case '(':
                    if (source[i+1] == '*'){
                        // 跳过多行注释符号
                        i = i+2;
                        // 当不满足匹配多行注释的时候，跳过
                        while (!(source[i] == '*' && source[i+1] == ')')){
                            i++;
                            // 检查有没有到程序的末尾
                            if (i >= source.length -2){
                                throw new IllegalArgumentException("源程序的注释不匹配!");
                            }
                        }
                        // 跳过多行注释符号
                        i = i+2;
                    } else{
                        temp[count++] = source[i];
                    }
                case '\n':
                case '\t':
                case '\r':
                    i++;
                    break;
                // 程序结束标志
                case '.':
                    temp[count++] = source[i];
                    break outLoop;
                default:
                    temp[count++] = source[i];
            }
        }

        // 设置源程序的长度为count
        this.length = count;
        return temp;
    }

    // 识别单词符号
    public Token scanner(char[] code, int begin){
        // 我们知道，要识别的PL/0语言中的单词符号有这几个类别：
        // 保留字、标识符、常数、运算符和界符
        // 可以进一步粗分为3类，字母开头（保留字和标识符）数字开头（常数）其他

        // 存储当前识别的单词符号
        char[] token = new char[20];
        // 指向程序的索引
        int index1 = begin;
        // 指向存储单词的数组的索引
        int index2 = 0;

        // 除去单词前的空格
        while (code[index1] == ' '){
            index1++;
        }

        // 如果单词的首字符为字母
        if (Character.isLetter(code[index1])){
            // 当后续为字母或数字时存入
            while (Character.isLetter(code[index1]) || Character.isDigit(code[index1])){
                token[index2++] = code[index1++];
            }
            // 在map中查找，如果能查找到说明是保留字
            // 否则说明是标识符
            // 每次返回都要维护下index1
            this.setBegin(index1);
            return new Token(String.valueOf(token).trim(), this.keyWords.getOrDefault(String.valueOf(token).trim(), 12));

        } else if (Character.isDigit(code[index1])){
            // 如果首字符是数字
            while (Character.isDigit(code[index1])){
                token[index2++] = code[index1++];
            }
            this.setBegin(index1);
            return new Token(String.valueOf(token).trim(), 13);
        } else {
            switch (code[index1]){
                case ':':
                    if (code[index1 + 1] == '='){
                        this.setBegin(index1+2);
                        return new Token(":=", this.keyWords.get(":="));
                    } else {
                        this.setBegin(index1+1);
                        return new Token(":", this.keyWords.get(":"));
                    }
                case '<':
                    if (code[index1 + 1] == '='){
                        this.setBegin(index1+2);
                        return new Token("<=", this.keyWords.get("<="));
                    } else if(code[index1 + 1] == '>') {
                        this.setBegin(index1+2);
                        return new Token("<>", this.keyWords.get("<>"));
                    } else{
                        this.setBegin(index1+1);
                        return new Token("<", this.keyWords.get("<"));
                    }
                case '>':
                    if (code[index1 + 1] == '='){
                        this.setBegin(index1+2);
                        return new Token(">=", this.keyWords.get(">="));
                    } else{
                        this.setBegin(index1+1);
                        return new Token(">", this.keyWords.get(">"));
                    }
                case '+':
                case '-':
                case '*':
                case '/':
                case '=':
                case '#':
                case ',':
                case ';':
                case '(':
                case ')':
                    this.setBegin(index1+1);
                    return new Token(String.valueOf(code[index1]), this.keyWords.get(String.valueOf(code[index1])));
                case '.':
                    return new Token(".", 0);
                default:
                    return new Token("noneType", -1);
            }
        }

    }

    public static void main(String[] args) {
        char[] source = new char[8000];
        LexicalAnalysis la = new LexicalAnalysis();
        // 开启文件流，读入文件
        try(BufferedReader reader = new BufferedReader(new FileReader("./src/source.txt"));){
            // 读入源程序
            reader.read(source);
            // 对源程序做预处理
            source = la.preProcess(source);
            // 开始识别单词

        } catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("源程序为：");

        for (int i=0; i<la.getLength(); i++){
            System.out.print(source[i]);
        }

        System.out.println();

        System.out.println("编码如下：");
        for (Map.Entry<String, Integer> entry : la.keyWords.entrySet()){
            System.out.println("Value: " + entry.getKey() + ", Syn: " + entry.getValue());
        }

        System.out.println("识别出的单词有：");

        Token tempToken = la.scanner(source, 0);

        while (tempToken.syn != 0){
            System.out.println(tempToken);
            tempToken = la.scanner(source, la.getBegin());
        }
    }
}