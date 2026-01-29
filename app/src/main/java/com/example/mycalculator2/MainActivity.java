package com.example.mycalculator2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay, tvHistory;
    private String currentInput = "";
    private String operator = "";
    private double firstValue = Double.NaN;
    private Calculator calculator;

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
                R.id.btnEqual, R.id.btnClear, R.id.btnDelete
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(view -> {
                Button b = (Button) view;
                String text = b.getText().toString();
                processClick(text);
            });
        }
    }

    private void processClick(String text) {
        if (text.matches("[0-9.]")) {
            handleNumber(text);
        } else if (text.equals("C")) {
            handleClear();
        } else if (text.equals("DEL")) {
            handleDelete();
        } else if (text.equals("=")) {
            handleEqual();
            return; // Không chạy updateDisplay() vì đã tự xử lý trong handleEqual
        } else {
            handleOperator(text);
        }
        updateDisplay();
    }

    private void handleNumber(String text) {
        currentInput += text;
    }

    private void handleClear() {
        currentInput = "";
        firstValue = Double.NaN;
        operator = "";
        tvHistory.setText("");
    }

    private void handleDelete() {
        if (currentInput.length() > 0) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        }
    }

    private void handleOperator(String op) {
        if (!currentInput.isEmpty()) {
            if (!Double.isNaN(firstValue)) {
                double secondValue = Double.parseDouble(currentInput);
                firstValue = calculator.calculate(firstValue, secondValue, operator);
            } else {
                firstValue = Double.parseDouble(currentInput);
            }
            operator = op;
            currentInput = "";
        } else if (!Double.isNaN(firstValue)) {
            operator = op;
        }
    }

    private void handleEqual() {
        if (!Double.isNaN(firstValue) && !currentInput.isEmpty()) {
            double secondValue = Double.parseDouble(currentInput);
            double result = calculator.calculate(firstValue, secondValue, operator);
            
            // Hiển thị phép tính ở tvHistory và kết quả ở tvDisplay
            tvHistory.setText(formatDouble(firstValue) + " " + operator + " " + currentInput + " =");
            tvDisplay.setText(formatDouble(result));
            
            firstValue = result;
            currentInput = "";
            operator = "";
        }
    }

    private void updateDisplay() {
        // Cập nhật dòng phép tính nhỏ phía trên
        if (!Double.isNaN(firstValue)) {
            tvHistory.setText(formatDouble(firstValue) + " " + operator);
        } else {
            tvHistory.setText("");
        }
        
        // Cập nhật dòng số đang nhập to phía dưới
        tvDisplay.setText(currentInput.isEmpty() ? "0" : currentInput);
    }

    private String formatDouble(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.valueOf(d);
    }
}