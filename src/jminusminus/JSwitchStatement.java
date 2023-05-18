// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a switch-statement.
 */
public class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> stmtGroup;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line      line in which the switch-statement occurs in the source file.
     * @param condition test expression.
     * @param stmtGroup list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition,
                            ArrayList<SwitchStatementGroup> stmtGroup) {
        super(line);
        this.condition = condition;
        this.stmtGroup = stmtGroup;
    }
    //Declare two instance variables in each control-flow statement (do, while, for, and switch): boolean hasBreak and String breakLabel.
    public boolean hasBreak;
    private boolean hasDefault;
    public String breakLabel;
    //Create a new LocalContext with context as the parent, and analyze the statements in each
    // case group in the new context.
    private int hi, lo, nLabels;
    private ArrayList<String> label;
    private ArrayList<Integer> caselabel;
    private TreeMap<Integer, String> maplabel;
    private LocalContext context;



    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
        condition = (JExpression) condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.INT); // Analyze the condition and make sure it is an integer.
        for (SwitchStatementGroup sgrp : stmtGroup) {
            for (int i = 0; i < sgrp.getLabels().size(); i++) {
                JExpression switchLabels = sgrp.getLabels().get(i);
                if (switchLabels instanceof JLiteralInt) { // Analyze the case expressions and
                    // make sure they are integer literals.
                    sgrp.getLabels().set(i, (JExpression) switchLabels.analyze(context));
                } else if (switchLabels != null) {
                    JAST.compilationUnit.reportSemanticError(line(), "Switch Labels must be Int " +
                            "Literal");
                }
            }
        }
        for (SwitchStatementGroup sgrp: stmtGroup) {
            this.context = new LocalContext(context); // Create a new LocalContext with context as the parent, and analyze the statements in each case group in the new context.
            for (int i = 0; i < sgrp.getBlock().size(); i++) {
                sgrp.getBlock().set(i, (JStatement) sgrp.getBlock().get(i).analyze(this.context));
            }
        }
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        condition.codegen(output);
        if (hasBreak) {
            breakLabel = output.createLabel();
        }
        String dLabel = output.createLabel();

        maplabel = new TreeMap<Integer, String>();
        label = new ArrayList<String>();
        caselabel = new ArrayList<Integer>();

        for (SwitchStatementGroup sgrp : stmtGroup) {
            for (int i = 0; i < sgrp.getLabels().size(); i++) {
                JExpression switchLabels = sgrp.getLabels().get(i);
                if (switchLabels != null) {
                    int caseRef = ((JLiteralInt) switchLabels).toInt();
                    caselabel.add(caseRef);
                    String caseInt = output.createLabel();
                    label.add(caseInt);
                    maplabel.put(caseRef, caseInt);

                } else {
                    hasDefault = true;
                }
            }
        }
        nLabels = maplabel.size();
        Collections.sort(caselabel);
        lo = caselabel.get(0);
        hi = caselabel.get(caselabel.size() - 1);
// In codegen() decide which instruction (TABLESWITCH or LOOKUPSWITCH) to emit using the above heuristic.
        long tableSpaceCost = 5 + hi - lo ;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels ;
        long lookupTimeCost = nLabels ;
        int opcode = nLabels > 0 && ( tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost ) ?
                TABLESWITCH : LOOKUPSWITCH ;
        if (opcode == TABLESWITCH) {
            output.addTABLESWITCHInstruction(dLabel, lo, hi, label); // Call the appropriate CLEmitter method to emit that instruction — you will first need to gather all the information that
            //must be passed as arguments to the method.
        } else {
            output.addLOOKUPSWITCHInstruction(dLabel, nLabels, maplabel);
        }

        Iterator<String> iterator = label.iterator();
// Generate code for the case group statements, adding labels at the appropriate places.
        for (SwitchStatementGroup sgrp : stmtGroup) {
            for (int i = 0; i < sgrp.getLabels().size(); i++) {
                JExpression switchLabels = sgrp.getLabels().get(i);
                if (switchLabels != null) {
                    output.addLabel(iterator.next());
                } else {
                    output.addLabel(dLabel);
                }
            }
            for (JStatement stmt: sgrp.getBlock()) {
                stmt.codegen(output);
            }
        }
        if (!hasDefault) {
            output.addLabel(dLabel);
        }
        if (hasBreak) {
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : stmtGroup) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch statement group consists of case labels and a block of statements.
 */
class SwitchStatementGroup {
    // Case labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels case labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    public ArrayList<JStatement> getBlock() {
        return this.block;
    }
    public ArrayList<JExpression> getLabels() {
        return this.switchLabels;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
