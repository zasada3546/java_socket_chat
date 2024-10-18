public class Calculator {

  
    public static int sum(int a, int b) {
        return a + b;
    
    public static int subtract(int a, int b) {
        return a - b;
    }

 
    public static int multiply(int a, int b) {
        return a * b;
    }


    public static double divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Деление на ноль невозможно");
        }
        return (double) a / b;
    }

    public static void main(String[] args) {
        int num1 = 10;
        int nu


