// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a break-statement.
 */
public class JBreakStatement extends JStatement {

    public JStatement enclosingStatement;

    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    //Declare an instance variable JStatement enclosingStatement in JBreakStatement

    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // during analysis, set it to the value at the top of JMember.enclosingStatement (use
        // peek()). Then set the enclosing statement’s hasBreak variable to true.
        enclosingStatement = JMember.enclosingStatement.peek();

        //Each control-flow statement (do, while, for, and switch), during codegen, must set breakLabel to an appropriate label if
        //hasBreak is true, and add the label at the appropriate place.
        if (enclosingStatement instanceof  JDoStatement) {
            ((JDoStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JForStatement) {
            ((JForStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JWhileStatement) {
            ((JWhileStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JSwitchStatement) {
            ((JSwitchStatement) enclosingStatement).hasBreak = true;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        //During codegen in JBreakStatement, access the break label via the enclosing statement, and generate an unconditional
        //jump to that label.
        if (enclosingStatement instanceof JDoStatement && ((JDoStatement) enclosingStatement).hasBreak) {
            output.addBranchInstruction(GOTO, ((JDoStatement) enclosingStatement).breakLabel);
        } else if (enclosingStatement instanceof JForStatement && ((JForStatement) enclosingStatement).hasBreak) {
            output.addBranchInstruction(GOTO, ((JForStatement) enclosingStatement).breakLabel);
        } else if (enclosingStatement instanceof JWhileStatement && ((JWhileStatement) enclosingStatement).hasBreak) {
            output.addBranchInstruction(GOTO, ((JWhileStatement) enclosingStatement).breakLabel);
        } else if (enclosingStatement instanceof JSwitchStatement && ((JSwitchStatement) enclosingStatement).hasBreak) {
            System.out.print("Has break true");
            output.addBranchInstruction(GOTO, ((JSwitchStatement) enclosingStatement).breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
