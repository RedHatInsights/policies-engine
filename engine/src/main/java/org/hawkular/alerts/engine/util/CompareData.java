package org.hawkular.alerts.engine.util;

import org.hawkular.alerts.api.model.data.Data;

/**
 * CompareConditions often work against data arriving at different times. We convert Data into CompareData and
 * apply special handling such that CompareData can live longer than one engine firing.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class CompareData {

    public Data data;

    public CompareData(Data data) {
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
        CompareData other = (CompareData) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CompareData [data=" + data + "]";
    }

}
