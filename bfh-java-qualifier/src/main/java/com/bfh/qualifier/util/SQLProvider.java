package com.bfh.qualifier.util;

public class SQLProvider {
    public static final String SQL_Q1 = "SELECT\n    p.AMOUNT AS SALARY,\n    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,\n    TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE,\n    d.DEPARTMENT_NAME\nFROM PAYMENTS p\nJOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID\nJOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID\nWHERE DAY(p.PAYMENT_TIME) <> 1\nORDER BY p.AMOUNT DESC\nLIMIT 1;";
    public static final String SQL_Q2 = "TODO: add Q2 SQL here";
}
