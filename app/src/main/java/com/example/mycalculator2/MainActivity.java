package com.example.mycalculator2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay, tvHistory;
    private String currentInput = "";
    private String operator = "";
    private double firstValue = Double.NaN;
    private Calculator calculator;
    private final double PI = Math.PI;
    private boolean isResult = false; 
    private boolean isError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);
        calculator = new Calculator();

        setButtonClickListeners();
    }

    private void setButtonClickListeners() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv,
                R.id.btnEqual, R.id.btnClear, R.id.btnDelete, R.id.btnPi, R.id.btnSign, R.id.btnSq, R.id.btnSqrt, R.id.btnPercent
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(view -> {
                if (isError) handleClear();
                Button b = (Button) view;
                String text = b.getText().toString();
                processClick(text);
            });
        }
    }

    private void processClick(String text) {
        if (text.matches("[0-9]")) {
            handleNumber(text);
        } else if (text.equals(".")) {
            handleDot();
        } else if (text.equals("π")) {
            handlePi();
        } else if (text.equals("+/-")) {
            handleSign();
        } else if (text.equals("x²")) {
            handleSquare();
            return;
        } else if (text.equals("√")) {
            handleSqrt();
            return;
        } else if (text.equals("%")) {
            handlePercent();
            return;
        } else if (text.equals("C")) {
            handleClear();
        } else if (text.equals("DEL")) {
            handleDelete();
        } else if (text.equals("=")) {
            handleEqual();
            return; 
        } else {
            handleOperator(text);
        }
        updateDisplay();
    }

    private void handleNumber(String text) {
        if (isResult) {
            if (currentInput.equals(String.valueOf(PI))) {
                firstValue = PI;
                operator = "x";
            }
            currentInput = "";
            isResult = false;
        }
        currentInput += text;
    }

    private void handleDot() {
        if (isResult) {
            currentInput = "0";
            isResult = false;
        }
        if (currentInput.contains(".")) return;
        if (currentInput.isEmpty()) currentInput = "0";
        currentInput += ".";
    }

    private void handlePi() {
        if (!currentInput.isEmpty() && !isResult) {
            double val = Double.parseDouble(currentInput);
            currentInput = String.valueOf(calculator.calculate(val, PI, "x"));
        } else {
            double val = isResult ? Double.parseDouble(currentInput) : 1.0;
            if (isResult) {
                currentInput = String.valueOf(calculator.calculate(val, PI, "x"));
            } else {
                currentInput = String.valueOf(PI);
            }
        }
        isResult = true;
    }

    private void handleSign() {
        if (currentInput.isEmpty() || currentInput.equals("0")) return;
        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1);
        } else {
            currentInput = "-" + currentInput;
        }
    }

    private void handleSquare() {
        String valStr = currentInput.isEmpty() ? "0" : currentInput;
        double val = Double.parseDouble(valStr);
        double res = calculator.calculateUnary(val, "sq");
        
        String history = (Double.isNaN(firstValue) ? "" : formatDouble(firstValue) + " " + operator + " ") + formatDouble(val) + "² =";

        if (!Double.isNaN(firstValue)) {
            String binaryError = calculator.getErrorMessage(firstValue, res, operator);
            if (binaryError != null) { handleError(binaryError, history); return; }
            currentInput = String.valueOf(calculator.calculate(firstValue, res, operator));
            firstValue = Double.NaN;
            operator = "";
        } else {
            currentInput = String.valueOf(res);
        }
        tvHistory.setText(history);
        isResult = true;
        updateDisplay();
    }

    private void handleSqrt() {
        String valStr = currentInput.isEmpty() ? "0" : currentInput;
        double val = Double.parseDouble(valStr);
        String history = (Double.isNaN(firstValue) ? "" : formatDouble(firstValue) + " " + operator + " ") + "√" + formatDouble(val) + " =";

        String unaryError = calculator.getUnaryErrorMessage(val, "sqrt");
        if (unaryError != null) { handleError(unaryError, history); return; }

        double res = calculator.calculateUnary(val, "sqrt");
        
        if (!Double.isNaN(firstValue)) {
            String binaryError = calculator.getErrorMessage(firstValue, res, operator);
            if (binaryError != null) { handleError(binaryError, history); return; }
            currentInput = String.valueOf(calculator.calculate(firstValue, res, operator));
            firstValue = Double.NaN;
            operator = "";
        } else {
            currentInput = String.valueOf(res);
        }
        tvHistory.setText(history);
        isResult = true;
        updateDisplay();
    }

    private void handlePercent() {
        String valStr = currentInput.isEmpty() ? "0" : currentInput;
        double val = Double.parseDouble(valStr);
        String history = (Double.isNaN(firstValue) ? "" : formatDouble(firstValue) + " " + operator + " ") + formatDouble(val) + "% =";

        double res = calculator.calculateUnary(val, "percent");

        if (!Double.isNaN(firstValue)) {
            String binaryError = calculator.getErrorMessage(firstValue, res, operator);
            if (binaryError != null) { handleError(binaryError, history); return; }
            currentInput = String.valueOf(calculator.calculate(firstValue, res, operator));
            firstValue = Double.NaN;
            operator = "";
        } else {
            currentInput = String.valueOf(res);
        }
        tvHistory.setText(history);
        isResult = true;
        updateDisplay();
    }

    private void handleOperator(String op) {
        if (currentInput.isEmpty()) {
            if (Double.isNaN(firstValue)) {
                firstValue = 0.0;
            }
            operator = op;
        } else {
            if (!Double.isNaN(firstValue)) {
                double secondValue = Double.parseDouble(currentInput);
                String history = formatDouble(firstValue) + " " + operator + " " + formatDouble(secondValue) + " =";
                String error = calculator.getErrorMessage(firstValue, secondValue, operator);
                if (error != null) { handleError(error, history); return; }
                firstValue = calculator.calculate(firstValue, secondValue, operator);
            } else {
                firstValue = Double.parseDouble(currentInput);
            }
            operator = op;
            currentInput = "";
            isResult = false;
        }
    }

    private void handleEqual() {
        if (!Double.isNaN(firstValue) && !currentInput.isEmpty()) {
            double secondValue = Double.parseDouble(currentInput);
            String history = formatDouble(firstValue) + " " + operator + " " + formatDouble(secondValue) + " =";
            String error = calculator.getErrorMessage(firstValue, secondValue, operator);
            if (error != null) { handleError(error, history); return; }
            
            double result = calculator.calculate(firstValue, secondValue, operator);
            tvHistory.setText(history);
            currentInput = String.valueOf(result);
            firstValue = Double.NaN;
            operator = "";
            isResult = true;
            updateDisplay();
        }
    }

    private void handleError(String message, String history) {
        tvHistory.setText(history);
        tvDisplay.setText(message);
        isError = true;
    }

    private void handleClear() {
        currentInput = "";
        firstValue = Double.NaN;
        operator = "";
        isResult = false;
        isError = false;
        tvHistory.setText("");
    }

    private void handleDelete() {
        if (isResult || currentInput.length() <= 1) {
            currentInput = "";
            isResult = false;
        } else {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        }
    }

    private void updateDisplay() {
        if (isError) return;
        if (operator.length() > 0 || (Double.isNaN(firstValue) && !isResult)) {
            if (!Double.isNaN(firstValue)) {
                tvHistory.setText(formatDouble(firstValue) + " " + operator);
            } else {
                tvHistory.setText("");
            }
        }

        String toDisplay = currentInput.isEmpty() ? "0" : currentInput;
        if (isResult && !currentInput.isEmpty()) {
            toDisplay = formatDouble(Double.parseDouble(currentInput));
        }
        tvDisplay.setText(toDisplay);
    }

    private String formatDouble(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            try {
                BigDecimal bd = new BigDecimal(Double.toString(d));
                bd = bd.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
                return bd.toPlainString();
            } catch (Exception e) {
                return String.valueOf(d);
            }
    }
}