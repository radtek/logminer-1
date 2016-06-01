package logminerplus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
	}
	
	/**
	 * 在字符串包含验证时
	 */
	public static void test1() {
		/**
		 * ^	表示为限制开头，以Java开头
		 * .*	表示0个以上任意字符
		 * $	表示为限制结束，以。结束
		 */
		String regex = "^Java.*。$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher("Java是个好东西。");
		boolean bool = matcher.matches();
		System.out.println("是否包含："+bool);
		System.out.println();
	}
	
	/**
	 * 以多条件分割字符串时
	 */
	public static void test2() {
		/**
		 * []	表示特定条件
		 */
		String regex = "[, |]+";
		Pattern pattern = Pattern.compile(regex);
		String[] strs = pattern.split("Java Hello World  Java,Hello,,World|Sun");
		for (int i = 0; i < strs.length; i++) {
			System.out.println(strs[i]);
		}
		System.out.println();
	}

	/**
	 * 文字替换
	 */
	public static void test3() {
		Pattern pattern = Pattern.compile("Java正则表达式");
		Matcher matcher = pattern.matcher("Java正则表达式 Hello World,Java正则表达式 Hello World");
		System.out.println("首个替代："+matcher.replaceFirst("Java"));
		System.out.println("全部替代："+matcher.replaceAll("Java"));
		System.out.println();
	}
	
	/**
	 * 文字替换（置换字符）
	 */
	public static void test4() {
		Pattern pattern = Pattern.compile("Java正则表达式");
		Matcher matcher = pattern.matcher("Java正则表达式 Hello World,Java正则表达式 Hello World");
		StringBuffer sbr = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sbr, "Java正则");
			System.out.println(sbr.toString());
		}
		matcher.appendTail(sbr);
		System.out.println(sbr.toString());
		System.out.println();
	}

	/**
	 * 验证是否为邮箱地址
	 */
	public static void test5() {
		Pattern pattern = Pattern.compile("");
		Matcher matcher = pattern.matcher("yudongya@ane56.com");
		System.out.println("是否邮箱：" + matcher.matches());
		System.out.println();
	}

	public static void test6() {
		
	}
	
}
