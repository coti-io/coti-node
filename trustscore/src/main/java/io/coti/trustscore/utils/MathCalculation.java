package io.coti.trustscore.utils;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.ArrayList;
import java.util.List;


public class MathCalculation {

    public static double evaluteExpression(String stringFormula) {
        Expression expression = new Expression(stringFormula);
        double result =  expression.calculate();
        return result;
    }

    public static double evaluteExpression(String stringFormula, List<FormulaArgument> parameters) {

        ArrayList<Argument> arguments = new ArrayList<>();
        for (FormulaArgument argument : parameters) {
            arguments.add(new Argument(argument.getArgument(), argument.getValue()));
        }
        Expression expression = new Expression(stringFormula);

        if (parameters != null && parameters.size() > 0)
            expression.addArguments((Argument[]) arguments.toArray());
        return expression.calculate();
    }

    public static double evaluteExpression(String stringFormula, Argument argument) {
        Argument arguments = argument;
        Expression expression = new Expression(stringFormula);
        expression.addArguments(arguments);
        return expression.calculate();
    }


    public static boolean ifTwoNumbersAreEqualOrAlmostEqual(double x , double y) {
        if (Math.abs(x - y) <= 0.000001)
            return true;
        return false;
    }
}
