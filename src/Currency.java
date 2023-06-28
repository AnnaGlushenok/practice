import Exeption.WrongCurrencyException;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Currency {
    private final static String USD_REGEX = "\\$\\d+(\\.\\d+)?", RUB_REGEX = "\\d+(\\.\\d+)?р",
            RUB_FUNC_REGEX = "toRubles", USD_FUNC_REGEX = "toDollars";
    private final static BigDecimal RUB, USD;
    private BigDecimal val;

    static {
        Properties prop = new Properties();
        try {
            FileInputStream fis = new FileInputStream("prop.properties");
            prop.load(fis);
        } catch (IOException e) {
            System.err.println("Файл не найден");
        }
        RUB = new BigDecimal(prop.getProperty("RUB"));
        USD = new BigDecimal(prop.getProperty("USD"));
    }

    public Currency(String num) {
        this.val = new BigDecimal(num);
    }

    public Currency() {
    }

    public String calculate(String line) throws WrongCurrencyException {
        line = line.replaceAll(" ", "");
        String[] result = splitString(line);
        Stack<String> stack = new Stack<>();
        Currency currency = new Currency();
        for (String res : result) {
            if (res.matches(USD_REGEX + "|" + RUB_REGEX)) {
                stack.add(res);
            } else if (res.equals(RUB_FUNC_REGEX)) {
                stack.add(RUB_FUNC_REGEX);
            } else if (res.equals(USD_FUNC_REGEX)) {
                stack.add(USD_FUNC_REGEX);
            } else if (res.equals("+")) {
                stack.add("+");
            } else if (res.equals("-")) {
                stack.add("-");
            } else if (res.equals(")")) {
                String num = stack.pop();
                String func = stack.pop();
                if (func.matches("[+-]")) {
                    stack.add(func);
                    stack.add(num);
                    break;
                }
                if (func.equals(RUB_FUNC_REGEX)) {
                    if (num.matches("^" + USD_REGEX + "$"))
                        stack.add(toRubles(new Currency(num.replace("$", ""))) + "р");
                    else
                        throw new WrongCurrencyException();
                } else if (func.equals(USD_FUNC_REGEX)) {
                    if (num.matches("^" + RUB_REGEX + "$"))
                        stack.add("$" + toDollars(new Currency(num.replace("р", ""))));
                    else
                        throw new WrongCurrencyException();
                }
            }
        }

        String operation, firstNum, secondNum, curr;
        while (stack.contains("+") || stack.contains("-")) {
            secondNum = stack.pop();
            operation = stack.pop();
            firstNum = stack.pop();
            curr = firstNum.contains("$") ? "$" : "р";
            if (operation.equals("+")) {
                currency = new Currency(firstNum.replace(curr, ""));
                currency.add(secondNum.replace(curr, ""));
            } else if (operation.equals("-")) {
                currency = new Currency(firstNum.replace(curr, ""));
                currency.sub(secondNum.replace(curr, ""));
            } else {
                line = String.join("", stack);
                calculate(line);
            }
            stack.add(curr.equals("$") ? "$" + currency.val.toString() : currency.val.toString() + "р");
        }
        String answer = "";
        if (stack.contains(USD_FUNC_REGEX) || stack.contains(RUB_FUNC_REGEX)) {
            stack.add(1, "(");
            stack.add(")");
            answer = calculate(String.join("", stack));
        }
        answer = answer.matches(USD_REGEX + "|" + RUB_REGEX) ? answer : stack.pop();
        return answer;
    }

    private String[] splitString(String input) {
        Matcher matcher = Pattern.
                compile(USD_FUNC_REGEX + "|" + RUB_FUNC_REGEX + "|" + USD_REGEX + "|" + RUB_REGEX + "|[\\(\\)\\+\\-]")
                .matcher(input.
                        replaceAll("\\(\\s+", "(")
                        .replaceAll("\\s+\\)", ")")
                );
        List<String> matches = new ArrayList<>();
        while (matcher.find())
            matches.add(matcher.group());

        return matches.toArray(new String[0]);
    }

    public void add(String val) {
        add(new BigDecimal(val));
    }

    public void sub(String val) {
        sub(new BigDecimal(val));
    }

    private void add(BigDecimal val) {
        this.val = this.val.add(val);
    }

    private void sub(BigDecimal val) {
        this.val = this.val.subtract(val);
    }

    private BigDecimal toDollars(Currency rub) {
        return USD.multiply(rub.val);
    }

    private BigDecimal toRubles(Currency usd) {
        return RUB.multiply(usd.val);
    }
}
