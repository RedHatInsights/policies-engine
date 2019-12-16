package org.hawkular.alerts.api.services;

import java.util.TreeSet;

import org.hawkular.alerts.api.model.data.Data;

/**
 * An extension that will process received Data.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public interface DataExtension {

    /**
     * The extension processes the supplied Data and returns Data to be forwarded, if any.
     *
     * @param data The Data to be processed by the extension.
     * @return The set of Data to be forwarded to the next extension, or core engine if this is the final extension.
     */
    TreeSet<Data> processData(TreeSet<Data> data);

}
