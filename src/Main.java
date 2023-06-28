import Exeption.WrongCurrencyException;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws WrongCurrencyException {
        Scanner scan = new Scanner(System.in);
        Currency currency = new Currency();
        System.out.println(currency.calculate(scan.nextLine()));
    }
}
