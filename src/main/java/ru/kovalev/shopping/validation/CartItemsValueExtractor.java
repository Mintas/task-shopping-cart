package ru.kovalev.shopping.validation;

import java.util.List;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;
import ru.kovalev.shopping.domain.Cart;

@UnwrapByDefault
public class CartItemsValueExtractor
        implements ValueExtractor<@ExtractedValue(type = List.class) Cart> {

    @Override
    public void extractValues(Cart originalValue, ValueReceiver receiver) {
        receiver.value("items", originalValue.getItems());
    }
}
