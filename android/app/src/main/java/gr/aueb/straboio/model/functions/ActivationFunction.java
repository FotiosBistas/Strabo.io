package gr.aueb.straboio.model.functions;

import org.pytorch.IValue;
import org.pytorch.Tensor;

public class ActivationFunction {

    public static IValue logSoftmax(IValue x) {
        Tensor xtensor = x.toTensor();
        float[] xdata = xtensor.getDataAsFloatArray();
        float sum = 0f;
        for(float num : xdata){
            sum = (float) (sum + Math.exp(num));
        }
        sum = (float) Math.log(sum);
        float[] xdata_log = new float[xdata.length];
        for(int i=0; i<xdata_log.length; i++)
            xdata_log[i] = xdata[i] - sum;

        return IValue.from(
                Tensor.fromBlob(xdata_log, new long[]{xdata_log.length})
        );
    }
}
