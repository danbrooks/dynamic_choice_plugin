package dynamic_choice;

import hudson.model.StringParameterValue;

import org.kohsuke.stapler.DataBoundConstructor;

public class DynamicChoiceParameterValue extends StringParameterValue {
    @DataBoundConstructor
    public DynamicChoiceParameterValue(String name, String value) {
        super(name, value);
    }
}
