package top.yeyezhi.hhu.preprocessing.handler.structure;

import java.util.*;
import java.util.regex.*;

/**
 * 这是一个基于“pre/回溯”的极简树构建器：
 * - 识别各种标题正则类型：一、 / （一） / 1. / （1） / 1）/ 1.1 / 1.1.1 / 1.1.1.1 / 1、/ 1
 * - 支持前缀可有 #（如 "# 一、xxx"）
 * - 按照一种算法逻辑：同型=兄弟；回溯找到同型=那个祖先的兄弟；否则=孩子
 * - 提供最长分支提取，用于后续判断结构以匹配具体的模板结构
 *
 * @author yeyezhi
 */
public class SimpleMdOutlineNew {

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

        public final String numRaw;   // ← 新增：原始编号串
        public final int numInt;      // ← 新增：阿拉伯数字（无则 -1）
        public final int[] numSeq;    // ← 新增：点分序列（如 1.2.3 = [1,2,3]；无则 null）

        public Node(HeaderType type, String rawLine, String titleText, int lineNo,
                    String numRaw, int numInt, int[] numSeq) {
            this.type = type;
            this.rawLine = rawLine;
            this.titleText = titleText;
            this.lineNo = lineNo;
            this.numRaw = numRaw;
            this.numInt = numInt;
            this.numSeq = numSeq;
        }

        public Node parent;
        public final List<Node> children = new ArrayList<>();

        @Override public String toString() { return type + " : " + titleText + " (line " + lineNo + ")"; }
    }

    // 3) 行匹配结果
    public static class Match {
        public final HeaderType type;
        public final String title;
        public final String numRaw;   // ← 新增：原始编号串（"1"、"1.1.1"、"一"等）

        public Match(HeaderType type, String title, String numRaw) {
            this.type = type;
            this.title = title;
            this.numRaw = numRaw;
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
                HeaderType.AR_L5_RIGHT,
                HeaderType.AR_L0_PLAIN,
                HeaderType.CN_L1,
                HeaderType.CN_L2_PAREN,
                HeaderType.AR_L4_PAREN,
        };

        for (HeaderType headerType : order) {
            Matcher m = headerType.getPattern().matcher(s);
            if (m.find()){
                String numRaw = m.group(1);   // ← 把编号带回
                String title  = m.group(2);
                return new Match(headerType, title, numRaw);

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

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
    private static int[] parseSeq(String s) {
        if (s == null || !s.contains(".")) return null;
        String[] parts = s.split("\\.");
        int[] r = new int[parts.length];
        for (int i = 0; i < parts.length; i++) r[i] = parseIntSafe(parts[i]);
        return r;
    }
// 如果需要中文数字转阿拉伯，这里也可以接入（略）

    private enum AttachDecision { AS_CHILD_OF_TOP, AS_SIBLING_OF_SAME_TYPE_ANCESTOR, FALLBACK }

    // 判断“当前类型是否常见为深层列表编号”（可按需扩展）
    private static boolean isPotentialDeepList(HeaderType t) {
        return t == HeaderType.AR_DUNHAO || t == HeaderType.AR_L0_PLAIN; // 你若有 AR_L0_PLAIN
    }

    // 栈顶是否属于“更细分的父级”，比如 1.1.1 / 1.1 等
    private static boolean isDeepParent(HeaderType t) {
        return t == HeaderType.AR_MULTI_L3 || t == HeaderType.AR_MULTI_L2 || t == HeaderType.AR_MULTI_L1
                || t == HeaderType.AR_L4_PAREN; // 也可把（1）视作更细父级
    }

    private static AttachDecision decideAttachment(Deque<Node> stack, Node top, Node sameTypeAncestor, Match hit) {
        // 仅在存在“同型祖先”时调用
        // 解析当前编号
        final int curNum = parseIntSafe(hit.numRaw);

        // A) 深层列表优先：在多级数字（或（1））之下再出现 1、，往往是“孩子”
        if (isPotentialDeepList(hit.type) && top != null && top.type != null && isDeepParent(top.type) && curNum == 1) {
            return AttachDecision.AS_CHILD_OF_TOP;
        }

        // B) 祖先同型的顺序递增：判定为兄弟
        if (sameTypeAncestor != null && sameTypeAncestor.numInt >= 1 && curNum >= 1) {
            if (curNum == sameTypeAncestor.numInt + 1) {
                return AttachDecision.AS_SIBLING_OF_SAME_TYPE_ANCESTOR;
            }
            // 若当前编号 <= 祖先编号，也更像“子层重启”，走孩子
            if (curNum <= sameTypeAncestor.numInt) {
                return AttachDecision.AS_CHILD_OF_TOP;
            }
        }

        return AttachDecision.FALLBACK;
    }

    /** 树构建（基于 pre/回溯策略） */
    public static Node buildTree(List<String> lines) {
        Node root = new Node(null, "<ROOT>", "", -1, null, -1, null);
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        boolean inCode = false;

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String t = raw.trim();
            if (t.startsWith("```")) { inCode = !inCode; continue; }
            if (inCode || t.isEmpty()) continue;

            Match hit = matchLine(raw);
            if (hit == null) continue;

            HeaderType T = hit.type;
            Node top = stack.peek();
            if (top == null) top = root;

            // 构造当前节点（先把编号解析好）
            int numInt = -1;
            int[] numSeq = null;
            if (hit.numRaw != null) {
                if (T == HeaderType.AR_MULTI_L1 || T == HeaderType.AR_MULTI_L2 || T == HeaderType.AR_MULTI_L3) {
                    numSeq = parseSeq(hit.numRaw);
                } else {
                    numInt = parseIntSafe(hit.numRaw);
                }
            }
            Node node = new Node(T, raw, hit.title, i+1, hit.numRaw, numInt, numSeq);

            // 1) 与 pre 同型 → 兄弟（保持你原有逻辑）
            if (top.type == T) {
                stack.pop();
                Node parent = stack.peek();
                attach(parent, node);
                stack.push(node);
                continue;
            }

            // 2) 回溯找同型祖先，但先用“编号语义”判定
            Node cur = top, sameTypeAncestor = null;
            while (cur != null && cur.type != null) {
                if (cur.type == T) { sameTypeAncestor = cur; break; }
                cur = cur.parent;
            }
            if (sameTypeAncestor != null) {
                AttachDecision d = decideAttachment(stack, top, sameTypeAncestor, hit);
                if (d == AttachDecision.AS_CHILD_OF_TOP) {
                    attach(top, node);
                    stack.push(node);
                    continue;
                } else if (d == AttachDecision.AS_SIBLING_OF_SAME_TYPE_ANCESTOR) {
                    while (stack.peek() != sameTypeAncestor) stack.pop();
                    stack.pop(); // 弹掉祖先自身
                    Node parent = stack.peek();
                    attach(parent, node);
                    stack.push(node);
                    continue;
                }
                // FALLBACK → 走你原有的“同型祖先=兄弟”
                while (stack.peek() != sameTypeAncestor) stack.pop();
                stack.pop();
                Node parent = stack.peek();
                attach(parent, node);
                stack.push(node);
                continue;
            }

            // 3) 无同型祖先 → 孩子（保持原逻辑）
            attach(top, node);
            stack.push(node);
        }
        return root;
    }


    private static void attach(Node parent, Node child) {
        child.parent = parent;
        parent.children.add(child);
    }

    /** 提取所有最长分支（用于判断结构）。返回多个分支，每个分支是 HeaderType 列表。 */
    public static List<List<HeaderType>> longestBranches(Node root) {
        List<List<HeaderType>> results = new ArrayList<>();
        List<HeaderType> path = new ArrayList<>();
        int[] max = new int[]{0}; // 用数组允许在 lambda/递归间修改
        dfsAll(root, path, results, max);
        return results;
    }

    private static void dfsAll(Node node, List<HeaderType> path,
                               List<List<HeaderType>> results, int[] max) {

        if (node.type != null)
            path.add(node.type);

        if (node.children.isEmpty()) {
            int size = path.size();
            if (size > max[0]) {
                // 找到更长分支 → 重置
                max[0] = size;
                results.clear();
                results.add(new ArrayList<>(path));
            } else if (size == max[0]) {
                // 相同长度 → 追加
                results.add(new ArrayList<>(path));
            }
        } else {
            for (Node c : node.children) dfsAll(c, path, results, max);
        }

        if (node.type != null)
            path.remove(path.size() - 1);
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
