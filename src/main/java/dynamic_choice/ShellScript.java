package dynamic_choice;

import hudson.Launcher;
import hudson.util.StreamTaskListener;

import java.io.*;

public abstract class ShellScript {
    final private static String TEMP_PREFIX = "jenkins-dc-";
    protected String scriptContents;

    public ShellScript(String scriptContents) {
        this.scriptContents = scriptContents;
    }

    protected abstract String getExtension();

    protected abstract String[] getCommandLine(File shellScript);

    protected File createFile() throws IOException {
        File shellScript = File.createTempFile(TEMP_PREFIX, getExtension());
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(shellScript.getAbsolutePath()));
        writer.write(scriptContents);
        writer.close();
        shellScript.setExecutable(true);

        return shellScript;
    }

    /*
     * Writes commands to a shell script and returns stdoutput.
     */
    public String run() throws IOException, InterruptedException {
        File shellScriptFile = createFile();
        File baseDir = new File(shellScriptFile.getParent());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Launcher.LocalLauncher launcher = new Launcher.LocalLauncher(StreamTaskListener.fromStdout());
            launcher.launch().cmds(getCommandLine(shellScriptFile)).pwd(baseDir).stdout(out).join();
        } finally {
            shellScriptFile.delete();
        }

        return out.toString();
    }

    public static class BatchFile extends ShellScript {
        final private static String ECHO_OFF = "@ECHO OFF";

        public BatchFile(String contents) {
            super(contents);
        }

        @Override
        protected String getExtension() {
            return ".bat";
        }

        @Override
        protected String[] getCommandLine(File file) {
            return new String[]{"cmd", "/c", "call", file.getAbsolutePath()};
        }

        @Override
        protected File createFile() throws IOException {
            // Make sure echo is off so the commands don't contaminate the output.
            if (!scriptContents.toUpperCase().contains(ECHO_OFF)) {
                scriptContents = ECHO_OFF + "\n" + scriptContents;
            }
            return super.createFile();
        }
    }

    public static class Shell extends ShellScript {
        public Shell(String scriptContents) {
            super(scriptContents);
        }

        @Override
        protected String getExtension() {
            return ".sh";
        }

        @Override
        protected String[] getCommandLine(File file) {
            return new String[]{file.getAbsolutePath()};
        }
    }
}
