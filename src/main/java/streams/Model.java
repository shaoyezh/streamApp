package streams;

import org.tensorflow.*;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;
import org.tensorflow.ndarray.buffer.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Model {

    SavedModelBundle savedModelBundle = SavedModelBundle.load("./models/classifier/", "serve");
    Graph graph = savedModelBundle.graph();

    FloatNdArray inputArray = NdArrays.ofFloats(Shape.of(1,2));

    public float[] predict(Float[] input){
//        SavedModelBundle savedModelBundle = SavedModelBundle.load("./models/classifier/", "serve");
//        Graph graph = savedModelBundle.graph();
//
//        FloatNdArray inputArray = NdArrays.ofFloats(Shape.of(1,2));
        inputArray.set(NdArrays.vectorOf(input[0],input[1]),0);
        TFloat32 inputTensor = TFloat32.tensorOf(inputArray);
        Tensor outputTensor = savedModelBundle.session().runner().feed("serving_default_dense_5_input", inputTensor).
                fetch("StatefulPartitionedCall").run().get(0);
        ByteDataBuffer buffer = outputTensor.asRawTensor().data();
        byte[] output = new byte[8];
        buffer.read(output);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf = buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(output, 0, 8);
        buf.rewind();
        float f1 = buf.getFloat();
        float f2 = buf.getFloat();
        inputTensor.close();
        outputTensor.close();
        return new float[]{f1, f2};
    }
    // StatefulPartitionedCall
    // serving_default_dense_input
//    public static void main(String[] args) throws Exception {
//        System.out.println("Hello TensorFlow " + TensorFlow.version());
//        SavedModelBundle savedModelBundle = SavedModelBundle.load("./models/classifier/", "serve");
//        Graph graph = savedModelBundle.graph();
//
//        FloatNdArray input = NdArrays.ofFloats(Shape.of(1,2));
//        input.set(NdArrays.vectorOf(0.5f,0.5f),0);
//        TFloat32 inputTensor = TFloat32.tensorOf(input);
//        Tensor outputTensor = savedModelBundle.session().runner().feed("serving_default_dense_5_input", inputTensor).
//                fetch("StatefulPartitionedCall").run().get(0);
//        ByteDataBuffer buffer = outputTensor.asRawTensor().data();
//        byte[] output = new byte[8];
//        buffer.read(output);
//        ByteBuffer buf = ByteBuffer.allocate(8);
//        buf = buf.order(ByteOrder.LITTLE_ENDIAN);
//        buf.put(output, 0, 8);
//        buf.rewind();
//        float f1 = buf.getFloat();
//        float f2 = buf.getFloat();
//        System.out.println(f1);
//        System.out.println(f2);
//
////        ByteDataBuffer buffer = DataBuffers.ofBytes(8);
////        outputTensor.asRawTensor().data().copyTo(buffer, 8);
////        FloatDataBuffer floatBuffer = buffer.asFloats();
////        System.out.println(floatBuffer.getFloat(0));
////        System.out.println(floatBuffer.getFloat(1));
//
//        inputTensor.close();
//        outputTensor.close();
//
//    }
}