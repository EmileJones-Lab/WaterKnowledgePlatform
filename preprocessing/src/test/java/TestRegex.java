import top.yeyezhi.hhu.preprocessing.structure.AbstractTitleTreeExtractor;
import top.yeyezhi.hhu.preprocessing.structure.TitleTreeExtractor;
import top.yeyezhi.hhu.preprocessing.structure.enums.TitleType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {
    public static void main(String[] args) {
//        Pattern compile = Pattern.compile(TitleType.TYPE_NUMBER_PLAIN.getTitleRegex());
//        Matcher matcher = compile.matcher("2.1 水工建筑物及金属结构安全运用条件");
//        System.out.println(matcher.find());
        AbstractTitleTreeExtractor e = new TitleTreeExtractor();
        String extract = e.extract("aaa");

    }
}
