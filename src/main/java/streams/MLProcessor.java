package streams;

import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
//import org.apache.kafka.streams.processor.*;


public class MLProcessor implements Processor<String, String, String, Integer> {
    private KeyValueStore<String, Integer> kvStore;

    private ProcessorContext<String, Integer> processorContext;
    private final Model model = new Model();
    @Override
    public void init(final ProcessorContext<String,Integer> context) {

        kvStore = context.getStateStore("Counts");
        this.processorContext = context;
        System.out.println("model initialized");
    }

    @Override
    public void process(final Record<String, String> record) {
        final String[] stringInput = record.value().split(",");
        Float[] input = new Float[2];
        input[0] = Float.valueOf(stringInput[0]);
        input[1] = Float.valueOf(stringInput[1]);
        float[] output = model.predict(input);
        if(output[0] >= output[1]){
            Integer normal_count = kvStore.get("normal");
            if(normal_count == null){
                kvStore.put("normal", 1);
            }else{
                kvStore.put("normal", normal_count + 1);
            }
            System.out.println(kvStore.get("normal"));
            this.processorContext.forward(new Record<String,Integer>("normal", kvStore.get("normal"), record.timestamp()));
        }
        else {
            Integer abnormal_count = kvStore.get("abnormal");
            if(abnormal_count == null){
                kvStore.put("abnormal", 1);
            }else{
                kvStore.put("abnormal", abnormal_count + 1);
            }
            System.out.println(kvStore.get("abnormal"));
            this.processorContext.forward(new Record<String,Integer>("abnormal", kvStore.get("abnormal"), record.timestamp()));
        }

    }

    @Override
    public void close() {

        // close any resources managed by this processor
        // Note: Do not close any StateStores as these are managed by the library
    }
}
