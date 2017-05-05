package org.spin.jpa.pk.converter;


import org.spin.jpa.pk.Id;

import java.io.Serializable;

public interface IdConverter<K extends Serializable, I extends Id> {

    K convert(I id);

    I convert(K id);
}
