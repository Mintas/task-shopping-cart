package ru.kovalev.shopping.validation;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;
import ru.kovalev.shopping.rest.dto.ChangeQuantityRequest;

@UnwrapByDefault
public class ChangeQuantityValueExtractor
        implements ValueExtractor<@ExtractedValue(type = Integer.class) ChangeQuantityRequest> {

    @Override
    public void extractValues(ChangeQuantityRequest originalValue, ValueReceiver receiver) {
        receiver.value("quantity", originalValue.getQuantity());
    }
}
