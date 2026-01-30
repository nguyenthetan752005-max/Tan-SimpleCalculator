package com.example.mycalculator2;

public class Calculator {
    
    // Phép tính 2 ngôi
    public double calculate(double num1, double num2, String operator) {
        switch (operator) {
            case "+": return num1 + num2;
            case "-": return num1 - num2;
            case "x": return num1 * num2;
            case "/": return num1 / num2;
            default: return num2;
        }
    }

    // Phép tính 1 ngôi (Căn bậc 2, bình phương, phần trăm)
    public double calculateUnary(double num, String type) {
        switch (type) {
            case "sqrt": return Math.sqrt(num);
            case "sq": return num * num;
            case "percent": return num / 100.0;
            default: return num;
        }
    }

    // Kiểm tra lỗi phép tính 2 ngôi
    public String getErrorMessage(double num1, double num2, String operator) {
        if (operator.equals("/") && num2 == 0) {
            return "Error: Div by 0";
        }
        return null; 
    }

    // Kiểm tra lỗi phép tính 1 ngôi
    public String getUnaryErrorMessage(double num, String type) {
        if (type.equals("sqrt") && num < 0) {
            return "Error: Invalid Input";
        }
        return null;
    }
}