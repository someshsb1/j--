// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a do-statement.
 */
public class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;
    //Declare two instance variables in each control-flow statement (do, while, for, and switch):
    // hasBreak and breakLabel
    public boolean hasBreak = false;
    public String breakLabel;
//  Declare two instance variables in each control-flow statement (do, while, and for): boolean hasContinue and String continueLabel.
    public boolean hasContinue = false;

    public String continueLabel;

    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        JMember.enclosingStatement.push(this); //push a reference to self onto upon entry
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN); //Analyze the condition and
        // make sure it’s a boolean.
        body = (JStatement) body.analyze(context); //Analyze the body.
        JMember.enclosingStatement.pop(); //pop the reference upon exit
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) { //codegen implemented to support the do while statement
        String bbody = output.createLabel();
        String out = output.createLabel();
        continueLabel = output.createLabel();
        breakLabel = output.createLabel();
        output.addLabel(bbody);
        output.addLabel(continueLabel);
        body.codegen(output);
        condition.codegen(output, out, false);
        output.addBranchInstruction(GOTO, bbody);
        output.addLabel(out);
        output.addLabel(breakLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
