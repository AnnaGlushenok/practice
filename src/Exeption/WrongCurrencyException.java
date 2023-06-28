package Exeption;

public class WrongCurrencyException extends Exception{
    public WrongCurrencyException() {
        super("Неверная валюта.");
    }
}
