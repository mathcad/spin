package org.spin.data.pk.converter;


import org.spin.data.pk.Id;

import java.io.Serializable;

public interface IdConverter<K extends Serializable, I extends Id> {

    K convert(I id);

    I convert(K id);
}
