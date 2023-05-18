// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import jdk.jshell.execution.JdiExecutionControl;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a conditional expression.
 */
class JConditionalExpression extends JExpression {
    // Test expression.
    private JExpression condition;

    // Then part.
    private JExpression thenPart;

    // Else part.
    private JExpression elsePart;

    /**
     * Constructs an AST node for a conditional expression.
     *
     * @param line      line in which the conditional expression occurs in the source file.
     * @param condition test expression.
     * @param thenPart  then part.
     * @param elsePart  else part.
     */
    public JConditionalExpression(int line, JExpression condition, JExpression thenPart,
                                  JExpression elsePart) {
        super(line);
        this.condition = condition;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        condition = (JExpression) condition.analyze(context);
        thenPart = (JExpression) thenPart.analyze(context);
        elsePart = (JExpression) elsePart.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN); //Analyze the condition and
        // make sure it’s a boolean.
        elsePart.type().mustMatchExpected(line(), thenPart.type()); //Analyze the consequent and
        // alternate and make sure they have the same type.
        type = thenPart.type();// Set the type of the expression to that of the consequent (or
        // alternate).
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) { //codegen implemented to support conditional expression
        String elseLabel = output.createLabel();
        String endLabel = output.createLabel();
        condition.codegen(output, elseLabel,false);
        thenPart.codegen(output);
        if (elsePart != null) { output.addBranchInstruction(GOTO, endLabel); }
        output.addLabel(elseLabel);
        if (elsePart != null) {
            elsePart.codegen(output);
            output.addLabel(endLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JConditionalExpression:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("ThenPart", e2);
        thenPart.toJSON(e2);
        JSONElement e3 = new JSONElement();
        e.addChild("ElsePart", e3);
        elsePart.toJSON(e3);
    }
}