// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a continue-statement.
 */
public class JContinueStatement extends JStatement {

    public JStatement enclosingStatement;
    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */
    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // during analysis, set it to the value at the top of JMember.enclosingStatement (use
        // peek()). Then set the enclosing statement’s hasContinue variable to true.
         enclosingStatement = JMember.enclosingStatement.peek();

        //Each control-flow statement (do, while, for), during codegen, must set breakLabel to an appropriate label if
        //hasBreak is true, and add the label at the appropriate place.
        if (enclosingStatement instanceof  JDoStatement) {
            ((JDoStatement) enclosingStatement).hasContinue = true;
        } else if (enclosingStatement instanceof JForStatement) {
            ((JForStatement) enclosingStatement).hasContinue = true;
        } else if (enclosingStatement instanceof JWhileStatement) {
            ((JWhileStatement) enclosingStatement).hasContinue = true;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        //during codegen, must set continueLabel to an appropriate label if
        //hasContinue is true, and add the label at the appropriate place.
        if (enclosingStatement instanceof JDoStatement && ((JDoStatement) enclosingStatement).hasContinue) {
            output.addBranchInstruction(GOTO, ((JDoStatement) enclosingStatement).continueLabel);
        } else if (enclosingStatement instanceof JForStatement && ((JForStatement) enclosingStatement).hasContinue) {
            output.addBranchInstruction(GOTO, ((JForStatement) enclosingStatement).continueLabel);
        } else if (enclosingStatement instanceof JWhileStatement && ((JWhileStatement) enclosingStatement).hasContinue) {
            output.addBranchInstruction(GOTO, ((JWhileStatement) enclosingStatement).continueLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}
