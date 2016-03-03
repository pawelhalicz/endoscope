package org.endoscope.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 03/03/16
 * Time: 20:31
 *
 * @author p.halicz
 */
public class AbstractCustomPropertyProvider extends SystemPropertyProvider {
    protected Map<String, String> override = new HashMap<>();

    @Override
    public String get(String name, String defaultValue) {
        String value = override.get(name);
        return value != null ? value : super.get(name, defaultValue);
    }
}
