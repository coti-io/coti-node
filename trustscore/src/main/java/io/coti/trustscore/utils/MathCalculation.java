package io.coti.trustscore.utils;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;


public class MathCalculation {

    private static final double DIFFERENCE_EQUALITY_COMPARE = 0.000001;

    private MathCalculation() {

    }

    public static double evaluateExpression(String stringFormula) {
        Expression expression = new Expression(stringFormula);
        return expression.calculate();
    }

    public static double evaluateExpression(String stringFormula, Argument argument) {
        Expression expression = new Expression(stringFormula);
        expression.addArguments(argument);
        return expression.calculate();
    }


    public static boolean ifTwoNumbersAreEqualOrAlmostEqual(double x, double y) {
        return Math.abs(x - y) <= DIFFERENCE_EQUALITY_COMPARE;
    }
}
