package io.coti.trustscore.utils;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.ArrayList;
import java.util.List;


public class MathCalculation {

    public static double evaluteExpression(String stringFormula) {
        Expression expression = new Expression(stringFormula);
        return expression.calculate();
    }

    public static double evaluteExpression(String stringFormula, List<FormaleArgument> parameters) {

        ArrayList<Argument> arguments = new ArrayList<>();
        for (FormaleArgument argument : parameters) {
            arguments.add(new Argument(argument.getArgument(), argument.getValue()));
        }
        Expression expression = new Expression(stringFormula);

        if (parameters != null && parameters.size() > 0)
            expression.addArguments((Argument[]) arguments.toArray());
        return expression.calculate();
    }
}
