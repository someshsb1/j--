// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a double literal.
 */
class JLiteralDouble extends JExpression {
    // String representation of the literal.
    private String text;

    /**
     * Constructs an AST node for a double literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public JLiteralDouble(int line, String text) {
        super(line);
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        type = Type.DOUBLE; // type is double
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        double d = Double.parseDouble(text); // checks the value of the parsed double d and adds the corresponding instructions to the output.

        if (d == 0.0d) { // If the parsed double value is 0 or 1, it adds the corresponding constant instruction (DCONST_0 or DCONST_1) to the output.
            output.addNoArgInstruction(DCONST_0);
        } else if (d == 1.0d) {
            output.addNoArgInstruction(DCONST_1);
        } else {
            output.addLDCInstruction(d); //adds an LDC instruction to the output, which loads the double value onto the stack.
        }
    }
    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JLiteralDouble:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}
