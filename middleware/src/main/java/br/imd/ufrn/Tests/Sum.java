package br.imd.ufrn.Tests;

public class Sum {
    private int num1;
    private int num2;
    private int result;
    private String test;

    public Sum(int num1, int num2, String test) {
        this.num1 = num1;
        this.num2 = num2;
        this.result = num1 + num2;
        this.test = test;
    }

    public int getNum1() {
        return this.num1;
    }

    public int getNum2() {
        return this.num2;
    }

    public int getResult() {
        return this.result;
    }

    public String getTest() {
        return this.test;
    }
}
