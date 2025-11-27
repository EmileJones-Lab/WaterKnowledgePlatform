package top.yeyezhi.hhu.preprocessing.handler.structure;

import java.util.*;
import java.util.regex.*;

/**
 * 这是一个基于“pre/回溯”的极简树构建器：
 * - 识别各种标题正则类型：一、 / （一） / 1. / （1） / 1）/ 1.1 / 1.1.1 / 1.1.1.1 / 1、
 * - 支持前缀可有 #（如 "# 一、xxx"）
 * - 按照一种算法逻辑：同型=兄弟；回溯找到同型=那个祖先的兄弟；否则=孩子
 * - 提供最长分支提取，用于后续判断结构以匹配具体的模板结构
 *
 * @author yeyezhi
 */
public class SimpleMdOutline {

    // 1) HeaderType 枚举是为了匹配不同类型的标题格式（比如中文数字编号、阿拉伯数字编号等），
    //    每种枚举常量都代表一种标题格式，并且内部保存了一个对应的正则表达式模式
    public enum HeaderType {
        CN_L1("^(?:#+\\s*)?([一二三四五六七八九十百千零〇两]{1,4})[、\\.．。]\\s*(.+)$"),       // 一、xxx
        CN_L2_PAREN("^(?:#+\\s*)?（([一二三四五六七八九十百千零〇两]{1,4})）\\s*(.+)$"),       // （一）xxx
        AR_L0_PLAIN("^(?:#+\\s*)?([1-9])\\s*(?![0-9])(.+)$"),                               // 1xxx
        AR_L3_DOT("^(?:#+\\s*)?([0-9]{1,3})[\\.．。]\\s*(.+)$"),                        // 1. xxx
        AR_MULTI_L1("^(?:#+\\s*)?([0-9]+\\.[0-9]+)\\s*(.+)$"),                           // 1.1
        AR_MULTI_L2("^(?:#+\\s*)?([0-9]+\\.[0-9]+\\.[0-9]+)\\s*(.+)$"),                  // 1.1.1
        AR_MULTI_L3("^(?:#+\\s*)?([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)\\s*(.+)$"),         // 1.1.1.1
        AR_DUNHAO("^(?:#+\\s*)?([0-9]{1,3})、\\s*(.+)$"),                                // 1、xxx
        AR_L4_PAREN("^(?:#+\\s*)?（([0-9]{1,3})）\\s*(.+)$"),                             // （1）xxx
        AR_L5_RIGHT("^(?:#+\\s*)?([0-9]{1,3})）\\s*(.+)$");                              // 1）xxx

        private final Pattern pattern; // 正则表达式属性
        // 枚举构造函数接收String类型的regex字符串，并立即编译为Pattern
        HeaderType(String regex) {
            pattern = Pattern.compile(regex);
        }

        // Getter 方法
        public Pattern getPattern(){
            return pattern;
        }
    }

    // 2) 节点
    public static class Node {
        public final HeaderType type;
        public final String rawLine;      // 原始行（可用于后续纠正）
        public final String titleText;    // 去掉编号后的标题文本（简单提取）
        public final int lineNo;
        public Node parent;
        public final List<Node> children = new ArrayList<>();

        public Node(HeaderType type, String rawLine, String titleText, int lineNo) {
            this.type = type;
            this.rawLine = rawLine;
            this.titleText = titleText;
            this.lineNo = lineNo;
        }

        @Override public String toString() { return type + " : " + titleText + " (line " + lineNo + ")"; }
    }

    // 3) 行匹配结果
    public static class Match {
        public final HeaderType type;
        public final String title;

        public Match(HeaderType type, String title) {
            this.type = type;
            this.title = title;
        }
    }

    /** 识别一行是否为标题（返回类型与标题文本）；非标题返回 null */
    public static Match matchLine(String line) {
        String s = normalize(line);
        // 按“最长匹配优先”的顺序硬编码遍历
        HeaderType[] order = {
                HeaderType.AR_MULTI_L3,
                HeaderType.AR_MULTI_L2,
                HeaderType.AR_MULTI_L1,
                HeaderType.AR_DUNHAO,
                HeaderType.AR_L3_DOT,
                HeaderType.AR_L0_PLAIN,
                HeaderType.CN_L1,
                HeaderType.CN_L2_PAREN,
                HeaderType.AR_L4_PAREN,
                HeaderType.AR_L5_RIGHT
        };

        for (HeaderType headerType : order) {
            Matcher m = headerType.getPattern().matcher(s);
            if (m.find()){
                return new Match(headerType,m.group(2));
            }
        }
        return null;
    }

    // 简单归一化（不动内容，以免影响后续纠正）
    private static String normalize(String s) {
        if (s == null) return "";
        return s.replace('\u00A0',' ')  // NBSP
                .replace("\t"," ")
                .replaceAll(" +", " ")
                .trim();
    }

    /** 树构建（基于你的 pre/回溯策略） */
    public static Node buildTree(List<String> lines) {
        Node root = new Node(null, "<ROOT>", "", -1);
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);

        boolean inCode = false;

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String t = raw.trim();
            if (t.startsWith("```")) { inCode = !inCode; continue; }
            if (inCode || t.isEmpty()) continue;

            Match hit = matchLine(raw);
            if (hit == null) continue; // 非标题行，先忽略（后续需要可挂载为内容）

            HeaderType T = hit.type;
            Node top = stack.peek(); // 当前“pre”所在节点（栈顶）
            if (top == null) top = root;

            // 1) 若与 pre 同类型 → 兄弟：弹出 pre，挂到 pre 的父亲下
            if (top.type == T) {
                stack.pop();                 // 弹掉 pre
                Node parent = stack.peek();  // pre 的父
                Node node = new Node(T, raw, hit.title, i+1);
                attach(parent, node);
                stack.push(node);
                continue;
            }

            // 2) 从 pre 回溯找同类型 → 作为那个祖先的兄弟（挂到祖先的父之下）
            Node cur = top;
            boolean attached = false;
            while (cur != null && cur.type != null) {
                if (cur.type == T) {
                    // 找到祖先同型：弹栈到该祖先
                    while (stack.peek() != cur) stack.pop();
                    stack.pop(); // 再弹出祖先本身，顶端变成祖先的父
                    Node parent = stack.peek();
                    Node node = new Node(T, raw, hit.title, i+1);
                    attach(parent, node);
                    stack.push(node);
                    attached = true;
                    break;
                }
                cur = cur.parent;
            }
            if (attached) continue;

            // 3) 既不是同型，也无法在祖先找到同型 → 作为 pre 的孩子
            Node node = new Node(T, raw, hit.title, i+1);
            attach(top, node);
            stack.push(node);
        }
        return root;
    }

    private static void attach(Node parent, Node child) {
        child.parent = parent;
        parent.children.add(child);
    }

    /** 提取最长分支（用于判断结构）。返回分支上的 HeaderType 列表（不含 root）。 */
    public static List<HeaderType> longestBranch(Node root) {
        List<HeaderType> best = new ArrayList<>();
        dfs(root, new ArrayList<>(), best);
        return best;
    }

    private static void dfs(Node node, List<HeaderType> path, List<HeaderType> best) {
        if (node.type != null) path.add(node.type); // 跳过 root 的 null
        if (node.children.isEmpty()) {
            if (path.size() > best.size()) {
                best.clear();
                best.addAll(path);
            }
        } else {
            for (Node c : node.children) dfs(c, path, best);
        }
        if (node.type != null) path.remove(path.size()-1);
    }

    /** 调试用：打印树 */
    public static void printTree(Node root) {
        printNode(root, 0);
    }

    private static void printNode(Node n, int depth) {
        if (n.type != null) {
            System.out.printf("%s- %s  %s  (line %d)%n",
                    "  ".repeat(Math.max(depth - 1, 0)),
                    n.type, n.titleText, n.lineNo);
        }
        for (Node c : n.children) printNode(c, depth+1);
    }

}
