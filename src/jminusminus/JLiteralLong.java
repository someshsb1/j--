// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a long literal.
 */
class JLiteralLong extends JExpression {
    // String representation of the literal.
    private String text;

    /**
     * Constructs an AST node for a long literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public JLiteralLong(int line, String text) {
        super(line);
        this.text = text;
    }

    //extracting a substring from a string and parses it into a long value.
    private long aLong() {
        return Long.parseLong(text.substring(0, text.length() - 1));
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        type = Type.LONG; // type is Long
        return this;
    }

    /**
     * {@inheritDoc}
     */

    public void codegen(CLEmitter output) {
        long l = aLong(); // checks the value returned by the aLong() method and adds the corresponding instructions to the output.
        if (l == 0L) {

            output.addNoArgInstruction(LCONST_0); //If the parsed long value is 0 or 1, it adds
            // the corresponding constant instruction (LCONST_0 or LCONST_1) to the output
        } else if (l == 1L) {
            output.addNoArgInstruction(LCONST_1);
        } else {
            output.addLDCInstruction(l); //adds an LDC instruction to the output, which loads the long value onto the stack.
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JLiteralLong:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}
