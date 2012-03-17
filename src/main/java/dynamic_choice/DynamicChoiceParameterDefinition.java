package dynamic_choice;

import hudson.Extension;
import hudson.Functions;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.util.FormValidation;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

public class DynamicChoiceParameterDefinition extends ParameterDefinition {
    private String command;

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.DynamicChoiceParameterDefinition_DisplayName();
        }

        public FormValidation doCheckCommand(@QueryParameter final String command)
                throws IOException, ServletException {
            if (StringUtils.isBlank(command)) {
                return FormValidation.ok();
            }

            return FormValidation.ok();
        }
    }

    @DataBoundConstructor
    public DynamicChoiceParameterDefinition(String name, String description, String command) {
        super(name, description);
        this.command = command;
    }

    @Override
    public ParameterValue createValue(StaplerRequest request) {
        return null;
    }

    @Override
    public ParameterValue createValue(StaplerRequest request, JSONObject jO) {
        Object value = jO.get("value");
        String strValue = (String) value;

        return new DynamicChoiceParameterValue(getName(), strValue);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getChoices() {
        String output;
        List<String> choices;
        ShellScript script;

        if (Functions.isWindows()) {
            script = new ShellScript.BatchFile(getCommand());
        } else {
            script = new ShellScript.Shell(getCommand());
        }

        try {
            output = script.run();
            output = output.replace("\r", "");
            choices = new ArrayList(Arrays.asList(output.split("\n")));
        } catch (IOException e) {
            choices = null;
        } catch (InterruptedException e) {
            choices = null;
        }

        return choices;
    }
}
