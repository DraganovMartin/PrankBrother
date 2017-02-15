/**
 * Created by DevM on 2/10/2017.
 */
public abstract class StatusCode {
    public static final String SLEEP_PC = "SLEEP";
    public static final String GET_LIST_PROCESSES = "GETPROCESSES";
    public static final String KILL_A_PROCESS = "KILLPROCESS";
    public static final String HELP = "HELP";
    public static final String EXIT = "EXIT";
    public static final String CHECK_CONNECTION = "CHECKCONN";
    public static final String KILL_SERVER = "STOPSER";
    public static final String SEND_KEYSTROKES = "STROKES";
    public static final String SEND_FILE = "FILE";




    public static final String[] statusCodes = {SLEEP_PC,GET_LIST_PROCESSES,KILL_A_PROCESS,HELP,EXIT,CHECK_CONNECTION,KILL_SERVER,SEND_KEYSTROKES,SEND_FILE};
}
