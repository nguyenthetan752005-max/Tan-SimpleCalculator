package com.example.mycalculator2;

public class Calculator {
    // Thực hiện phép tính dựa trên hai số và toán tử
    // Performs calculation based on two numbers and an operator
    public double calculate(double num1, double num2, String operator) {
        switch (operator) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "x":
                return num1 * num2;
            case "/":
                if (num2 == 0) return 0; // Tránh lỗi chia cho 0 / Avoid division by zero
                return num1 / num2;
            default:
                return num2;
        }
    }
}