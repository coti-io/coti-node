package io.coti.trustscore.utils;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;


public class MathCalculation {

    private static final double differenceEqualityCompare = 0.000001;

    public static double evaluteExpression(String stringFormula) {
        Expression expression = new Expression(stringFormula);
        return expression.calculate();
    }

    public static double evaluteExpression(String stringFormula, Argument argument) {
        Expression expression = new Expression(stringFormula);
        expression.addArguments(argument);
        return expression.calculate();
    }


    public static boolean ifTwoNumbersAreEqualOrAlmostEqual(double x, double y) {
        return Math.abs(x - y) <= differenceEqualityCompare;
    }
}
