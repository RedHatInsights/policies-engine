package org.hawkular.alerts.engine.impl.ispn;

import com.google.common.collect.Multimap;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Set;

public class MultimapExternalizer implements AdvancedExternalizer<Multimap> {
    @Override
    public Set<Class<? extends Multimap>> getTypeClasses() {
        return Util.<Class<? extends Multimap>>asSet(Multimap.class);
    }

    @Override
    public Integer getId() {
        return 6969;
    }

    @Override
    public void writeObject(ObjectOutput output, Multimap object) throws IOException {
        System.out.printf("writeObject called\n");
        output.writeObject(object);
    }

    @Override
    public Multimap readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        System.out.printf("readObject called\n");
        Object inputObject = input.readObject();
        if(inputObject instanceof Map) {
            // Cast to Multimap
            System.out.printf("Found a map, need to cast it to a Multimap\n");
        } else if(inputObject instanceof Multimap) {
            return (Multimap<String, String>) inputObject;
        }
        return null;
    }
}
