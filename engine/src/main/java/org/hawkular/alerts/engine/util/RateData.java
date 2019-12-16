package org.hawkular.alerts.engine.util;

import org.hawkular.alerts.api.model.data.Data;

/**
 * RateConditions use a current and previous datum for the same dataId.  This class simply stores the
 * previous datum as a Fact in Drools' working memory, for use in the evaluation.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class RateData {

    public Data data;

    public RateData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RateData other = (RateData) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RateData [data=" + data + "]";
    }

}
