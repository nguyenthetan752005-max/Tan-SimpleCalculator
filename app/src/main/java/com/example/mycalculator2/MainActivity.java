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
    //Biến chỉ con số hiện tại đang nhập
    private String operator = "";
    //Dấu để xử lý biểu thức +-*/ 
    private double firstValue = Double.NaN;
    //Gía trị đầu của biểu thức, ví dụ sau khi nhập 1+2, thì firstValue là 1

    private Calculator calculator;
    //Khởi tạo 1 lớp Calculator để xử lý phép tính
    private final double PI = Math.PI;
    private boolean isResult = false; 
    //Biến kiểm tra xem có phải là kết quả tính toán hay không, 
    // dùng để hỗ trợ hiển thị giao diện
    private boolean isError = false;
    //Check lỗi, hỗ trợ hiển thị lỗi và hỗ trợ dọn dẹp màn hình



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);
        calculator = new Calculator();

        setButtonClickListeners();
    }
    
    //Gán sự kiện cho tất cả nút
    private void setButtonClickListeners() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv,
                R.id.btnEqual, R.id.btnClear, R.id.btnDelete, R.id.btnPi, 
                R.id.btnSign, R.id.btnSq, R.id.btnSqrt, R.id.btnPercent
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(view -> {

                //Tất cả các nút đều sẽ clear màn hình nếu có lỗi
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
        } else if (text.equals("√")) {
            handleSqrt();
        } else if (text.equals("%")) {
            handlePercent();
        } else if (text.equals("C")) {
            handleClear();
        } else if (text.equals("DEL")) {
            handleDelete();
        } else if (text.equals("=")) {
            handleEqual();
        } else {
            handleOperator(text);
        }
        //Update màn hình hiển thị mỗi lần
        updateDisplay();
    }

    private void handleNumber(String text) {
        // Nếu vừa ra kết quả, ấn số thì hệ thống sẽ hiểu là muốn
        // tạo biểu thức mới
        if (isResult) {
            //Input cũ trở nên rỗng
            currentInput = "";
            isResult = false;
        }
        // Chặn nhập số vào sau Pi (người dùng phải bấm phép tính trước)
        if (currentInput.equals(String.valueOf(PI))) return;

        //Thêm số mới nhập vào input
        currentInput += text;
    }
    
    //Xử lý dấu .
    private void handleDot() {
        // Nếu đang isResult, ấn . sẽ ngầm hiểu là tạo số mới "0.gì đó"
        if (isResult) {
            currentInput = "0";
            isResult = false;
        }
        // Chặn dấu chấm sau Pi
        if (currentInput.equals(String.valueOf(PI))) return;

        //Chỉ cho phép 1 dấu "."
        if (currentInput.contains(".")) return;

        //Nếu trước dấu . không có gì thì ngầm định là "0.gì đó", thêm 0 đằng trước
        if (currentInput.isEmpty()) currentInput = "0";
        currentInput += ".";
        //Nhờ có lệnh Double.parseDouble
        //Mà các trường hợp không có số sau dấu .
        //sẽ được xem là ".0", ví dụ 9. thì là 9.0
    }
    
    //Logic xử lý pi, pi hoạt động như một con số độc lập
    private void handlePi() {
        // Nếu đang nhập một con số, không cho phép chèn Pi vào
        if (!currentInput.isEmpty() && !isResult) return;

        // Xóa đang isResult, ấn pi sẽ bắt đầu một biểu thức mới với pi
        if (isResult) {
            currentInput = "";
            isResult = false;
        }

        currentInput = String.valueOf(PI);
    }

    private void handleSign() {
        if (currentInput.isEmpty() || currentInput.equals("0")) return;
        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1);
        } else {
            currentInput = "-" + currentInput;
        }
    }

    // --- NHÓM HÀM XỬ LÝ TOÁN TỬ MỘT NGÔI ---

    // Xử lý bình phương
    private void handleSquare() {
        //Xử lý bình phương,
        // cho trường hợp chỉ có 1 số và trường hợp có thêm 1 biểu thức ở trước.
        // Ví dụ 6² hoặc 1 + 6²
        applyUnaryOperator("sq", "²", false);
    }

    // Xử lý căn bậc hai
    private void handleSqrt() {
        //Hàm xử lý căn bậc 2 hoạt động tương tự,
        // có 2 trường hợp là có firstValue và không có firstValue
        applyUnaryOperator("sqrt", "√", true);
    }

    // Xử lý phần trăm
    private void handlePercent() {
        applyUnaryOperator("percent", "%", false);
    }

    //Hàm dùng chung của các phép tính đặc biệt
    // Xử lý cho 2 trường hợp là có firstValue và không có firstValue

    //isPrefix để biết ký hiệu nằm trước hay sao
    // ví dụ % và mũ sẽ nằm sau còn căn sẽ nẳm trước, prefix dùng để hỗ trợ hiển thị
    private void applyUnaryOperator(String type, String symbol, boolean isPrefix) {
        // Nếu currentInput mà rỗng thì lấy phép tính đặc biệt áp vào số 0
        // ví dụ khi chỉ nhập dấu % thì hệ thống sẽ hiểu là 0%
        String valStr = currentInput.isEmpty() ? "0" : currentInput;
        // Chuyển đổi chuỗi thành số thực
        double val = Double.parseDouble(valStr);

        // Định dạng lại con số (ví dụ: từ 9.0 thành 9)
        String formattedVal = formatDouble(val);

        // Tạo chuỗi kết hợp số và ký hiệu (ví dụ: √9 hoặc 9² hoặc 9%)
        String operandPart =
                isPrefix ? symbol + formattedVal : formattedVal + symbol;

        // Xây dựng chuỗi lịch sử đầy đủ.
        // Sử dụng toán tử điều kiện (? :) để kiểm tra:
        // - Nếu chưa có số thứ nhất (firstValue là NaN): Tiền tố là chuỗi rỗng ""
        // - Nếu đã có (ví dụ "1 + "): Ghép thêm phần đó vào đầu chuỗi.
        // Cuối cùng thêm ký hiệu vừa tạo (operandPart) và dấu bằng " =".
        String history = (Double.isNaN(firstValue) ? ""
                : formatDouble(firstValue) + " " + operator + " ")
                + operandPart + " =";

        // Kiểm tra lỗi unary cho phần tính toán đặc biệt(ví dụ căn số âm)
        String unaryError = calculator.getUnaryErrorMessage(val, type);
        if (unaryError != null) {
            handleError(unaryError, history);
            return;
        }

        // Tính giá trị của phép tính đặc biệt
        double res = calculator.calculateUnary(val, type);

        // Nếu đang có một phép tính chờ(có firstValue) (ví dụ: 1 + 6²),
        // thực hiện tính nốt cả cụm
        if (!Double.isNaN(firstValue)) {
            // Kiểm tra phần tính toán thông thường, ví dụ 1+2 xem có lỗi(ví dụ chia cho 0)
            String binaryError = calculator.getErrorMessage(firstValue, res, operator);
            if (binaryError != null) {
                handleError(binaryError, history);
                return;
            }
            // Thực hiện phép tính giữa firstValue và kết quả của phép tính đặc biệt
            currentInput = String.valueOf(calculator.calculate(firstValue, res, operator));
            firstValue = Double.NaN;
            operator = "";

        } else { //Nếu không có firstValue thì
            // chỉ hiện thị kết quả của phép tính đặc biệt
            currentInput = String.valueOf(res);
        }
        //Hiện thị lịch sử và kết quả
        tvDisplay.setText(currentInput);
        tvHistory.setText(history);
        isResult = true;
    }

//Xử lý dấu phép tính +-*/, bao gồm cả việc có tác dụng như 1 dấu =
    private void handleOperator(String op) {

        if (currentInput.isEmpty()) {
            //Nếu chưa có currentInput và firstValue, khi ấn dấu sẽ mặc định firstValue là 0
            if (Double.isNaN(firstValue)) firstValue = 0.0;
            //Cho dấu vừa nhập là operator tổng, sẵn sàng cho phép tính kế tiếp
            operator = op;
        } else {
            //Nếu đã có cả currentInput và firstValue thì chức năng của dấu
            // sẽ là thực hiện phép tính trước đó
            // và lưu lại kết quả vào firstValue
            if (!Double.isNaN(firstValue)) {
                double secondValue = Double.parseDouble(currentInput);
                String history = formatDouble(firstValue) + " " + operator + " " + formatDouble(secondValue) + " =";
                String error = calculator.getErrorMessage(firstValue, secondValue, operator);
                if (error != null) { handleError(error, history); return; }
                firstValue = calculator.calculate(firstValue, secondValue, operator);
            } else {
                firstValue = Double.parseDouble(currentInput);
            }
            //Sau khi tính xong phép tính trước đó thì lưu dấu vừa nhập vào operator tổng
            operator = op;
            currentInput = "";
            isResult = false;
        }
    }

    private void handleEqual() {
        //Chỉ thực hiện nếu cả firstValue và currentInput đều có giá trị
        //Nó tính toán biểu thức và chuyển currentInput thành kết quả,
        // đưa firstValue và operator về rỗng
        if (!Double.isNaN(firstValue) && !currentInput.isEmpty()) {
            double secondValue = Double.parseDouble(currentInput);
            String fullEquation = formatDouble(firstValue) + " " + operator + " " + formatDouble(secondValue) + " =";
            String error = calculator.getErrorMessage(firstValue, secondValue, operator);

            if (error != null) { handleError(error, fullEquation); return; }

            double result = calculator.calculate(firstValue, secondValue, operator);
            tvHistory.setText(fullEquation);
            currentInput = String.valueOf(result);
            firstValue = Double.NaN;
            operator = "";
            isResult = true;
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
        // Nút DEL sẽ xóa sạch nếu là hằng số Pi hoặc kết quả tính toán
        if (isResult || currentInput.equals(String.valueOf(PI)) || currentInput.length() <= 1) {
            currentInput = "";
            isResult = false;
        } else {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        }
    }

    //Hàm này không hỗ trợ hoàn toàn phần hiển thị do có những trường hợp đặc biệt
    private void updateDisplay() {
        if (isError) return;
        //Hiển thị history
        if (!isResult) {
            //Nếu không có kết quả
            // nhưng có firstValue thì History hiển thị firstValue và dấu
            if (!Double.isNaN(firstValue)) {
                tvHistory.setText(formatDouble(firstValue) + " " + operator);
                //Nếu không có kết quả và không có cả firstValue thì History hiển thị rỗng
            } else {
                tvHistory.setText("");
            }
        }
        //Hiển thị display
        //Nếu không có currentInput thì hiển thị 0
        String toDisplay = currentInput.isEmpty() ? "0" : currentInput;
        // Định dạng Pi hoặc Kết quả để tránh tràn màn hình
        if ((isResult || currentInput.equals(String.valueOf(PI))) && !currentInput.isEmpty()) {
            toDisplay = formatDouble(Double.parseDouble(currentInput));
        }
        tvDisplay.setText(toDisplay);
    }

    //Hàm đinh dạng lại số thực, hỗ trợ hiển thị ra màn hình

    private String formatDouble(double d) {
        // Kiểm tra nếu số thực chất là một số nguyên
        if (d == (long) d) {
            // Định dạng trả về kiểu số nguyên để không hiển thị phần ".0"
            return String.format("%d", (long) d);
        } else {
            try {
                //Xử lý chính xác số thập phân (tránh lỗi kiểu 0.300000000004)
                BigDecimal bd = new BigDecimal(Double.toString(d));
                // Làm tròn tối đa 10 chữ số thập phân và xóa các số 0 vô nghĩa ở cuối
                bd = bd.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
                // Trả về chuỗi số ở dạng thông thường (không hiển thị kiểu ký hiệu khoa học 1E-10)
                return bd.toPlainString();
            } catch (Exception e) {
                // Nếu có lỗi trong quá trình xử lý (ví dụ số quá lớn), trả về giá trị mặc định
                return String.valueOf(d);
            }
        }
    }
}