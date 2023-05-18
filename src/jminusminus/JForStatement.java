// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.Locale;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;
    //Declare two instance variables in each control-flow statement (do, while, for, and switch):
    // hasBreak and breakLabel
    public boolean hasBreak;
    public String breakLabel;

    // Declare two instance variables in each control-flow statement (do, while, and for): boolean hasContinue and String continueLabel.
    public String continueLabel;

    public boolean hasContinue;
    // Update.
    private ArrayList<JStatement> update;

    // The body.
    private JStatement body;

    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition,
                         ArrayList<JStatement> update, JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    /**
     * {@inheritDoc}
     */
    public JForStatement analyze(Context context) {
        JMember.enclosingStatement.push(this); //push a reference to self onto upon entry
        context = new LocalContext(context); //Create a new LocalContext with context as the parent.
        if (init != null) {
            for (int i = 0; i < init.size(); i++) { //Analyze the init in the new context.
                init.set(i, (JStatement) init.get(i).analyze(context));
            }
        }
        if (condition != null) {
            condition = condition.analyze(context);
            condition.type().mustMatchExpected(line, Type.BOOLEAN); // Analyze the condition in the new context and make sure it’s a boolean.
        }
        // Analyze the update in the new context.
        if (update != null) {
            for (int i = 0; i < update.size(); i++) { //Analyze the init in the new context.
                update.set(i, (JStatement) update.get(i).analyze(context));
            }

        }
        // Analyze the body in the new context.
        body = (JStatement) body.analyze(context);
        JMember.enclosingStatement.pop();//pop the reference upon exit
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) { //codegen support added for -> For statement
        continueLabel = output.createLabel();
        breakLabel = output.createLabel();
        String conditionLabel = output.createLabel();
        String exitLoop = output.createLabel();
        String exitlabel = output.createLabel();

        if(init != null) {
            for(JStatement forInit: init) { forInit.codegen(output); }
        }
        output.addLabel(conditionLabel);
        if (condition != null) { condition.codegen(output, exitLoop, false); }
        body.codegen(output);
        output.addLabel(continueLabel);

        if (update != null) {
            for (JStatement forUpdate: update) { forUpdate.codegen(output); }
        }
        output.addBranchInstruction(GOTO, conditionLabel);
        output.addLabel(exitlabel);
        output.addLabel(exitLoop);
        output.addLabel(breakLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
