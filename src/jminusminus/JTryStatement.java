// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a try-catch-finally statement.
 */
class JTryStatement extends JStatement {
    // The try block.
    private JBlock tryBlock;

    // The catch parameters.
    private ArrayList<JFormalParameter> parameters;

    // The catch blocks.
    private ArrayList<JBlock> catchBlocks;

    // The finally block.
    private JBlock finallyBlock;
    protected String name;

    protected Type type;
    protected MethodContext context;

    protected boolean isStatic;
    protected Type returnType;

    /**
     * Constructs an AST node for a try-statement.
     *
     * @param line         line in which the while-statement occurs in the source file.
     * @param tryBlock     the try block.
     * @param parameters   the catch parameters.
     * @param catchBlocks  the catch blocks.
     * @param finallyBlock the finally block.
     */
    public JTryStatement(int line, JBlock tryBlock, ArrayList<JFormalParameter> parameters,
                         ArrayList<JBlock> catchBlocks, JBlock finallyBlock) {
        super(line);
        this.tryBlock = tryBlock;
        this.parameters = parameters;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;

    }

    /**
     * {@inheritDoc}
     */
    public JTryStatement analyze(Context context) {

        LocalContext tryContext = new LocalContext(context);
        tryBlock = (JBlock) tryBlock.analyze(tryContext);
        for (int i = 0; i < parameters.size(); i++) {
            LocalContext catchContext = new LocalContext(context);
            JFormalParameter param = parameters.get(i);
            param.setType(param.type().resolve(catchContext));
            Type type = param.type();
            LocalVariableDefn defn = new LocalVariableDefn(type, catchContext.nextOffset());
            defn.initialize();
            catchContext.addEntry(param.line(), param.name(), defn);
            catchBlocks.set(i, ((JBlock) catchBlocks.get(i)).analyze(catchContext));
        }
        if (finallyBlock != null) {
            finallyBlock = (JBlock) finallyBlock.analyze(context);
        }
        return this;
    }


    /**
     * {@inheritDoc}
     */

    public void codegen(CLEmitter output) {
        String startTry = output.createLabel();
        String endTry = output.createLabel();
        String endCatch = output.createLabel();
        String startFinally = output.createLabel();
        String startFinallyPlusOne = output.createLabel();
        String endFinally = output.createLabel();
        String startCatch = output.createLabel();

        output.addLabel(startTry);
        tryBlock.codegen(output);
        if (finallyBlock != null) {
            finallyBlock.codegen(output);
        }
        output.addBranchInstruction(GOTO, endFinally);
        output.addLabel(endTry);


        for (int i = 0; i < catchBlocks.size(); i++) {
            output.addLabel(startCatch);
            output.addNoArgInstruction(ASTORE_1);
            catchBlocks.get(i).codegen(output);
            output.addLabel(endCatch);
            output.addExceptionHandler(startTry, endTry, startCatch, parameters.get(i).type().jvmName());
            if (finallyBlock != null) {
                finallyBlock.codegen(output);
            }
            output.addBranchInstruction(GOTO, endFinally);
        }

        output.addLabel(startFinally);
        if (finallyBlock != null) {
            output.addOneArgInstruction(ASTORE, 0);
            output.addLabel(startFinallyPlusOne);
            finallyBlock.codegen(output);
            output.addOneArgInstruction(ALOAD, 0);
            output.addNoArgInstruction(ATHROW);
        }
        output.addLabel(endFinally);
        output.addExceptionHandler(startTry, endTry, startFinally, null);

        for (JBlock catchBlock : catchBlocks) { // for each catch block, add an
            // exception handler with the arguments “start catch”, “end catch”, “start
            // finally”, and null;
            output.addExceptionHandler(startCatch, endCatch, startFinally, null);
        }
        output.addExceptionHandler(startFinally, startFinallyPlusOne, startFinally, null);

    }





    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JTryStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("TryBlock", e1);
        tryBlock.toJSON(e1);
        if (catchBlocks != null) {
            for (int i = 0; i < catchBlocks.size(); i++) {
                JFormalParameter param = parameters.get(i);
                JBlock catchBlock = catchBlocks.get(i);
                JSONElement e2 = new JSONElement();
                e.addChild("CatchBlock", e2);
                String s = String.format("[\"%s\", \"%s\"]", param.name(), param.type() == null ?
                        "" : param.type().toString());
                e2.addAttribute("parameter", s);
                catchBlock.toJSON(e2);
            }
        }
        if (finallyBlock != null) {
            JSONElement e2 = new JSONElement();
            e.addChild("FinallyBlock", e2);
            finallyBlock.toJSON(e2);
        }
    }
}
