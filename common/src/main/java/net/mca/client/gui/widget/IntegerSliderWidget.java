package net.mca.client.gui.widget;

import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class IntegerSliderWidget extends ExtendedSliderWidget<Integer> {
    private final int min;
    private final int max;
    private final Function<Integer, Text> textFunction;

    public IntegerSliderWidget(int x, int y, int width, int height, double value, int min, int max, Consumer<Integer> onApplyValue, Function<Integer, Text> function) {
        super(x, y, width, height, Text.literal(""), (value - min) / (max - min), onApplyValue);
        this.min = min;
        this.max = max;
        textFunction = function;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(textFunction.apply(getValue()));
    }

    @Override
    Integer getValue() {
        return (int)(value * (max - min) + min);
    }

    @Override
    protected double getOpticalValue() {
        return ((double)getValue() - min) / (max - min);
    }
}
