// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a throw-statement.
 */
class JThrowStatement extends JStatement {
    // The thrown exception.
    private JExpression expr;



    /**
     * Constructs an AST node for a throw-statement.
     *
     * @param line line in which the throw-statement appears in the source file.
     * @param expr the returned expression.
     */
    public JThrowStatement(int line, JExpression expr) {
        super(line);
        this.expr = expr;
    }

    /**
     * {@inheritDoc}
     */
    //Implement analyze() and codegen() in JThrowStatement.
    public JStatement analyze(Context context) {
        expr = (JExpression) expr.analyze(context); //analyze the expression expr
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) { //codegen support added for throw statement
        expr.codegen(output);
        output.addNoArgInstruction(ATHROW);
    }


    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JThrowStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Expression", e1);
        expr.toJSON(e1);
    }
}
