package ml.karmaconfigs.ModPackUpdater.Utils.Launcher;

/*
Bro, this exists because this was about to be a
modpack launcher, but forge > 1.13 it's a real shit
and make everything not to work, honestly, I think
I will re-implement the launch modpack option, but
only supporting Forge < 1.12.2 (included)
 */
public final class ArgumentBuilder {

    private static String arguments;

    /**
     * Initialize the argument builder
     */
    public ArgumentBuilder() {
        arguments = "java ";
    }

    /**
     * Add the argument
     *
     * @param arg the argument
     */
    public final void addArgument(String arg, Object val) {
        arguments = arguments + arg + " " + val + " ";
    }

    public final void addFlag(String arg, Object val) {
        arguments = arguments + arg + val;
    }

    private String buildArgument() {
        String[] argData = arguments.split(" ");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < argData.length; i++) {
            if (i != argData.length - 1) {
                builder.append(argData[i]).append(",");
            } else {
                builder.append(argData[i]);
            }
        }

        return builder.toString();
    }

    public final String[] getArguments() {
        String args = buildArgument();;

        return args.split(",");
    }

    public final String getRawArgs() {
        return arguments;
    }
}
