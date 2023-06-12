package gr.aueb.straboio.model;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

public class Model {
    /**
     * Wrapper class for PyTorch's 'Module' to mimic Python's functionality.
     */
    private Module module;
    public final int INPUT_SIZE;
    public final int EMBED_SIZE;
    public final int HIDDEN_SIZE;
    public final int OUTPUT_SIZE;


    public Model(Module module, int INPUT_SIZE, int EMBED_SIZE, int HIDDEN_SIZE, int OUTPUT_SIZE) {
        this.module = module;
        this.INPUT_SIZE = INPUT_SIZE;
        this.EMBED_SIZE = EMBED_SIZE;
        this.HIDDEN_SIZE = HIDDEN_SIZE;
        this.OUTPUT_SIZE = OUTPUT_SIZE;
    }

    /**
     * The module's complete forward method.
     * @param input - the module's input
     * @param hidden - the hidden layer representation
     * @param cell - the cell lauer representation
     * @return array of three IValue elements (output, hidden, cell)
     */
    public IValue[] forward(IValue input, IValue hidden, IValue cell){
        return this.module.forward(
                input,
                hidden,
                cell
        ).toTuple();
    }

    /**
     * The module's forward method with zero-filled tensors for hidden layers.
     * @param input - the module's input
     * @return array of three IValue elements (output, hidden, cell)
     */
    public IValue[] forward(IValue input){
        return this.forward(
                input,
                IValue.from(Tensor.fromBlob(new float[this.HIDDEN_SIZE], new long[]{1, this.HIDDEN_SIZE})),
                IValue.from(Tensor.fromBlob(new float[this.HIDDEN_SIZE], new long[]{1, this.HIDDEN_SIZE}))
        );
    }
}